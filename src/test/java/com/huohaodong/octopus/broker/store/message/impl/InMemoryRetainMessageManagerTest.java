package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.RetainMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryRetainMessageManagerTest {

    private InMemoryRetainMessageManager manager;

    @BeforeEach
    void init() {
        manager = new InMemoryRetainMessageManager();
    }

    @Test
    void put() {

        manager.put("client/sensor/temp", new RetainMessage("client/sensor/temp", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/sensor1/temp", new RetainMessage("client/sensor1/temp", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/sensor2/temp", new RetainMessage("client/sensor2/temp", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/+/temp", new RetainMessage("client/+/temp", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/+/#", new RetainMessage("client/+/#", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/sensor/temp", new RetainMessage("client/sensor/temp", MqttQoS.AT_LEAST_ONCE, null));

        Assertions.assertEquals(manager.getAllMatched("client/sensor/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("client/+/temp").size(), 4);
        Assertions.assertEquals(manager.getAllMatched("client/#").size(), 5);
        Assertions.assertEquals(manager.getAllMatched("client/+/temp#").size(), 0);
        Assertions.assertEquals(manager.size(), 5);

        manager.put("client/sensor/temp", new RetainMessage("client/sensor/temp", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/sensor1/temp", new RetainMessage("client/sensor1/temp", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/sensor2/temp", new RetainMessage("client/sensor2/temp", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/+/temp", new RetainMessage("client/+/temp", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/+/#", new RetainMessage("client/+/#", MqttQoS.AT_LEAST_ONCE, null));
        manager.put("client/sensor/temp", new RetainMessage("client/sensor/temp", MqttQoS.AT_LEAST_ONCE, null));

        Assertions.assertEquals(manager.getAllMatched("client/sensor/temp").size(), 1);
        Assertions.assertEquals(manager.getAllMatched("client/+/temp").size(), 4);
        Assertions.assertEquals(manager.getAllMatched("client/#").size(), 5);
        Assertions.assertEquals(manager.getAllMatched("client/+/temp#").size(), 0);
        Assertions.assertEquals(manager.size(), 5);

    }

    @Test
    void get() {

        RetainMessage message1 = new RetainMessage("client/sensor/temp", MqttQoS.AT_LEAST_ONCE, null);
        RetainMessage message2 = new RetainMessage("client/sensor1/temp", MqttQoS.AT_LEAST_ONCE, null);
        RetainMessage message3 = new RetainMessage("client/sensor2/temp", MqttQoS.AT_LEAST_ONCE, null);

        manager.put("client/sensor/temp", message1);
        manager.put("client/sensor1/temp", message2);
        manager.put("client/sensor2/temp", message3);


        Assertions.assertEquals(manager.get("client/sensor/temp"), message1);
        Assertions.assertEquals(manager.get("client/sensor1/temp"), message2);
        Assertions.assertEquals(manager.get("client/sensor2/temp"), message3);

    }

    @Test
    void remove() {
        RetainMessage message1 = new RetainMessage("client/sensor/temp", MqttQoS.AT_LEAST_ONCE, null);
        RetainMessage message2 = new RetainMessage("client/sensor1/temp", MqttQoS.AT_LEAST_ONCE, null);
        RetainMessage message3 = new RetainMessage("client/sensor2/temp", MqttQoS.AT_LEAST_ONCE, null);

        manager.put(message1.getTopic(), message1);
        manager.put(message2.getTopic(), message2);
        manager.put(message3.getTopic(), message3);

        manager.remove("client/sensor/temp");
        manager.remove("client/sensor1/temp");

        Assertions.assertEquals(manager.get("client/sensor/temp"), null);
        Assertions.assertEquals(manager.get("client/sensor1/temp"), null);
        Assertions.assertEquals(manager.get("client/sensor2/temp"), message3);
        Assertions.assertEquals(manager.size(), 1);

        manager.remove("client/sensor/temp");
        manager.remove("client/sensor1/temp");
        Assertions.assertEquals(manager.size(), 1);

        manager.remove("client/sensor2/temp");
        Assertions.assertEquals(manager.size(), 0);

        manager.remove("client/sensor2/temp");
        Assertions.assertEquals(manager.size(), 0);

    }

    @Test
    void contains() {

        RetainMessage message1 = new RetainMessage("client/sensor/temp", MqttQoS.AT_LEAST_ONCE, null);
        RetainMessage message2 = new RetainMessage("client/sensor1/temp", MqttQoS.AT_LEAST_ONCE, null);
        RetainMessage message3 = new RetainMessage("client/sensor2/temp", MqttQoS.AT_LEAST_ONCE, null);

        manager.put(message1.getTopic(), message1);
        manager.put(message2.getTopic(), message2);
        manager.put(message3.getTopic(), message3);

        manager.remove("client/sensor/temp");

        Assertions.assertFalse(manager.contains("client/sensor/temp"));
        Assertions.assertTrue(manager.contains("client/sensor1/temp"));
        Assertions.assertTrue(manager.contains("client/sensor2/temp"));

    }

}