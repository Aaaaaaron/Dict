package dict;

import static java.util.Collections.synchronizedMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V extends CloseableSizedFile> {
    private long maxSizeInBytes = 0L;
    private long currentSizeInBytes = 0L;

    private Map<K, V> cache;

    public LRUCache(long maxSizeInBytes) {
        this.maxSizeInBytes = maxSizeInBytes;
        cache = synchronizedMap(new LinkedHashMap<K, V>(16, 0.75F, true));
    }

    public V put(K key, V value) {
        currentSizeInBytes += value.getSizeInBytes();
        while (currentSizeInBytes > maxSizeInBytes) {
            if (cache.isEmpty()) {
                throw new RuntimeException("dict max size in mem is too small");
            }
            removeLastNotUsed();
        }
        value.init();
        return cache.put(key, value);
    }

    public V getIfPresent(Object key) {
        return cache.get(key);
    }

    public int size() {
        return cache.size();
    }

    private void removeLastNotUsed() {
        if (cache.isEmpty()) {
            return;
        }
        Map.Entry<K, V> eldest = cache.entrySet().iterator().next();
        eldest.getValue().close();
        currentSizeInBytes -= eldest.getValue().getSizeInBytes();
        cache.remove(eldest.getKey());
    }
}