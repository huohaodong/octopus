package com.huohaodong.octopus.broker.store.message.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultMessageIdGeneratorTest {
    DefaultMessageIdGenerator idGenerator;

    @BeforeEach
    void init() {
        idGenerator = new DefaultMessageIdGenerator();
    }

    @Test
    void testAcquireAndRelease() {
        Set<Integer> ids = new HashSet<>();
        for (int i = 1; i <= 65535; i++) {
            ids.add(idGenerator.acquireId());
        }
        idGenerator.releaseId(6);
        idGenerator.releaseId(7);
        idGenerator.releaseId(8);
        assertEquals(idGenerator.acquireId(), 6);
        assertEquals(idGenerator.acquireId(), 7);
        assertEquals(idGenerator.acquireId(), 8);
    }
}