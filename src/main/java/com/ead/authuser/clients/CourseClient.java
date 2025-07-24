package com.ead.authuser.clients;

import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.ResponsePageDto;
import com.ead.authuser.services.UtilsService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class CourseClient {

    private final RestTemplate restTemplate;
    private final UtilsService utilsService;

    @Value("${ead.api.url.course}")
    String REQUEST_URL_COURSE;

//    Utilizado para retentativas, não está sendo utilizado pois estamos utilizando o circuit breaker
//    @Retry(name = "retryInstance", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "circuitBreakerInstance")
    public Page<CourseDto> getAllCoursesByUser(UUID userId, Pageable pageable, String token) {
        List<CourseDto> searchResult = null;
        ResponseEntity<ResponsePageDto<CourseDto>> result = null;

        String url = REQUEST_URL_COURSE + utilsService.createUrlGetAllCoursesByUser(userId, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        log.debug("Request URL: {}", url);
        log.info("Request URL: {}", url);


        ParameterizedTypeReference<ResponsePageDto<CourseDto>> responseType = new ParameterizedTypeReference<>() {};
        result = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                responseType
        );
        searchResult = result.getBody().getContent();
        log.debug("Response number of elements: {}", searchResult.size());

        log.info("Ending request /courses userId {}", userId);
        return result.getBody();
    }

    public Page<CourseDto> retryFallback(UUID userId, Pageable pageable, Throwable throwable) {
        log.error("Error request /courses userId {}", userId, throwable);
        List<CourseDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }
}
