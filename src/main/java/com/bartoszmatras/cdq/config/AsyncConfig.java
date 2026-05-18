package com.bartoszmatras.cdq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${import.thread-pool.core-size:2}")
    private int coreSize;

    @Value("${import.thread-pool.max-size:4}")
    private int maxSize;

    @Value("${import.thread-pool.queue-capacity:10}")
    private int queueCapacity;

    @Bean(name = "importTaskExecutor")
    public Executor importTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("import-");
        executor.setRejectedExecutionHandler((runnable, pool) -> {
            log.error("Import task rejected - thread pool and queue are full. "
                    + "Pool size: {}, active: {}, queue size: {}",
                    pool.getPoolSize(), pool.getActiveCount(), pool.getQueue().size());
            throw new RejectedExecutionException(
                    "Import service is at capacity. Please try again later.");
        });
        executor.initialize();
        return executor;
    }
}