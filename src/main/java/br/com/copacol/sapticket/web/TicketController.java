package br.com.copacol.sapticket.web;

import br.com.copacol.sapticket.service.SapClientService;
import br.com.copacol.sapticket.web.dto.TicketRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final SapClientService sapClientService;

    public TicketController(SapClientService sapClientService) {
        this.sapClientService = sapClientService;
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody TicketRequest request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String csrfToken = session != null ? (String) session.getAttribute("csrfToken") : null;

        if (session == null || csrfToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Sessão expirada, faça login novamente"));
        }

        String authHeader = (String) session.getAttribute("passwordBase64");
        String cookies = (String) session.getAttribute("cookies");

        try {
            Map<?, ?> result = sapClientService.createTicket(authHeader, csrfToken, cookies, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro ao criar chamado"));
        }
    }
}
