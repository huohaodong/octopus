package com.huohaodong.octopus.broker.store.persistent;

import java.util.Collection;

// TODO 按照 JPA 接口规范来设计
public interface Repository<K, V> {
    V put(K k, V v);

    V remove(K k);

    V get(K k);

    boolean containsKey(K k);

    Collection<V> getAll();

    int size();
}
