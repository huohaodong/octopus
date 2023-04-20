package com.huohaodong.octopus.exporter;

import io.prometheus.client.Gauge;

public class Constants {
    Gauge CONNECTION_COUNT = Gauge.build().name("SUBSCRIPTION").help("CONN").register();
    Gauge SUBSCRIPTION_COUNT = Gauge.build().name("SUBSCRIPTION1").help("CONN").register();
}
