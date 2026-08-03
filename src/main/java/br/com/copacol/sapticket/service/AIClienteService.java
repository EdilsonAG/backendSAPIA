package br.com.copacol.sapticket.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.com.copacol.sapticket.web.dto.CriarTicketStatus;

 
@Service
public class AIClienteService {

    private final RestClient restClient;

    public AIClienteService(RestClient.Builder builder,@Value("${ia.server.base-url}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(500)); // IA demora, ajuste ao seu caso

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
record ChatResponse(String answer) {}
record BodyResponse(String body) {}
record ChatRequest(String message, String session) {}

//  @Async("iaTaskExecutor")
//     public String iaPerguntar(String session,String message) {
//        // ChatResponse response = restClient.post()
//         ChatResponse response = restClient.post()
//                 .uri("/chat")
//                 //.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.getToken())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .body(new ChatRequest(message,session))
//                 .retrieve()
//                 .body(ChatResponse.class);

//                 System.out.println(response.answer());
//         return response != null ? response.answer() : "";
//     }

   record ResponseIA(String resposta, CriarTicketStatus status, String description, String longText, String priority, String type) {}

    public ResponseIA iaPerguntar(String session,String message) {
       // ChatResponse response = restClient.post()
        ResponseIA response = restClient.post()
                .uri("/chat")
                //.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChatRequest(message,session))
                .retrieve()
                .body(ResponseIA.class);

                System.out.println(response.resposta());
        return response != null ? response : null;
    }
}