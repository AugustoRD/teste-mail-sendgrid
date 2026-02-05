package com.example.sendgrid;


import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Value("${spring.mail.password}")
    private String sendGridApiKey;

    // Defina seu e-mail principal aqui 
    // Esse e-mail será o remetente oficial e quem recebe os avisos
    private final String EMAIL_CENTRAL = "emailCadastrado@email.com";

    // Caminhos dos templates na pasta resources
    private final String TEMPLATE_ADMIN = "templates/email-visita.html";
    private final String TEMPLATE_CLIENTE = "templates/email-confirmacao.html";

    // --- MÉTODO PRINCIPAL ---
    public void sendVisitRequest(EmailDTO dados) {
        try {
            //Envia o aviso para Admin
            enviarParaAdmin(dados);

            //Envia a confirmação para o visitante 
            enviarParaVisitante(dados);

            System.out.println("✅ Ciclo de e-mails concluído com sucesso!");

        } catch (IOException ex) {
            System.err.println("Erro crítico ao enviar e-mails: " + ex.getMessage());
            throw new RuntimeException("Falha no envio de e-mails", ex);
        }
    }

    // --- MÉTODOS AUXILIARES  ---

    private void enviarParaAdmin(EmailDTO dados) throws IOException {
        // Prepara o HTML do Admin
        String html = readTemplate(TEMPLATE_ADMIN);
        html = html.replace("{{nome}}", dados.nomeVisitante())
                   .replace("{{email}}", dados.emailVisitante())
                   .replace("{{mensagem}}", dados.mensagem());

        // De: Central -> Para: Central
        // Se a Central clicar em "Responder", vai para o e-mail do VISITANTE
        sendEmailFinal(
            EMAIL_CENTRAL, 
            "🌱 Nova Solicitação: " + dados.nomeVisitante(), 
            html, 
            dados.emailVisitante() 
        );
    }

    private void enviarParaVisitante(EmailDTO dados) throws IOException {
        // Prepara o HTML do Cliente
        String html = readTemplate(TEMPLATE_CLIENTE);
        // O template de confirmação geralmente só usa o nome, mas por garantia substituímos tudo
        html = html.replace("{{nome}}", dados.nomeVisitante());

        // De: Central -> Para: Visitante
        // Se o Visitante clicar em "Responder", vai para o e-mail da CENTRAL
        sendEmailFinal(
            dados.emailVisitante(), 
            "Recebemos sua solicitação! 🌱", 
            html, 
            EMAIL_CENTRAL 
        );
    }

    // --- MÉTODO GENÉRICO (Conexão com SendGrid) ---
    // Esse método faz o trabalho de conectar na API, servindo para qualquer envio.
    private void sendEmailFinal(String destinatario, String assunto, String htmlContent, String replyToEmail) throws IOException {
        Email from = new Email(EMAIL_CENTRAL, "Sistema de Visitas");
        Email to = new Email(destinatario);
        Content content = new Content("text/html", htmlContent);

        Mail mail = new Mail(from, assunto, to, content);
        
        // Configura o Reply-To (quem recebe a resposta)
        if (replyToEmail != null && !replyToEmail.isEmpty()) {
            mail.setReplyTo(new Email(replyToEmail));
        }

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        Response response = sg.api(request);
        
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            System.out.println("📤 E-mail enviado para: " + destinatario);
        } else {
            System.err.println("❌ Erro SendGrid para " + destinatario + ": " + response.getBody());
        }
    }

    // Leitura do arquivo 
    private String readTemplate(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}