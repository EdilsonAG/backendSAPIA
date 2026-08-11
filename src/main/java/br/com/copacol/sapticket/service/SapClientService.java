package br.com.copacol.sapticket.service;

import br.com.copacol.sapticket.service.AIClienteService.ResponseIA;
import br.com.copacol.sapticket.service.redis.RedisStatus;
import br.com.copacol.sapticket.web.dto.CriarTicketStatus;
import br.com.copacol.sapticket.web.dto.Incident;
import br.com.copacol.sapticket.web.dto.ProcessIdDTO;
import br.com.copacol.sapticket.web.dto.RedisContextDTO;
import br.com.copacol.sapticket.web.dto.SapIncidentResponse;
import br.com.copacol.sapticket.web.dto.Status;
import br.com.copacol.sapticket.web.dto.TicketRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SapClientService {

    private final RestClient restClient;
    private final String sapBaseUrl;
    private final String sapClient;
    private final AIClienteService aiClienteService;
    private final RedisStatus redisStatus;

    @Autowired
    private SessionRepository<? extends Session> sessionRepository;

    public SapClientService(
            @Value("${sap.base-url}") String sapBaseUrl,
            @Value("${sap.client}") String sapClient, AIClienteService aiClienteService, RedisStatus redisStatus) {
        this.aiClienteService = aiClienteService;
        this.sapBaseUrl = sapBaseUrl;
        this.sapClient = sapClient;
        this.redisStatus = redisStatus;
        this.restClient = RestClient.create();
    }

    public String buildBasicAuth(String username, String password) {
        String raw = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public CsrfResult fetchCsrfToken(String authHeader) {
        String url = sapBaseUrl + "/AI_CRM_GW_CREATE_INCIDENT_SRV/?sap-client=" + sapClient;

        System.out.println(url);
        System.out.println(authHeader);
        try {
            ResponseEntity<Void> response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Basic "+ authHeader)
                    .header("X-CSRF-Token", "Fetch")
                    .retrieve()
                    .toBodilessEntity();
                    System.out.println("teste");

            String csrfToken = response.getHeaders().getFirst("X-CSRF-Token");
            String cookies = String.join("; ", response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE));
            return new CsrfResult(csrfToken != null ? csrfToken : "", cookies);
        } catch (RestClientResponseException e) {
            throw new SapClientException("Falha ao autenticar no SAP (status " + e.getStatusCode().value() + ")", e);
        }
    }

    public Map<?, ?> createTicketcorreto(String authHeader, String csrfToken, String cookies, TicketRequest input,
            String D, HttpServletRequest httpRequest, String longText) {
        boolean isIncident = input.type() == TicketRequest.TicketType.incident;

        Session se = sessionRepository.findById(D);

        // HttpSession session2 = httpRequest.getSession(false);
        String pass = (String) se.getAttribute("passwordBase64");
        String cookiess = (String) se.getAttribute("cookies");
        String csrfTokens = (String) se.getAttribute("csrfToken");

        String url = isIncident
                ? sapBaseUrl + "/AI_CRM_GW_CREATE_INCIDENT_SRV/IncidentSet?sap-client=" +
                        sapClient
                : sapBaseUrl +
                        "/AI_CRM_GW_MYBUSI_REQUIRE_SRV/BusinessRequirementSet?sap-client=" +
                        sapClient;

        System.out.println("antes do body");
        Map<String, Object> body = isIncident
                ? Map.of(
                        "ProcessType", "ZMIN",
                        "Description", "FALHA",
                        "LongText", longText,
                        "Priority", input.priority(),
                        "PartnerFct3", "SLFN0003")

                : Map.of(
                        "Description", "FALHA",
                        "Priority", input.priority());

        System.out.println("depois do map body ja");
        try {

            SapIncidentResponse resposta = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + pass)
                    .header("X-CSRF-Token", csrfTokens)
                    .header(HttpHeaders.COOKIE, cookiess)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .body(body)
                    .retrieve()
                    .body(SapIncidentResponse.class);

            System.out.println("RESPOSTA SAP: " + resposta);

            Incident incident = resposta.getD();
            System.out.println(incident.getGuid());
            System.out.println(incident.getObjectId());
            System.out.println(incident.getLongText());
            return Map.of("raw", resposta);
        } catch (RestClientResponseException e) {
            e.printStackTrace();
            throw new SapClientException(
                    "Erro do SAP (status " + e.getStatusCode().value() + "): " +
                            e.getResponseBodyAsString(),
                    e);
        }
    }

    // public Map<?, ?> createTicket(String authHeader, String csrfToken, String
    // cookies, TicketRequest input,
    // String session) {
    // boolean isIncident = input.type() == TicketRequest.TicketType.incident;

    // aiClienteService.iaPerguntar(session, input.description());

    // return Map.of("teste", isIncident);
    // }

    public List<String> findProcessById(String session) {

        return redisStatus.buscarProcessIdsDaSessao(session);
    }

    public ProcessIdDTO findConversation(String processId) {
        return redisStatus.findConversationByProcessId(processId);
    }

    @Async("iaTaskExecutor")
    public void processarMensagem(String session, TicketRequest input, String processId,
            HttpServletRequest httpRequest) {
        try {
            // ResponseIA resposta = new ResponseIA(processId, CriarTicketStatus.PRONTO,
            // processId, "teste descritivo", session, processId);

            ResponseIA resposta = aiClienteService.iaPerguntar(session, input.description());

            RedisContextDTO concluido = new RedisContextDTO();
            concluido.setProcessId(processId);
            concluido.setPergunta(input.description());
            concluido.setResposta(resposta.resposta());
            concluido.setSessionId(session);
            concluido.setStatus(resposta.status());
            System.out.println("antes do if");
            if (resposta.status() == CriarTicketStatus.PRONTO) {
                System.out.println("VEIO PRA CRIAR TICKET");
                System.out.println("dentro do if");
                this.createTicketcorreto(processId, processId, processId, input, session, httpRequest,
                        resposta.longText());
                System.out.println("chegou pra criar o ticket");
                System.out.println("description");
                System.out.println(resposta.description());
                System.out.println("longtext");
                System.out.println(resposta.longText());

            }

            redisStatus.salvar(processId, concluido);

        } catch (Exception e) {
            RedisContextDTO erro = new RedisContextDTO();
            erro.setProcessId(processId);
            erro.setSessionId(session);
            erro.setStatus(CriarTicketStatus.CANCELADO);
            erro.setResposta(e.getMessage());
            redisStatus.salvar(processId, erro);
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
