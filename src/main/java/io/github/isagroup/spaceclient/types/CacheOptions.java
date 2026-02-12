package io.github.isagroup.spaceclient.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cache configuration options
 */
public class CacheOptions {
    
    @JsonProperty("enabled")
    private boolean enabled;
    
    @JsonProperty("type")
    private CacheType type;
    
    @JsonProperty("external")
    private ExternalCacheConfig external;
    
    @JsonProperty("ttl")
    private Integer ttl;

    public CacheOptions() {
        this.type = CacheType.BUILTIN;
        this.ttl = 300; // 5 minutes default
    }

    public CacheOptions(boolean enabled) {
        this.enabled = enabled;
        this.type = CacheType.BUILTIN;
        this.ttl = 300;
    }

    public CacheOptions(boolean enabled, CacheType type, Integer ttl) {
        this.enabled = enabled;
        this.type = type != null ? type : CacheType.BUILTIN;
        this.ttl = ttl != null ? ttl : 300;
    }

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CacheType getType() {
        return type != null ? type : CacheType.BUILTIN;
    }

    public void setType(CacheType type) {
        this.type = type;
    }

    public ExternalCacheConfig getExternal() {
        return external;
    }

    public void setExternal(ExternalCacheConfig external) {
        this.external = external;
    }

    public Integer getTtl() {
        return ttl != null ? ttl : 300;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    /**
     * Cache type enumeration
     */
    public enum CacheType {
        @JsonProperty("builtin")
        BUILTIN,
        @JsonProperty("redis")
        REDIS
    }

    /**
     * External cache configuration
     */
    public static class ExternalCacheConfig {
        @JsonProperty("redis")
        private RedisConfig redis;

        public ExternalCacheConfig() {
        }

        public RedisConfig getRedis() {
            return redis;
        }

        public void setRedis(RedisConfig redis) {
            this.redis = redis;
        }
    }

    /**
     * Redis configuration
     */
    public static class RedisConfig {
        @JsonProperty("host")
        private String host;
        
        @JsonProperty("port")
        private Integer port;
        
        @JsonProperty("password")
        private String password;
        
        @JsonProperty("db")
        private Integer db;
        
        @JsonProperty("connectTimeout")
        private Integer connectTimeout;
        
        @JsonProperty("keyPrefix")
        private String keyPrefix;

        public RedisConfig() {
            this.port = 6379;
            this.db = 0;
            this.connectTimeout = 5000;
            this.keyPrefix = "space-client:";
        }

        public RedisConfig(String host) {
            this();
            this.host = host;
        }

        public RedisConfig(String host, Integer port) {
            this(host);
            this.port = port != null ? port : 6379;
        }

        // Getters and Setters
        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port != null ? port : 6379;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getDb() {
            return db != null ? db : 0;
        }

        public void setDb(Integer db) {
            this.db = db;
        }

        public Integer getConnectTimeout() {
            return connectTimeout != null ? connectTimeout : 5000;
        }

        public void setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public String getKeyPrefix() {
            return keyPrefix != null ? keyPrefix : "space-client:";
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }
}
