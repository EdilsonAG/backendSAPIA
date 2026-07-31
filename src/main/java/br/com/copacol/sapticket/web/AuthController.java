package br.com.copacol.sapticket.web;

import br.com.copacol.sapticket.service.SapClientService;
import br.com.copacol.sapticket.web.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final SapClientService sapClientService;

    public AuthController(SapClientService sapClientService) {
        this.sapClientService = sapClientService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        if (isBlank(request.username()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuário e senha são obrigatórios"));
        }

        try {
            String usernameUpperCase = request.username().toUpperCase();
            String textoBase64 = usernameUpperCase + ":" + request.password();
            
            String base64 = Base64.getEncoder().encodeToString(textoBase64.toUpperCase().getBytes(StandardCharsets.UTF_8));
             System.out.println(base64);
            SapClientService.CsrfResult csrf = sapClientService.fetchCsrfToken(base64);

            // getSession(true) cria a sessao; o Spring Session grava no Redis
            // e o cookie e emitido automaticamente conforme server.servlet.session.cookie.*
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("username", usernameUpperCase);
            session.setAttribute("passwordBase64", textoBase64);
            session.setAttribute("csrfToken", csrf.csrfToken());
            session.setAttribute("cookies", csrf.cookies());

            return ResponseEntity.ok(Map.of("username", usernameUpperCase));
        } catch (Exception e) {
            log.warn("Falha no login SAP para usuario {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuário ou senha inválidos"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
