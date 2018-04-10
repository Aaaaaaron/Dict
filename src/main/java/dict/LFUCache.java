package dict;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class LFUCache {
    public static void main(String[] args) {
        Configuration conf = new Configuration();
//        conf.setMaxBytesLocalHeap("100M");
        CacheManager cacheManager = CacheManager.create(conf);

        net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache( //
                new net.sf.ehcache.config.CacheConfiguration("test", 100) //
                        .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU) //
                        .overflowToDisk(false) //
                        .eternal(false) //
                        .timeToIdleSeconds(86400) //
                        .diskExpiryThreadIntervalSeconds(0) //
                        .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))); //
        cacheManager.addCache(cache);

        cache.put(new Element("1", "a"));
        cache.put(new Element("2", "b"));
        cache.put(new Element("3", "c"));

        System.out.println(cache.get("1"));
        System.out.println(cache.get("2"));
        System.out.println(cache.get("3"));
        System.out.println(cache.get("2"));
        System.out.println(cache.get("3"));

        System.out.println("--------");

        cache.evictExpiredElements();
        System.out.println(cache.isExpired(new Element("1", "a")));
        System.out.println(cache.isExpired(new Element("2", "b")));
        System.out.println(cache.isExpired(new Element("3", "c")));

    }
}
