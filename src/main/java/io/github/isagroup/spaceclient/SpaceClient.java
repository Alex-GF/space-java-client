package io.github.isagroup.spaceclient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.isagroup.spaceclient.modules.CacheModule;
import io.github.isagroup.spaceclient.modules.ContractModule;
import io.github.isagroup.spaceclient.modules.FeatureModule;
import io.github.isagroup.spaceclient.modules.PricingModule;
import io.github.isagroup.spaceclient.modules.ServiceModule;
import io.github.isagroup.spaceclient.types.ContractEventDetails;
import io.github.isagroup.spaceclient.types.PricingEventDetails;
import io.github.isagroup.spaceclient.types.ResyncInfo;
import io.github.isagroup.spaceclient.types.SpaceConnectionOptions;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.thread.EventThread;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * The SpaceClient class provides an interface to interact with the Space API and WebSocket services
 * It allows for HTTP requests, WebSocket connections, and event handling for real-time updates
 */
public class SpaceClient {
    private static final Logger logger = LoggerFactory.getLogger(SpaceClient.class);

    private final String httpUrl;
    private final String apiKey;
    private final String organizationId;
    private final int timeout;
    
    private Socket socketClient;
    private Socket pricingSocketNamespace;
    private Socket contractSocketNamespace;
    
    private static final Set<String> CONTRACT_EVENTS = new HashSet<>(Arrays.asList(
            "contract_created", "contract_novated", "contract_deleted", "contract_renewed"
    ));

    private final Set<String> validEvents = new HashSet<>(Arrays.asList(
            "synchronized", "pricing_created", "pricing_archived", 
            "pricing_actived", "service_disabled", "error", "resync",
            "contract_created", "contract_novated", "contract_deleted", "contract_renewed"
    ));
    
    // Registered from application threads, read from the socket.io event thread.
    private final Map<String, Consumer<Object>> callbackFunctions = new java.util.concurrent.ConcurrentHashMap<>();
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /** The {@code eventId}s seen recently, shared by both namespaces; {@code null} when off. */
    private final EventIdWindow eventIds;

    /** The namespaces that connected before, to tell a reconnection from the first connection. */
    private final Set<String> connectedBefore = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * How long the capabilities a SPACE response advertises ({@code X-Space-Capabilities}) are
     * trusted before the client checks them again.
     */
    public static final long CAPABILITIES_TTL_MS = 60_000L;

    /** The capabilities last seen on a SPACE response, or {@code null} if unknown. */
    private volatile Set<String> capabilities;
    private volatile long capabilitiesSeenAt;
    private volatile LongSupplier clock = System::currentTimeMillis;

    /** Upper bound of {@link SpaceConnectionOptions#getRetries()}. */
    public static final int MAX_RETRIES = 10;

    private final int retries;
    private final long retryBaseDelayMs;
    private final long retryMaxDelayMs;

    /** Waits between retries; replaced in tests. */
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private volatile Sleeper sleeper = Thread::sleep;
    private volatile DoubleSupplier random = Math::random;
    
    public final CacheModule cache;
    public final ContractModule contracts;
    public final FeatureModule features;
    public final PricingModule pricings;
    public final ServiceModule services;

    /**
     * Constructs a new instance of the SpaceClient class
     *
     * @param options Configuration options for the client
     */
    public SpaceClient(SpaceConnectionOptions options) {
        if (options.getUrl() == null || options.getUrl().isEmpty()) {
            throw new IllegalArgumentException("URL is required");
        }
        if (options.getApiKey() == null || options.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }

        String url = options.getUrl();
        this.httpUrl = url.endsWith("/") 
                ? url.substring(0, url.length() - 1) + "/api/v1" 
                : url + "/api/v1";
        this.apiKey = options.getApiKey();
        this.organizationId = options.getOrganizationId();
        this.timeout = options.getTimeout();
        this.eventIds = options.isEventDeduplication()
                ? new EventIdWindow(options.getEventDeduplicationMaxEntries(), options.getEventDeduplicationWindow())
                : null;

        this.retries = options.getRetries();
        this.retryBaseDelayMs = options.getRetryBaseDelayMs();
        this.retryMaxDelayMs = options.getRetryMaxDelayMs();
        if (retries < 0 || retries > MAX_RETRIES) {
            throw new IllegalArgumentException("retries must be from 0 to " + MAX_RETRIES);
        }
        if (retryBaseDelayMs < 0 || retryMaxDelayMs < 0) {
            throw new IllegalArgumentException("retryBaseDelayMs and retryMaxDelayMs must not be negative");
        }

        // Initialize ObjectMapper with JavaTimeModule for Date handling
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Initialize HTTP client
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();

        // Initialize modules
        this.cache = new CacheModule(this);
        this.contracts = new ContractModule(this, httpClient, objectMapper);
        this.features = new FeatureModule(this, httpClient, objectMapper);
        this.pricings = new PricingModule(this, httpClient);
        this.services = new ServiceModule(this, httpClient, objectMapper);

        // Initialize cache if configured
        if (options.getCache() != null && options.getCache().isEnabled()) {
            initializeCache(options.getCache());
        }

        // Initialize WebSocket
        try {
            initializeWebSocket(url);
        } catch (URISyntaxException e) {
            logger.error("Failed to initialize WebSocket", e);
            throw new RuntimeException("Failed to initialize WebSocket: " + e.getMessage(), e);
        }
    }

    // State of the socket.io manager, read and written on the socket.io event thread only.
    /** Whether the manager's connection is open (between its {@code open} and {@code close} events). */
    private boolean managerOpen;
    /** Whether disconnect() started closing an open connection that has not closed yet. */
    private boolean managerClosing;
    /** Whether connect() was called while {@link #managerClosing}: connect once the close is done. */
    private boolean connectWhenClosed;

    private final Emitter.Listener onManagerOpen = args -> managerOpen = true;
    private final Emitter.Listener onManagerClose = args -> {
        managerOpen = false;
        managerClosing = false;
        if (connectWhenClosed) {
            connectWhenClosed = false;
            pricingSocketNamespace.connect();
                if (hasContractListeners()) connectContractSocket();
        }
    };

    /**
     * Initialize WebSocket connection
     */
    private void initializeWebSocket(String url) throws URISyntaxException {
        String wsUrl = url.replace("http://", "ws://").replace("https://", "wss://");
        
        IO.Options opts = new IO.Options();
        opts.path = "/events";
        opts.transports = new String[]{"websocket"};
        opts.reconnection = true;

        this.socketClient = IO.socket(wsUrl, opts);
        // Don't auto-connect on initialization
        this.pricingSocketNamespace = socketClient.io().socket("/pricings");
        socketClient.io().on(Manager.EVENT_OPEN, onManagerOpen);
        socketClient.io().on(Manager.EVENT_CLOSE, onManagerClose);
        
        configureSocket();
        // Connect manually after setup
        this.pricingSocketNamespace.connect();
                if (hasContractListeners()) connectContractSocket();
    }

    /**
     * Initialize the cache with the provided options
     */
    private void initializeCache(io.github.isagroup.spaceclient.types.CacheOptions cacheOptions) {
        try {
            cache.initialize(cacheOptions);
        } catch (Exception e) {
            logger.warn("[SpaceClient] Cache initialization failed, continuing without cache", e);
        }
    }

    /**
     * Configure WebSocket event handlers
     */
    private void configureSocket() {
        pricingSocketNamespace.on(Socket.EVENT_CONNECT, args -> {
            logger.info("Connected to Space WebSocket");
            Consumer<Object> callback = callbackFunctions.get("synchronized");
            if (callback != null) {
                callback.accept(null);
            }
            onNamespaceConnected(ResyncInfo.PRICINGS);
        });

        pricingSocketNamespace.on("message", args -> {
            if (args.length > 0 && args[0] instanceof JSONObject) {
                dispatchPricingMessage((JSONObject) args[0]);
            }
        });

        pricingSocketNamespace.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Consumer<Object> callback = callbackFunctions.get("error");
            if (callback != null) {
                callback.accept(args.length > 0 ? args[0] : null);
            }
        });
    }

    /**
     * Dispatches a message of the {@code /pricings} namespace to its listener. Contract codes are
     * ignored here: contract events only come from the authenticated {@code /contracts} namespace.
     */
    void dispatchPricingMessage(JSONObject data) {
        String code = data.optString("code", "").toLowerCase();
        if (CONTRACT_EVENTS.contains(code)) {
            return;
        }
        if (!acceptEvent(data)) {
            return;
        }
        Consumer<Object> callback = callbackFunctions.get(code);
        if (callback != null) {
            PricingEventDetails details = PricingEventDetails.fromMessage(data);
            callback.accept(details != null ? details : data.opt("details"));
        }
    }

    /**
     * Whether to deliver a message: {@code false} for a repeat of an {@code eventId} seen in the
     * deduplication window. Messages without {@code eventId} (older SPACE) always pass.
     */
    private boolean acceptEvent(JSONObject data) {
        Object eventId = data.opt("eventId");
        if (eventIds == null || !(eventId instanceof String) || ((String) eventId).isEmpty()) {
            return true;
        }
        return eventIds.accept((String) eventId);
    }

    /**
     * Called when a namespace ({@code "pricings"} or {@code "contracts"}) connects: after a
     * reconnection of {@code /contracts} the cached data of every user is invalidated (contract
     * events sent meanwhile are lost), then the {@code resync} listener receives a
     * {@link ResyncInfo}.
     */
    void onNamespaceConnected(String namespace) {
        boolean reconnect = !connectedBefore.add(namespace);
        if (reconnect && ResyncInfo.CONTRACTS.equals(namespace) && cache != null && cache.isEnabled()) {
            cache.invalidateAllUsers();
        }
        Consumer<Object> callback = callbackFunctions.get("resync");
        if (callback != null) {
            callback.accept(new ResyncInfo(namespace, reconnect));
        }
    }

    /**
     * Dispatches a message of the {@code /contracts} namespace: invalidates the cached data of the
     * affected user first (so a callback that re-reads gets fresh data), then calls the listener
     * with a {@link ContractEventDetails}.
     */
    void dispatchContractMessage(JSONObject data) {
        if (!acceptEvent(data)) {
            return;
        }
        String code = data.optString("code", "").toLowerCase();
        ContractEventDetails details = ContractEventDetails.fromMessage(data);
        if (details != null && details.getUserId() != null && cache != null && cache.isEnabled()) {
            cache.invalidateUser(details.getUserId());
        }
        if (!CONTRACT_EVENTS.contains(code)) {
            return;
        }
        Consumer<Object> callback = callbackFunctions.get(code);
        if (callback != null) {
            callback.accept(details);
        }
    }

    /**
     * Opens the authenticated {@code /contracts} namespace (creating and wiring it on first use).
     * The API key goes in the socket.io {@code auth} payload: {@code {apiKey: "..."}}, plus
     * {@code organizationId} when configured (member user keys).
     */
    private synchronized void connectContractSocket() {
        if (socketClient == null) {
            return;
        }
        if (contractSocketNamespace == null) {
            io.socket.client.Manager.Options opts = new io.socket.client.Manager.Options();
            opts.auth = new HashMap<>();
            opts.auth.put("apiKey", apiKey);
            if (organizationId != null && !organizationId.isEmpty()) {
                // Member user keys act in an organisation they name.
                opts.auth.put("organizationId", organizationId);
            }
            contractSocketNamespace = socketClient.io().socket("/contracts", opts);
            contractSocketNamespace.on(Socket.EVENT_CONNECT, args -> onNamespaceConnected(ResyncInfo.CONTRACTS));
            contractSocketNamespace.on("message", args -> {
                if (args.length > 0 && args[0] instanceof JSONObject) {
                    dispatchContractMessage((JSONObject) args[0]);
                }
            });
            contractSocketNamespace.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Consumer<Object> callback = callbackFunctions.get("error");
                if (callback != null) {
                    callback.accept(args.length > 0 ? args[0] : null);
                }
            });
        }
        if (!contractSocketNamespace.connected()) {
            contractSocketNamespace.connect();
        }
    }

    private synchronized void disconnectContractSocket() {
        if (contractSocketNamespace != null) {
            contractSocketNamespace.disconnect();
        }
    }

    private boolean hasContractListeners() {
        for (String event : CONTRACT_EVENTS) {
            if (callbackFunctions.containsKey(event)) {
                return true;
            }
        }
        return false;
    }

    /** The {@code /contracts} namespace socket, or {@code null} if no contract listener was registered. */
    Socket contractSocketNamespace() {
        return contractSocketNamespace;
    }

    /**
     * Checks if the client is connected to the Space API by performing a health check
     *
     * @return true if the connection is healthy, otherwise false
     */
    public boolean isConnectedToSpace() {
        Request request = new Request.Builder()
                .url(httpUrl + "/healthcheck")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseBody);
                    return json.has("message");
                } catch (org.json.JSONException e) {
                    logger.error("Failed to parse health check response", e);
                    return false;
                }
            }
            return false;
        } catch (IOException e) {
            logger.error("Health check failed", e);
            return false;
        }
    }

    /**
     * Registers a callback function for a specific event
     *
     * Contract events ({@code contract_created}, {@code contract_novated},
     * {@code contract_deleted}, {@code contract_renewed}) are received on the authenticated
     * {@code /contracts} namespace, only for contracts of the API key's organisation; registering
     * the first one opens it. Their callback receives a {@link ContractEventDetails}, after the
     * affected user's cache entries have been invalidated. Pricing callbacks receive a
     * {@link PricingEventDetails} (a {@link JSONObject}). Both carry {@code eventId} and
     * {@code occurredAt}; a repeated {@code eventId} is not delivered again.
     *
     * {@code resync} fires each time a namespace connects, with a {@link ResyncInfo}. SPACE does
     * not replay events, so after a reconnection re-read the contracts you hold.
     *
     * @param event The name of the event to listen for
     * @param callback The function to execute when the event is triggered
     */
    public void on(String event, Consumer<Object> callback) {
        String eventLower = event.toLowerCase();
        if (validEvents.contains(eventLower)) {
            callbackFunctions.put(eventLower, callback);
            if (CONTRACT_EVENTS.contains(eventLower)) {
                connectContractSocket();
            }
        } else {
            logger.warn("No handler for event: {}", event);
        }
    }

    /**
     * Remove a specific event listener
     *
     * @param event The name of the event to remove
     */
    public void removeListener(String event) {
        String eventLower = event.toLowerCase();
        if (validEvents.contains(eventLower)) {
            callbackFunctions.remove(eventLower);
            if (!hasContractListeners()) {
                disconnectContractSocket();
            }
        } else {
            logger.warn("No handler to remove for event: {}", event);
        }
    }

    /**
     * Remove all event listeners
     */
    public void removeAllListeners() {
        callbackFunctions.clear();
        disconnectContractSocket();
    }

    /**
     * Establishes a connection to the pricing WebSocket namespace. If already connected or
     * connecting, this method does nothing. Called right after {@link #disconnect()}, it connects
     * once the previous connection has closed.
     */
    public void connect() {
        if (pricingSocketNamespace == null) {
            return;
        }
        // On the event thread, after any disconnect() queued before it.
        EventThread.exec(() -> {
            if (managerClosing) {
                // Opening now would race with the pending close of the previous connection.
                connectWhenClosed = true;
            } else {
                pricingSocketNamespace.connect();
                if (hasContractListeners()) connectContractSocket();
            }
        });
    }

    /**
     * Disconnects from the pricing WebSocket namespace. The socket handlers and the listeners
     * registered with {@link #on(String, Consumer)} are kept, so {@link #connect()} resumes
     * delivering events to them. A connection attempt in progress is stopped too: no further
     * reconnection attempts are made.
     */
    public void disconnect() {
        if (pricingSocketNamespace == null) {
            return;
        }
        EventThread.exec(() -> {
            connectWhenClosed = false;
            disconnectContractSocket();
            // Not only when connected: while SPACE is unreachable the socket is retrying, and this
            // stops it. With no namespace left active, socket.io closes the manager and cancels
            // the pending reconnection.
            pricingSocketNamespace.disconnect();
            // An open connection may finish closing later (after its last write is flushed);
            // the manager's close event then clears this.
            managerClosing = managerOpen;
        });
    }

    /**
     * Close all connections and cleanup resources
     */
    public void close() {
        disconnect();
        if (contractSocketNamespace != null) contractSocketNamespace.off();
        if (pricingSocketNamespace != null) {
            // The client is not reused after close(): drop the socket handlers.
            pricingSocketNamespace.off();
            Manager manager = socketClient.io();
            EventThread.exec(() -> {
                manager.off(Manager.EVENT_OPEN, onManagerOpen);
                manager.off(Manager.EVENT_CLOSE, onManagerClose);
            });
        }
        
        if (cache != null) {
            cache.close();
        }
        
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }

    /**
     * Records the capabilities advertised by a SPACE response ({@code X-Space-Capabilities}, see S0
     * of the design). A successful response without the header comes from a SPACE that predates
     * it, i.e. one that implements none of the advertised features; a refusal without it is
     * ignored, since it may come from a proxy. Called by the modules for every response.
     *
     * @param header     the header value, or {@code null} if absent
     * @param successful whether the response was a 2xx/3xx
     */
    public void observeCapabilities(String header, boolean successful) {
        if (header == null && !successful) {
            return;
        }
        Set<String> tokens = new HashSet<>();
        if (header != null) {
            for (String token : header.split(",")) {
                String trimmed = token.trim().toLowerCase(Locale.ROOT);
                if (!trimmed.isEmpty()) {
                    tokens.add(trimmed);
                }
            }
        }
        capabilitiesSeenAt = clock.getAsLong();
        capabilities = Collections.unmodifiableSet(tokens);
    }

    /**
     * The capabilities seen on a SPACE response less than {@link #CAPABILITIES_TTL_MS} ago, or
     * {@code null} if they have to be checked again.
     */
    public Set<String> getCachedCapabilities() {
        Set<String> known = capabilities;
        if (known == null || clock.getAsLong() - capabilitiesSeenAt >= CAPABILITIES_TTL_MS) {
            return null;
        }
        return known;
    }

    /**
     * Whether the connected SPACE advertises a capability token in {@code X-Space-Capabilities}
     * (e.g. {@code idempotency-key}, {@code atomic-usage-limits}). Uses the capabilities seen on a
     * response less than {@link #CAPABILITIES_TTL_MS} ago; when there are none, reads them from
     * {@code GET /healthcheck} (unauthenticated). {@code false} when they cannot be read.
     *
     * @param capability the token to look for
     */
    public boolean hasCapability(String capability) {
        Set<String> known = getCachedCapabilities();
        if (known == null) {
            Request request = new Request.Builder().url(httpUrl + "/healthcheck").get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                observeCapabilities(response.header("X-Space-Capabilities"), response.isSuccessful());
            } catch (IOException e) {
                logger.debug("Could not read the SPACE capabilities: {}", e.getMessage());
            }
            known = getCachedCapabilities();
        }
        return known != null && known.contains(capability.toLowerCase(Locale.ROOT));
    }

    /** Retries of writes that carry an {@code Idempotency-Key} (see {@link SpaceConnectionOptions#getRetries()}). */
    public int getRetries() {
        return retries;
    }

    /**
     * How long to wait before retry number {@code attempt + 1}, in milliseconds, or {@code -1} to
     * stop: {@code Retry-After} when SPACE sent one (stop if it is longer than
     * {@code retryMaxDelayMs}), otherwise an exponential backoff with jitter capped at
     * {@code retryMaxDelayMs}. Used by the modules.
     *
     * @param attempt           0 for the first retry
     * @param retryAfterSeconds the parsed {@code Retry-After}, or {@code null}
     */
    public long retryDelayMillis(int attempt, Integer retryAfterSeconds) {
        if (retryAfterSeconds != null) {
            long millis = retryAfterSeconds * 1000L;
            return millis <= retryMaxDelayMs ? millis : -1;
        }
        double ceiling = Math.min((double) retryMaxDelayMs, retryBaseDelayMs * Math.pow(2, attempt));
        return Math.round(ceiling * (0.5 + random.getAsDouble() / 2));
    }

    /**
     * Waits before a retry. Used by the modules.
     *
     * @throws io.github.isagroup.spaceclient.exceptions.SpaceException if the thread is
     *         interrupted (its interrupt flag is restored)
     */
    public void sleepBeforeRetry(long millis) {
        try {
            sleeper.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new io.github.isagroup.spaceclient.exceptions.SpaceException("Interrupted while waiting to retry", e);
        }
    }

    /** Replaces the wait between retries (tests only). */
    void setSleeperForTesting(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /** Replaces the jitter source of the retry backoff (tests only). */
    void setRandomForTesting(DoubleSupplier random) {
        this.random = random;
    }

    /** Forgets the cached capabilities, e.g. right after upgrading SPACE. */
    public void clearCapabilities() {
        capabilities = null;
    }

    /** Replaces the clock of the capability cache (tests only). */
    void setClockForTesting(LongSupplier clock) {
        this.clock = clock;
    }

    // Getters
    public String getHttpUrl() {
        return httpUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getTimeout() {
        return timeout;
    }



    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
