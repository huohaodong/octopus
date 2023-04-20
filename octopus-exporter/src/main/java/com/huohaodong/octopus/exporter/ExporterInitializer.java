package com.huohaodong.octopus.exporter;

import com.huohaodong.octopus.exporter.config.ExporterProperties;
import io.prometheus.client.exporter.HTTPServer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j(topic = "EXPORTER")
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "octopus.broker.exporter.enable", havingValue = "true")
public class ExporterInitializer {
    private final ExporterProperties exporterProperties;

    @PostConstruct
    public void initExporter() throws IOException {
        Metrics.init();
        new HTTPServer(exporterProperties.getPort());
        log.info("Prometheus Exporter started, listening on port {}", exporterProperties.getPort());
    }
}
