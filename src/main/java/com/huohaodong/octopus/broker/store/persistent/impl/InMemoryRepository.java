package com.huohaodong.octopus.broker.store.persistent.impl;

import com.huohaodong.octopus.broker.store.persistent.Repository;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository<K, V> implements Repository<K, V> {
    ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    public InMemoryRepository() {
    }

    @Override
    public V put(K k, V v) {
        if (k == null) {
            return null;
        }
        return map.put(k, v);
    }

    @Override
    public V remove(K k) {
        if (k == null) {
            return null;
        }
        return map.remove(k);
    }

    @Override
    public V get(K k) {
        if (k == null) {
            return null;
        }
        return map.get(k);
    }

    @Override
    public boolean containsKey(K k) {
        if (k == null) {
            return false;
        }
        return map.containsKey(k);
    }

    @Override
    public Collection<V> getAll() {
        return map.values();
    }

    @Override
    public int size() {
        return map.size();
    }
}
