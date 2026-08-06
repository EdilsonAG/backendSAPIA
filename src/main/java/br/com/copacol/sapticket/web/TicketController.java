package br.com.copacol.sapticket.web;

import br.com.copacol.sapticket.service.SapClientService;
import br.com.copacol.sapticket.service.redis.RedisStatus;
import br.com.copacol.sapticket.web.dto.CriarTicketStatus;
import br.com.copacol.sapticket.web.dto.ProcessIdDTO;
import br.com.copacol.sapticket.web.dto.RedisContextDTO;
import br.com.copacol.sapticket.web.dto.Status;
import br.com.copacol.sapticket.web.dto.TicketRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final SapClientService sapClientService;
    private final RedisStatus redisStatus;

    public TicketController(SapClientService sapClientService, RedisStatus redisStatus) {
        this.sapClientService = sapClientService;
        this.redisStatus = redisStatus;
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody TicketRequest request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String csrfToken = session != null ? (String) session.getAttribute("csrfToken") : null;

        if (session == null || csrfToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Sessão expirada, faça login novamente"));
        }

        // String contextSessionId = (String) session.getAttribute("contextSessionId");
        // String authHeader = (String) session.getAttribute("passwordBase64");
        // String cookies = (String) session.getAttribute("cookies");

        try {
           // Map<?, ?> result = sapClientService.createTicket(authHeader, csrfToken, cookies, request,session.getId());
           String processId = UUID.randomUUID().toString();
           RedisContextDTO redisContextDTO = new RedisContextDTO();
           redisContextDTO.setProcessId(processId);
           redisContextDTO.setSessionId(session.getId());
           redisContextDTO.setStatus(CriarTicketStatus.COLETANDO);
           redisContextDTO.setPergunta(request.description());
           redisStatus.adicionarProcessIdNaSessao(session.getId(), processId);
           redisStatus.salvar(processId, redisContextDTO);
           sapClientService.processarMensagem(session.getId(), request,processId,httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("processId", processId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro ao criar chamado"));
        }
    }

    @GetMapping("/{processId}")
    public ProcessIdDTO getConversationByProcessid(@PathVariable String processId){
        return sapClientService.findConversation(processId);
    }

    @GetMapping
    public List<String> getMessage(HttpServletRequest httpRequest){
      HttpSession session = httpRequest.getSession(false);
        return sapClientService.findProcessById(session.getId());
    }
}