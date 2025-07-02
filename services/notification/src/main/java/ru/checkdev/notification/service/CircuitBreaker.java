package ru.checkdev.notification.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CircuitBreaker {
    private final int failureThreshold;
    private int failureCount = 0;
    private State state = State.CLOSED;

    public CircuitBreaker(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    private enum State {
        OPEN,
        CLOSED,
    }

    public interface Act<T> {
        T exec() throws Exception;
    }

    public <R> R exec(Act<R> act, R defVal) {
        if (state == State.CLOSED) {
            try {
                return act.exec();
            } catch (Exception e) {
                failureCount++;
                log.error("Attempt failed, failure count: {}", failureCount);
                if (failureCount >= failureThreshold) {
                    state = State.OPEN;
                    log.warn("Circuit Breaker OPENED due to failure threshold exceeded.");
                }
                return defVal;
            }
        }
        log.warn("Circuit Breaker is OPEN. Skipping request.");
        throw new CircuitBreakerOpenException("Circuit Breaker is OPEN. Request skipped.");
    }

    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        var cb = new CircuitBreaker(2);
        Act<String> failAct = () -> {
            throw new Exception("Service failure");
        };
        var retry = new Retry(3, 1);
        String result = retry.exec(
                () -> cb.exec(failAct, "BS DevVal"), "Retry Dev Val"
        );
        System.out.println(result);
    }
}

