package br.com.copacol.sapticket.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

 
@Service
public class AIClienteService {

    private final RestClient restClient;

    public AIClienteService(RestClient.Builder builder,@Value("${ia.server.base-url}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30)); // IA demora, ajuste ao seu caso

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
record ChatResponse(String answer) {}
record ChatRequest(String description) {}

    public String iaPerguntar(String description) {
        ChatResponse response = restClient.post()
                .uri("/chat")
                //.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChatRequest(description))
                .retrieve()
                .body(ChatResponse.class);

                System.out.println(response.answer());
        return response != null ? response.answer() : "";
    }
}