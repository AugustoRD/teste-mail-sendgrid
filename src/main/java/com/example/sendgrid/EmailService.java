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

    // QUEM ENVIA: Use o Email verificado no SendGrid 
    private final String EMAIL_REMETENTE_OFICIAL = "remetente@email.com";

    // QUEM RECEBE: Sua caixa de entrada 
    private final String EMAIL_DESTINO_ADMIN = "destino@email.com";

    // Caminhos dos templates
    private final String TEMPLATE_ADMIN = "templates/email-visita.html";
    private final String TEMPLATE_CLIENTE = "templates/email-confirmacao.html";

    // --- MÉTODO PRINCIPAL ---
    public void sendVisitRequest(EmailDTO dados) {
        try {
            // Envia para a Central com todos os detalhes
            enviarParaAdmin(dados);

            // Envia confirmação para o Solicitante
            enviarParaVisitante(dados);

            System.out.println("✅ Ciclo de e-mails concluído com sucesso!");

        } catch (IOException ex) {
            System.err.println("Erro crítico ao enviar e-mails: " + ex.getMessage());
            throw new RuntimeException("Falha no envio de e-mails", ex);
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void enviarParaAdmin(EmailDTO dados) throws IOException {
        String html = readTemplate(TEMPLATE_ADMIN);
        
        // Substitui os novos campos no HTML
        html = html.replace("{{nome}}", dados.nomeResponsavel())
                   .replace("{{email}}", dados.emailContato())
                   .replace("{{instituicao}}", dados.instituicao())
                   .replace("{{data}}", dados.dataVisita())
                   .replace("{{horario}}", dados.horarioVisita())
                   .replace("{{tipoGrupo}}", dados.tipoGrupo())
                   .replace("{{qtd}}", String.valueOf(dados.qtdPessoas()))
                   .replace("{{mensagem}}", dados.mensagem() != null ? dados.mensagem() : "Sem observações");

        // ENVIO: De Sistema -> Para Admin
        // Reply-To: Vai para o e-mail do Solicitante
        sendEmailFinal(
            EMAIL_DESTINO_ADMIN, 
            "🌱 Nova Solicitação: " + dados.instituicao(), 
            html, 
            dados.emailContato() 
        );
    }

    private void enviarParaVisitante(EmailDTO dados) throws IOException {
        String html = readTemplate(TEMPLATE_CLIENTE);
        
        // Confirma apenas o básico para o cliente
        html = html.replace("{{nome}}", dados.nomeResponsavel())
                   .replace("{{data}}", dados.dataVisita());

        // ENVIO: De Sistema -> Para Solicitante 
        sendEmailFinal(
            dados.emailContato(), 
            "Recebemos sua solicitação! 🌱", 
            html, 
            EMAIL_DESTINO_ADMIN 
        );
    }

    // --- SendGrid ---
    private void sendEmailFinal(String destinatario, String assunto, String htmlContent, String replyToEmail) throws IOException {
        // O "From" é sempre o email verificado para garantir a entrega
        Email from = new Email(EMAIL_REMETENTE_OFICIAL, "Central de Resíduos PUCRS");
        Email to = new Email(destinatario);
        Content content = new Content("text/html", htmlContent);

        Mail mail = new Mail(from, assunto, to, content);
        
        // Configura o Reply-To dinamicamente
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