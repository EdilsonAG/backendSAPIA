package br.com.copacol.sapticket.service;

import br.com.copacol.sapticket.web.dto.TicketRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SapClientService {

    private final RestClient restClient;
    private final String sapBaseUrl;
    private final String sapClient;
    private final AIClienteService aiClienteService;

    public SapClientService(
            @Value("${sap.base-url}") String sapBaseUrl,
            @Value("${sap.client}") String sapClient, AIClienteService aiClienteService) {
        this.aiClienteService = aiClienteService;
        this.sapBaseUrl = sapBaseUrl;
        this.sapClient = sapClient;
        this.restClient = RestClient.create();
    }

    public String buildBasicAuth(String username, String password) {
        String raw = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public CsrfResult fetchCsrfToken(String authHeader) {
        String url = sapBaseUrl + "/AI_CRM_GW_CREATE_INCIDENT_SRV/?sap-client=" + sapClient;

        try {
            ResponseEntity<Void> response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Basic U0RLMDIxOkNvcGFjb2wyNkA=")
                    .header("X-CSRF-Token", "Fetch")
                    .retrieve()
                    .toBodilessEntity();

            String csrfToken = response.getHeaders().getFirst("X-CSRF-Token");
            String cookies = String.join("; ", response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE));
            return new CsrfResult(csrfToken != null ? csrfToken : "", cookies);
        } catch (RestClientResponseException e) {
            throw new SapClientException("Falha ao autenticar no SAP (status " + e.getStatusCode().value() + ")", e);
        }
    }

    public Map<?, ?> createTicket(String authHeader, String csrfToken, String cookies, TicketRequest input) {
        boolean isIncident = input.type() == TicketRequest.TicketType.incident;

        String ia = aiClienteService.iaPerguntar(input.description());

        if (true) {
            return Map.of("resposta",ia);
        }

        String url = isIncident
                ? sapBaseUrl + "/AI_CRM_GW_CREATE_INCIDENT_SRV/IncidentSet?sap-client=" + sapClient
                : sapBaseUrl + "/AI_CRM_GW_MYBUSI_REQUIRE_SRV/BusinessRequirementSet?sap-client=" + sapClient;

                System.out.println("antes do body");
        Map<String, Object> body = isIncident
                ? Map.of(
                        "ProcessType", "ZMIN",
                        "Description", input.description(),
                        "LongText", input.longText(),
                        "Priority", input.priority())
                : Map.of(
                        "Description", input.description(),
                        "Priority", input.priority());

                        System.out.println("depois do map body ja");
        try {
            
            String teste = restClient.post()
        .uri(url)
        .header(HttpHeaders.AUTHORIZATION, authHeader)
        .header("X-CSRF-Token", csrfToken)
        .header(HttpHeaders.COOKIE, cookies)
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .retrieve()
        .body(String.class);

System.out.println("RESPOSTA SAP: " + teste);

return Map.of("raw", teste);
        } catch (RestClientResponseException e) {
            e.printStackTrace();
            throw new SapClientException(
                    "Erro do SAP (status " + e.getStatusCode().value() + "): " + e.getResponseBodyAsString(), e);
        }
    }

    public record CsrfResult(String csrfToken, String cookies) {
    }

    public static class SapClientException extends RuntimeException {
        public SapClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
