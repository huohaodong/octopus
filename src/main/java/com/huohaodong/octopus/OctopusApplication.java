package com.huohaodong.octopus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.function.BiFunction;
import java.util.function.Function;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories
public class OctopusApplication {

    public static void main(String[] args) {
//        SpringApplication.run(OctopusApplication.class, args);
        ConcurrentReferenceHashMap<Integer, String> map = new ConcurrentReferenceHashMap<>();
        map.put(1, "123");
    }

}
