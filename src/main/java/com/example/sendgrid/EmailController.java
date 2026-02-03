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
        emailService.sendVisitRequest(formulario);
        return "Sua solicitação foi recebida! Verifique se o e-mail chegou.";
    }
}