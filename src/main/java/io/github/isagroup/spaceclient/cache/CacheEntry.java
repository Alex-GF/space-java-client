package io.github.isagroup.spaceclient.cache;

/**
 * Cache entry with metadata
 */
public class CacheEntry<T> {
    private T value;
    private long createdAt;
    private int ttl;
    private long expiresAt;

    public CacheEntry() {
    }

    public CacheEntry(T value, int ttl) {
        this.value = value;
        this.ttl = ttl;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = this.createdAt + (ttl * 1000L);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
