# sap-ticket-backend (Spring Boot 4.1 / Java 25)

## Rodar

1. Subir Redis:
   ```
   docker run -p 6379:6379 redis:7
   ```
2. Rodar a aplicação:
   ```
   mvn spring-boot:run
   ```

API sobe em `http://localhost:3001`, mesmas rotas do backend original:
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/tickets`

## O que mudou em relação ao Express/TS

- `sessionStore.ts` (Map em memória) → `HttpSession` gerenciado pelo Spring Session, persistido no Redis (`spring-session-data-redis`). Isso já resolve o problema do Express: sessão em memória não escala horizontalmente (2 instâncias = sessão perdida). Com Redis, qualquer instância acessa a mesma sessão.
- Cookie de sessão (`res.cookie(...)` manual) → configurado via `server.servlet.session.cookie.*` no `application.properties`. O Spring Session cuida de gerar/enviar/expirar o cookie.
- TTL da sessão: expirava manualmente checando `expiresAt` a cada request → agora é TTL nativo do Redis (`spring.session.timeout`), expira sozinho.
- `node-fetch` → `RestClient` (cliente HTTP síncrono nativo do Spring, desde 6.1).
- **Bug corrigido**: em `sapClient.ts`, as chamadas ao SAP ignoravam o `authHeader` recebido por parâmetro e usavam um `Authorization: Basic U0RLMDIxOkNvcGFjb2wyNkA=` fixo hardcoded no código — ou seja, todo usuário virava a mesma conta de serviço no SAP, independente do login. Na versão Java, o `authHeader` da sessão do usuário é o que efetivamente vai pro SAP. Se a intenção era mesmo usar uma conta de serviço fixa, isso deve ser decisão explícita e a credencial precisa sair do código-fonte (variável de ambiente / vault), nunca hardcoded.
