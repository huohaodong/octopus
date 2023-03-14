package com.huohaodong.octopus.broker.store.persistent;

// TODO 按照 JPA 接口规范来设计
public interface Repository<K, V> {
    V put(K k, V v);

    V remove(K k);

    V get(K k);

    boolean contains(K k);
}
