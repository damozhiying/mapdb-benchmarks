package org.mapdb.benchmark;

import org.mapdb.DBMaker;
import org.mapdb.DataIO;
import org.mapdb.Serializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Tests how long it takes to insert 100M items
 */
public class InMemoryCreate {

    static final int memUsage = 20;
    static final int max = (int) 100e6;
    
    static final Map<String, Callable<Map<Long,UUID>>> fabs = new LinkedHashMap(max);
    static{
        fabs.put("ConcurrentHashMap", new Callable<Map<Long, UUID>>() {
            @Override public Map<Long, UUID> call() throws Exception {
                return new ConcurrentHashMap<Long, UUID>();
            }
        });

        fabs.put("ConcurrentSkipListMap", new Callable<Map<Long, UUID>>() {
            @Override public Map<Long, UUID> call() throws Exception {
                return new ConcurrentSkipListMap<Long, UUID>();
            }
        });

        fabs.put("HTreeMap_heap", new Callable<Map<Long, UUID>>() {
            @Override public Map<Long, UUID> call() throws Exception {
                return DBMaker.heapDB().transactionDisable().make()
                        .hashMap("map", Serializer.LONG, Serializer.UUID);
            }
        });

        fabs.put("BTreeMap_heap", new Callable<Map<Long, UUID>>() {
            @Override public Map<Long, UUID> call() throws Exception {
                return DBMaker.heapDB().transactionDisable().make()
                        .treeMap("map", Serializer.LONG, Serializer.UUID);
            }
        });

        fabs.put("HTreeMap_offheap", new Callable<Map<Long, UUID>>() {
            @Override public Map<Long, UUID> call() throws Exception {
                return DBMaker.memoryDB().transactionDisable().asyncWriteEnable().make()
                        .hashMap("map", Serializer.LONG, Serializer.UUID);
            }
        });

        fabs.put("BTreeMap_offheap", new Callable<Map<Long, UUID>>() {
            @Override public Map<Long, UUID> call() throws Exception {
                return DBMaker.memoryDB().asyncWriteEnable()
                        .transactionDisable().make()
                        .treeMap("map", Serializer.LONG, Serializer.UUID);
            }
        });
    }

    public static void main(String[] args) throws Throwable {
        long time = System.currentTimeMillis();
        String name = args[0];
        Map map = fabs.get(name).call();
        for (long i=0;i<max;i++) {
            UUID val = new UUID(DataIO.longHash(i),DataIO.longHash(i+1)); //Random is too slow, so use faster hash
            map.put(i, val);
        }
        System.out.println(System.currentTimeMillis()-time);
    }

}
