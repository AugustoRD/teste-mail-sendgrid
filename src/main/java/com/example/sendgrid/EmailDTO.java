package com.example.sendgrid;

// O DTO representa o formulário que o visitante preencheu
public record EmailDTO(String nomeVisitante, String emailVisitante, String mensagem) {
}
