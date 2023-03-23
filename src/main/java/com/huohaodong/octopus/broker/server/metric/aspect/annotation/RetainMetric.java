package com.huohaodong.octopus.broker.server.metric.aspect.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetainMetric {
}
