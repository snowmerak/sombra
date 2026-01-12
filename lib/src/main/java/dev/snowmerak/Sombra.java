package dev.snowmerak;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.RedisClient;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;

import java.time.Duration;

public class Sombra implements AutoCloseable {
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final CacheFrontend<String, String> cacheFrontend;
    private final Cache<String, String> localCache;

    public Sombra(String redisUri, Duration expireAfter, long maxSize) {
        this.redisClient = RedisClient.create(redisUri);
        this.connection = redisClient.connect();

        this.localCache = Caffeine.newBuilder()
                .expireAfterWrite(expireAfter)
                .maximumSize(maxSize)
                .build();

        // Enable Server-Assisted Client Side Caching
        this.cacheFrontend = ClientSideCaching.enable(
                CacheAccessor.forMap(localCache.asMap()),
                connection,
                TrackingArgs.Builder.enabled()
        );
    }

    public String get(String key) {
        return cacheFrontend.get(key);
    }

    public void set(String key, String value) {
        connection.sync().set(key, value);
    }

    public Cache<String, String> getLocalCache() {
        return localCache;
    }

    @Override
    public void close() {
        connection.close();
        redisClient.shutdown();
    }
}
