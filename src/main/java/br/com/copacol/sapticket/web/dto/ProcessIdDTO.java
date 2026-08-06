package br.com.copacol.sapticket.web.dto;

public class ProcessIdDTO {
    private String processId;
    private String resposta;
    private String sessionId;
    private CriarTicketStatus status;
    public CriarTicketStatus getStatus() {
        return status;
    }
    public void setStatus(CriarTicketStatus status) {
        this.status = status;
    }
    private String pergunta;
    public String getPergunta() {
        return pergunta;
    }
    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }
    public String getProcessId() {
        return processId;
    }
    public void setProcessId(String processId) {
        this.processId = processId;
    }
    public String getResposta() {
        return resposta;
    }
    public void setResposta(String resposta) {
        this.resposta = resposta;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
 
}
