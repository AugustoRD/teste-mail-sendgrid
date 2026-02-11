package com.example.sendgrid;

public record EmailDTO(
    String nomeResponsavel, 
    String emailContato,    
    String instituicao,     
    String dataVisita,      
    String horarioVisita,   
    String tipoGrupo,       //(Adultos/Crianças)
    Integer qtdPessoas,     
    String mensagem         // Justificativa ou observação
) {}
