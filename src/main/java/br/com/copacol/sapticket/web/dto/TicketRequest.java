package br.com.copacol.sapticket.web.dto;

public record TicketRequest(
        String description,
        String longText,
        String priority,
        TicketType type
) {
    public enum TicketType {
        incident,
        requirement
    }
}
