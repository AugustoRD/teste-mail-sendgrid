#  POC Envio de E-mail 


## 🛠️ Passo 1: Configurar a Senha 

1. Abra a pasta `src/main/resources`.
2. Encontre o arquivo chamado `secrets.example.properties`.
3. Faça uma cópia dele e renomeie a cópia para **`secrets.properties`** .
4. Abra esse novo arquivo e coloque a chave do SendGrid :

```properties
spring.mail.password=SG.COLE_A_CHAVE_AQUI
```

## ▶️ Passo 2: Rodar o Projeto
No terminal do VS Code (PowerShell):
./mvn spring-boot:run
O servidor vai iniciar na porta 8080.


## 🧪 Passo 3: Testar o Envio
Abra um novo terminal e cole este comando para simular um visitante solicitando visita:

Invoke-RestMethod -Uri "http://localhost:8080/email/solicitar-visita" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"nomeVisitante": "Teste da Equipe", "emailVisitante": "seu.email.pessoal@gmail.com", "mensagem": "Testando se o Reply-To funciona!"}'
