package com.huohaodong.octopus.broker.store.subscription.impl;

import com.huohaodong.octopus.broker.store.subscription.Subscription;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class InMemorySubscriptionManagerTest {

    private InMemorySubscriptionManager manager;

    @BeforeEach
    void init() {
        manager = new InMemorySubscriptionManager(new CTrieSubscriptionMatcher());
    }

    @Test
    void subscribe() {

        Subscription sub1 = new Subscription("client1", "sensor/1/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub2 = new Subscription("client2", "sensor/2/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub3 = new Subscription("client3", "sensor/3/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub4 = new Subscription("client4", "sensor/4/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub5 = new Subscription("client5", "sensor/5/temp", MqttQoS.AT_LEAST_ONCE);

        manager.subscribe(sub1);
        manager.subscribe(sub2);
        manager.subscribe(sub3);
        manager.subscribe(sub4);
        manager.subscribe(sub5);

        manager.subscribe(sub1);
        manager.subscribe(sub2);
        manager.subscribe(sub3);
        manager.subscribe(sub4);
        manager.subscribe(new Subscription("client5", "sensor/5/temp", MqttQoS.EXACTLY_ONCE));

        Assertions.assertEquals(manager.getAllMatched("sensor/1/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/2/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/3/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/4/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/5/temp").size(), 1);

        Subscription subAll = new Subscription("client4", "sensor/+/temp", MqttQoS.AT_LEAST_ONCE);
        manager.subscribe(subAll);

        Assertions.assertEquals(manager.getAllMatched("sensor/1/temp").size(), 2);
        Assertions.assertEquals(manager.getAllMatched("sensor/2/temp").size(), 2);
        Assertions.assertEquals(manager.getAllMatched("sensor/3/temp").size(), 2);
        Assertions.assertEquals(manager.getAllMatched("sensor/4/temp").size(), 2);
        Assertions.assertEquals(manager.getAllMatched("sensor/5/temp").size(), 2);

        manager.unSubscribe(sub1);
        manager.unSubscribe(new Subscription("client2", "sensor/2/temp"));
        manager.unSubscribe(subAll);

        Assertions.assertEquals(manager.getAllMatched("sensor/1/temp").size(), 0);
        Assertions.assertEquals(manager.getAllMatched("sensor/2/temp").size(), 0);
        Assertions.assertEquals(manager.getAllMatched("sensor/3/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/4/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/5/temp").size(), 1);

    }

    @Test
    void unSubscribe() {

        Subscription sub1 = new Subscription("client1", "sensor/1/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub2 = new Subscription("client2", "sensor/2/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub3 = new Subscription("client3", "sensor/3/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub4 = new Subscription("client4", "sensor/4/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub5 = new Subscription("client5", "sensor/5/temp", MqttQoS.AT_LEAST_ONCE);

        manager.subscribe(sub1);
        manager.subscribe(sub2);
        manager.subscribe(sub3);
        manager.subscribe(sub4);
        manager.subscribe(sub5);

        Assertions.assertEquals(manager.getAllMatched("sensor/1/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/2/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/3/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/4/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/5/temp").size(), 1);

        manager.unSubscribe(sub1);
        manager.unSubscribe(sub2);
        manager.unSubscribe(sub3);
        manager.unSubscribe(sub4);

        Assertions.assertEquals(manager.getAllMatched("sensor/1/temp").size(), 0);
        Assertions.assertEquals(manager.getAllMatched("sensor/2/temp").size(), 0);
        Assertions.assertEquals(manager.getAllMatched("sensor/3/temp").size(), 0);
        Assertions.assertEquals(manager.getAllMatched("sensor/4/temp").size(), 0);
        Assertions.assertEquals(manager.getAllMatched("sensor/5/temp").size(), 1);

    }

    @Test
    void getAllMatched() {

        Subscription sub1 = new Subscription("client1", "sensor/1/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub2 = new Subscription("client2", "sensor/2/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub3 = new Subscription("client3", "sensor/3/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub4 = new Subscription("client4", "sensor/4/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub5 = new Subscription("client5", "sensor/5/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub6 = new Subscription("client6", "sensor/2/#", MqttQoS.AT_LEAST_ONCE);

        manager.subscribe(sub1);
        manager.subscribe(sub2);
        manager.subscribe(sub3);
        manager.subscribe(sub4);
        manager.subscribe(sub5);
        manager.subscribe(sub6);

        Assertions.assertEquals(manager.getAllMatched("sensor/temp").size(), 0);
        Assertions.assertEquals(manager.getAllMatched("sensor/1/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("sensor/2/temp").size(), 2);

        Subscription sub7 = new Subscription("client2", "sensor/2/#", MqttQoS.AT_LEAST_ONCE);
        manager.subscribe(sub7);
        Assertions.assertEquals(manager.getAllMatched("sensor/2/temp").size(), 3);

    }

    @Test
    void getAllByClientId() {

        Subscription sub1 = new Subscription("client1", "sensor/1/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub2 = new Subscription("client2", "sensor/2/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub3 = new Subscription("client3", "sensor/3/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub4 = new Subscription("client4", "sensor/4/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub5 = new Subscription("client5", "sensor/+/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub6 = new Subscription("client6", "sensor/2/#", MqttQoS.AT_LEAST_ONCE);

        manager.subscribe(sub1);
        manager.subscribe(sub2);
        manager.subscribe(sub3);
        manager.subscribe(sub4);
        manager.subscribe(sub5);
        manager.subscribe(sub6);

        Assertions.assertEquals(manager.getAllByClientId("client1").size(), 1);
        Assertions.assertEquals(manager.getAllByClientId("client2").size(), 1);
        Assertions.assertEquals(manager.getAllByClientId("client3").size(), 1);
        Assertions.assertEquals(manager.getAllByClientId("client4").size(), 1);
        Assertions.assertEquals(manager.getAllByClientId("client5").size(), 1);
        Assertions.assertEquals(manager.getAllByClientId("client6").size(), 1);

        manager.subscribe(sub1);
        manager.subscribe(sub3);
        manager.subscribe(sub5);

        Assertions.assertEquals(manager.getAllByClientId("client1").size(), 2);
        Assertions.assertEquals(manager.getAllByClientId("client3").size(), 2);
        Assertions.assertEquals(manager.getAllByClientId("client5").size(), 2);

        manager.unSubscribeAll("client1");
        assertNull(manager.getAllByClientId("client1"));

        manager.unSubscribeAll("client6");
        assertNull(manager.getAllByClientId("client6"));

        Assertions.assertEquals(manager.getAllByClientId("client2").size(), 1);

    }

    @Test
    void unSubscribeAll() {

        Subscription sub1 = new Subscription("client1", "sensor/1/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub2 = new Subscription("client2", "sensor/2/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub3 = new Subscription("client3", "sensor/+/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub4 = new Subscription("client4", "sensor/2/temp", MqttQoS.AT_LEAST_ONCE);
        Subscription sub5 = new Subscription("client5", "sensor/2/#", MqttQoS.AT_LEAST_ONCE);

        manager.subscribe(sub1);
        manager.subscribe(sub2);
        manager.subscribe(sub3);
        manager.subscribe(sub4);
        manager.subscribe(sub5);

        manager.unSubscribeAll("client3");
        Assertions.assertEquals(manager.getAllByClientId("client1").size(), 1);
        Assertions.assertEquals(manager.getAllByClientId("client2").size(), 1);
        Assertions.assertNull(manager.getAllByClientId("client3"));
        Assertions.assertEquals(manager.getAllByClientId("client4").size(), 1);
        Assertions.assertEquals(manager.getAllByClientId("client5").size(), 1);

        manager.unSubscribeAll("client5");
        Assertions.assertEquals(manager.getAllByClientId("client1").size(), 1);
        Assertions.assertEquals(manager.getAllByClientId("client2").size(), 1);
        Assertions.assertNull(manager.getAllByClientId("client3"));
        Assertions.assertEquals(manager.getAllByClientId("client4").size(), 1);
        Assertions.assertNull(manager.getAllByClientId("client5"));

        manager.unSubscribeAll("client1");
        manager.unSubscribeAll("client2");
        manager.unSubscribeAll("client3");
        manager.unSubscribeAll("client4");
        manager.unSubscribeAll("client5");

        Assertions.assertNull(manager.getAllByClientId("client1"));
        Assertions.assertNull(manager.getAllByClientId("client2"));
        Assertions.assertNull(manager.getAllByClientId("client3"));
        Assertions.assertNull(manager.getAllByClientId("client4"));
        Assertions.assertNull(manager.getAllByClientId("client5"));

    }
}