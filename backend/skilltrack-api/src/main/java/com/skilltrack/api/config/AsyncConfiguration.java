package com.skilltrack.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for asynchronous task execution.
 * 
 * Purpose:
 * - Enable @Async annotation support
 * - Allow email sending to run in background threads
 * - Prevent blocking HTTP request-response cycle
 * 
 * Benefits:
 * - Faster API response times (doesn't wait for email to send)
 * - Better user experience (user gets immediate feedback)
 * - Improved throughput (server can handle more requests)
 * - Resilience (email failures don't fail registration)
 * 
 * Thread Pool:
 * - Uses default Spring task executor
 * - Can be customized with @Bean TaskExecutor if needed
 * 
 * Usage:
 * - EmailService methods annotated with @Async
 * - Methods return void or Future<T>
 * - Exceptions logged but don't propagate to caller
 * 
 * Production Considerations:
 * - Configure thread pool size based on load
 * - Consider using dedicated queue for emails (RabbitMQ, SQS)
 * - Implement retry mechanism for failed emails
 * - Monitor async task execution metrics
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    // Uses Spring Boot's default async configuration
    // Can customize with @Bean TaskExecutor if needed
}
