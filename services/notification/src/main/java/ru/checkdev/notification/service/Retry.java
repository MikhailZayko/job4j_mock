package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class Retry {
    private final int retries;
    private final long delay;

    public interface Act<T> {
        T exec() throws Exception;
    }

    public <R> R exec(Act<R> act, R defVal) {
        int i = 0;
        do {
            i++;
            try {
                return act.exec();
            } catch (Exception e) {
                log.error("Attempt {} failed: {}", i, e.getMessage(), e);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted during sleep", ie);
                }
            }
        } while (i < retries);
        return defVal;
    }

    public static void main(String[] args) {
        Act<String> permitExc = () -> {
            throw new Exception("Error");
        };
        var retry = new Retry(3, 1000); // 1 секунда задержки
        var result = retry.exec(permitExc, "DefVal");
        System.out.println(result);
    }
}
