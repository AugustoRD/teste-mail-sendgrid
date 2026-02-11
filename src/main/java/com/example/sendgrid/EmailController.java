package com.example.sendgrid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/solicitar-visita")
    public String solicitarVisita(@RequestBody EmailDTO formulario) {
        System.out.println("📩 Recebendo solicitação de: " + formulario.instituicao());
        
        emailService.sendVisitRequest(formulario);
        
        return "Solicitação enviada com sucesso! Verifique seu e-mail.";
    }
}