package com.huohaodong.octopus.exporter.metric.aspect;

import com.huohaodong.octopus.exporter.metric.Metrics;
import com.huohaodong.octopus.exporter.metric.annotation.GaugeDecMetric;
import com.huohaodong.octopus.exporter.metric.annotation.GaugeIncMetric;
import io.prometheus.client.Gauge;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExporterAspect {
    @After("@annotation(gaugeMetric)")
    public void gaugePointcut(GaugeIncMetric gaugeMetric) {
        Metrics.getGaugeByName(gaugeMetric.name()).ifPresent(Gauge::inc);
    }

    @After("@annotation(gaugeMetric)")
    public void gaugePointcut(GaugeDecMetric gaugeMetric) {
        Metrics.getGaugeByName(gaugeMetric.name()).ifPresent(Gauge::dec);
    }
}
