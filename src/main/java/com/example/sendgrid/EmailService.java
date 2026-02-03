package com.example.sendgrid;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVisitRequest(EmailDTO dadosFormulario) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            //QUEM ENVIA (Obrigatório ser o verificado no SendGrid)
            message.setFrom("seu-email-verificado@gmail.com"); 
            
            //QUEM RECEBE 
            message.setTo("seu-email-destino@gmail.com"); 
            
            // Reply-To:
            message.setReplyTo(dadosFormulario.emailVisitante());

            //Assunto Padronizado
            message.setSubject("Nova Solicitação de Visita: " + dadosFormulario.nomeVisitante());

            //Corpo do e-mail formatado
            String corpoEmail = """
                    Olá, Central de Sustentabilidade!
                    
                    Recebemos uma nova solicitação de visita pelo site.
                    
                    --- DADOS DO VISITANTE ---
                    Nome: %s
                    E-mail para contato: %s
                    
                    --- MENSAGEM ---
                    %s
                    """.formatted(
                            dadosFormulario.nomeVisitante(), 
                            dadosFormulario.emailVisitante(), 
                            dadosFormulario.mensagem()
                    );

            message.setText(corpoEmail);

            mailSender.send(message);
            System.out.println(" Solicitação de visita enviada com sucesso!");
            
        } catch (Exception e) {
            System.out.println(" Erro ao enviar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}