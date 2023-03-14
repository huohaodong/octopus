package com.huohaodong.octopus.broker.store.message;

public interface MessageIdGenerator {

    int acquireId();

    void releaseId(int id);

}
