package ru.checkdev.notification.telegram.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;

@Service
@Slf4j
public class TgAuthCallWebClient {
    @Setter
    private WebClient webClient;

    public TgAuthCallWebClient(@Value("${server.auth}") String urlAuth) {
        this.webClient = WebClient.create(urlAuth);
    }

    /**
     * Метод GET с применением Retry и Circuit Breaker
     *
     * @param url URL http
     * @return Mono<Person>
     */
    @Retry(name = "tgAuthRetry") // Применение Retry
    @CircuitBreaker(name = "tgAuthCircuitBreaker", fallbackMethod = "fallbackGet") // Применение Circuit Breaker
    public Mono<PersonDTO> doGet(String url) {
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(PersonDTO.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    /**
     * Метод POST с применением Retry и Circuit Breaker
     *
     * @param url       URL http
     * @param personDTO Body PersonDTO.class
     * @return Mono<Object>
     */
    @Retry(name = "tgAuthRetry") // Применение Retry
    @CircuitBreaker(name = "tgAuthCircuitBreaker", fallbackMethod = "fallbackPost") // Применение Circuit Breaker
    public Mono<Object> doPost(String url, PersonDTO personDTO) {
        return webClient
                .post()
                .uri(url)
                .bodyValue(personDTO)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    // Fallback метод для GET
    public Mono<PersonDTO> fallbackGet(String url, Throwable throwable) {
        log.error("GET request failed, fallback triggered: {}", throwable.getMessage());
        return Mono.empty(); // Или возвращайте какой-то запасной ответ
    }

    // Fallback метод для POST
    public Mono<Object> fallbackPost(String url, PersonDTO personDTO, Throwable throwable) {
        log.error("POST request failed, fallback triggered: {}", throwable.getMessage());
        return Mono.empty(); // Или возвращайте какой-то запасной ответ
    }
}
