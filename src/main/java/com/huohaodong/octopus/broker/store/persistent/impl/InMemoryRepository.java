package com.huohaodong.octopus.broker.store.persistent.impl;

import com.huohaodong.octopus.broker.store.persistent.Repository;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository<K, V> implements Repository<K, V> {
    ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    public InMemoryRepository() {
    }

    @Override
    public V put(K k, V v) {
        return map.put(k, v);
    }

    @Override
    public V remove(K k) {
        return map.remove(k);
    }

    @Override
    public V get(K k) {
        return map.get(k);
    }
}
