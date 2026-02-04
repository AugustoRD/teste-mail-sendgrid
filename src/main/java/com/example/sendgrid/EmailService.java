package com.example.sendgrid;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {

    @Value("${spring.mail.password}")
    private String sendGridApiKey;

    public void sendVisitRequest(EmailDTO dadosFormulario) {

        Email from = new Email("emailOrigem@gmail.com", "Sistema de Visitas");
        Email to = new Email("emailDestino@gmail.com");

        String subject = "Nova Solicitação de visita: " + dadosFormulario.nomeVisitante();

        // O Conteúdo do E-mail (Usando HTML)
        String htmlContent = String.format(
            "<strong>Nova visita recebida!</strong><br><br>" +
            "Nome: %s<br>" +
            "E-mail: %s<br>" +
            "Mensagem: <br><i>%s</i>",
            dadosFormulario.nomeVisitante(), dadosFormulario.emailVisitante(), dadosFormulario.mensagem()
        );

        Content content = new Content("text/html", htmlContent);

        //Monta o pacote do e-mail
        Mail mail = new Mail(from, subject, to, content);
        
        // Define que se você clicar em "Responder", vai para o visitante
        mail.setReplyTo(new Email(dadosFormulario.emailVisitante()));

        //Envia para a Nuvem do SendGrid (Via API HTTP)
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            // Dispara a requisição
            Response response = sg.api(request);

            // Verifica se deu certo 
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ E-mail enviado com sucesso via API!");
            } else {
                System.err.println("❌ Erro no SendGrid: " + response.getBody());
            }

        } catch (IOException ex) {
            throw new RuntimeException("Falha ao conectar na API do SendGrid", ex);
        }
    }
}