package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.MessageIdGenerator;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DefaultMessageIdGenerator implements MessageIdGenerator {

    private final int ID_MIN = 1;

    private final int ID_MAX = 65536;

    private final Set<Integer> ID_POOL = new HashSet<>(List.of(0));

    private int CANDIDATE = ID_MIN;

    @Override
    public synchronized int acquireId() {
        while (ID_POOL.contains(CANDIDATE)) {
            CANDIDATE = (CANDIDATE + 1) % ID_MAX;
        }
        ID_POOL.add(CANDIDATE);
        return CANDIDATE;
    }

    @Override
    public synchronized void releaseId(int id) {
        ID_POOL.remove(id);
    }
}
