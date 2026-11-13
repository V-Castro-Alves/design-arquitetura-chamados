# Chamados

Aplicação web para gerenciar chamados de suporte, clientes, categorias, técnicos,
status e comentários. O projeto utiliza Spring Boot, Spring MVC, Thymeleaf,
Spring Data JPA e um banco de dados H2 em memória.

## Pré-requisitos

- JDK 21
- Conexão com a internet na primeira execução para que o Maven baixe as dependências

Não é necessário instalar o Maven nem um servidor de banco de dados. O repositório
inclui o Maven Wrapper, e a aplicação inicia automaticamente um banco H2 em memória.

Confirme se o Java 21 está ativo:

```bash
java -version
```

## Executar o projeto

Na raiz do projeto, execute:

### Linux ou macOS

```bash
./mvnw spring-boot:run
```

### Windows (Prompt de Comando ou PowerShell)

```powershell
.\mvnw.cmd spring-boot:run
```

Quando o log informar que a aplicação foi iniciada, acesse:

- Interface web: <http://localhost:8080/app>
- Console do banco H2: <http://localhost:8080/h2-console>

Utilize estes valores para se conectar pelo console do H2:

| Campo | Valor |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:chamadosdb` |
| Usuário | `sa` |
| Senha | deixe em branco |

Encerre a aplicação com `Ctrl+C`.

## Executar os testes

Linux ou macOS:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

## Gerar e executar o JAR

Linux ou macOS:

```bash
./mvnw clean package
java -jar target/chamados-0.0.1-SNAPSHOT.jar
```

Windows:

```powershell
.\mvnw.cmd clean package
java -jar target\chamados-0.0.1-SNAPSHOT.jar
```

## Utilizar outra porta

A porta `8080` é utilizada por padrão. Caso ela já esteja ocupada, inicie a
aplicação em outra porta, por exemplo, `8081`:

Linux ou macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Depois, acesse a aplicação em <http://localhost:8081/app>.
