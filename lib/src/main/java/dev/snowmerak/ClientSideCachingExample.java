package dev.snowmerak;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;
import io.lettuce.core.TrackingArgs;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSideCachingExample {
    
    public void example() {
        RedisClient client = RedisClient.create("redis://localhost");
        StatefulRedisConnection<String, String> connection = client.connect();
        
        Map<String, String> mapConfig = new ConcurrentHashMap<>();
        
        CacheFrontend<String, String> frontend = ClientSideCaching.enable(
            CacheAccessor.forMap(mapConfig), 
            connection, 
            new TrackingArgs().bcast()
        );
        
        frontend.get("key");
    }
}
