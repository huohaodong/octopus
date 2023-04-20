package com.huohaodong.octopus.exporter;

import com.huohaodong.octopus.exporter.config.ExporterProperties;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j(topic = "EXPORTER")
@RequiredArgsConstructor
@Component
public class ExporterInitializer {
    private final ExporterProperties exporterProperties;

    private void initMetrics() {
        Gauge CONNECTION_COUNT = Gauge.build().name("CONNECTION_COUNT").help("CONN").register();
        Gauge SUBSCRIPTION_COUNT = Gauge.build().name("SUBSCRIPTION_COUNT").help("CONN").register();
        Gauge WILL_COUNT = Gauge.build().name("WILL_COUNT").help("CONN").register();
        Gauge RETAIN_COUNT = Gauge.build().name("RETAIN_COUNT").help("CONN").register();
    }

    @PostConstruct
    public void initExporter() throws IOException {
        initMetrics();
        new HTTPServer(exporterProperties.getPort());
        log.info("Prometheus Exporter started, listening on port {}", exporterProperties.getPort());
    }
}
