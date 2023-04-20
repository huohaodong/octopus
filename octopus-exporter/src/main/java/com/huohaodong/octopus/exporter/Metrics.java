package com.huohaodong.octopus.exporter;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

import static com.huohaodong.octopus.exporter.Constants.*;


public class Metrics {
    public static Gauge CONNECTION_ACTIVE;

    public static Gauge SUBSCRIPTION_ACTIVE;

    public static Gauge WILL_MESSAGE_ACTIVE;

    public static Gauge RETAIN_MESSAGE_ACTIVE;

    public static Gauge TOPIC_ACTIVE;

    public static Counter MESSAGE_SENT_TOTAL;

    public static Counter MESSAGE_RECEIVED_TOTAL;

    public static void init() {
        CONNECTION_ACTIVE = Gauge.build().name(METRIC_CONNECTION_ACTIVE).help(HELP_METRIC_CONNECTION_ACTIVE).register();
        SUBSCRIPTION_ACTIVE = Gauge.build().name(METRIC_SUBSCRIPTION_ACTIVE).help(HELP_METRIC_SUBSCRIPTION_ACTIVE).register();
        WILL_MESSAGE_ACTIVE = Gauge.build().name(METRIC_WILL_MESSAGE_ACTIVE).help(HELP_METRIC_WILL_MESSAGE_ACTIVE).register();
        RETAIN_MESSAGE_ACTIVE = Gauge.build().name(METRIC_RETAIN_MESSAGE_ACTIVE).help(HELP_METRIC_RETAIN_MESSAGE_ACTIVE).register();
        TOPIC_ACTIVE = Gauge.build().name(METRIC_TOPIC_ACTIVE).help(HELP_METRIC_TOPIC_ACTIVE).register();
        MESSAGE_SENT_TOTAL = Counter.build().name(METRIC_MESSAGE_SENT_TOTAL).help(HELP_METRIC_MESSAGE_SENT_TOTAL).register();
        MESSAGE_RECEIVED_TOTAL = Counter.build().name(METRIC_MESSAGE_RECEIVED_TOTAL).help(HELP_METRIC_MESSAGE_RECEIVED_TOTAL).register();
    }
}