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

    // Mantemos a segurança: A chave vem do arquivo secrets.properties
    @Value("${spring.mail.password}")
    private String sendGridApiKey;

    // Caminho do template que criamos
    private final String TEMPLATE_PATH = "templates/email-visita.html";

    public void sendVisitRequest(EmailDTO dadosFormulario) {
        try {
            // 1. Quem envia (SEU e-mail verificado no SendGrid)
            Email from = new Email("emailOrigem@gmail.com", "Sistema de Visitas");
            
            // 2. Quem recebe (Você mesmo / A Central)
            Email to = new Email("emailDestino@gmail.com"); 

            // 3. Lê o HTML e substitui os dados
            String htmlTemplate = readTemplate(TEMPLATE_PATH);
            String htmlPronto = replacePlaceholders(htmlTemplate, dadosFormulario);

            // 4. Monta o e-mail
            Content content = new Content("text/html", htmlPronto);
            String assunto = "🌱 Nova Solicitação: " + dadosFormulario.nomeVisitante();
            
            Mail mail = new Mail(from, assunto, to, content);
            
            // Configura para que o botão "Responder" vá para o visitante
            mail.setReplyTo(new Email(dadosFormulario.emailVisitante()));

            // 5. Envia via API
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ E-mail com Template Local enviado com sucesso!");
            } else {
                System.err.println("❌ Erro SendGrid: " + response.getBody());
            }

        } catch (IOException ex) {
            throw new RuntimeException("Erro ao processar template ou enviar e-mail", ex);
        }
    }

    // Função Auxiliar 1: Lê o arquivo da pasta resources
    private String readTemplate(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        // Lê os bytes e transforma em String UTF-8 (para aceitar acentos)
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Função Auxiliar 2: Troca os {{ }} pelos dados reais
    private String replacePlaceholders(String template, EmailDTO dados) {
        return template
                .replace("{{nome}}", dados.nomeVisitante())
                .replace("{{email}}", dados.emailVisitante())
                .replace("{{mensagem}}", dados.mensagem());
    }
}