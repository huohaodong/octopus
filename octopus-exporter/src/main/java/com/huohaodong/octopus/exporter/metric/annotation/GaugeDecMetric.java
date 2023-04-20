package com.huohaodong.octopus.exporter.metric.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface GaugeDecMetric {
    String name() default "";
}