package com.huohaodong.octopus.exporter.metric;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

import java.util.Optional;

import static com.huohaodong.octopus.exporter.metric.Constants.*;
import static com.huohaodong.octopus.exporter.metric.Constants.HELP_METRIC_MESSAGE_RECEIVED_TOTAL;

public class Metrics {
    private static Gauge CONNECTION_ACTIVE;
    private static Gauge SUBSCRIPTION_ACTIVE;
    private static Gauge WILL_MESSAGE_ACTIVE;
    private static Gauge RETAIN_MESSAGE_ACTIVE;
    private static Gauge TOPIC_ACTIVE;
    private static Counter MESSAGE_SENT_TOTAL;
    private static Counter MESSAGE_RECEIVED_TOTAL;

    public static Optional<Gauge> getGaugeByName(String name) {
        switch (name) {
            case METRIC_CONNECTION_ACTIVE -> {
                return Optional.ofNullable(CONNECTION_ACTIVE);
            }
            case METRIC_SUBSCRIPTION_ACTIVE -> {
                return Optional.ofNullable(SUBSCRIPTION_ACTIVE);
            }
            case METRIC_WILL_MESSAGE_ACTIVE -> {
                return Optional.ofNullable(WILL_MESSAGE_ACTIVE);
            }
            case METRIC_RETAIN_MESSAGE_ACTIVE -> {
                return Optional.ofNullable(RETAIN_MESSAGE_ACTIVE);
            }
            case METRIC_TOPIC_ACTIVE -> {
                return Optional.ofNullable(TOPIC_ACTIVE);
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    public static Optional<Counter> getCounterByName(String name) {
        switch (name) {
            case METRIC_MESSAGE_SENT_TOTAL -> {
                return Optional.ofNullable(MESSAGE_SENT_TOTAL);
            }
            case METRIC_MESSAGE_RECEIVED_TOTAL -> {
                return Optional.ofNullable(MESSAGE_RECEIVED_TOTAL);
            }
            default -> {
                return Optional.empty();
            }
        }
    }

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
