# Clínica Médica

Sistema de console em Java para uma clínica pequena: cadastro de pacientes e médicos e agendamento de consultas. Os dados ficam em um banco **PostgreSQL**, acessados por JDBC com o padrão **DAO**.

## O que o sistema faz

- Cadastrar e listar pacientes (CPF único)
- Cadastrar e listar médicos (CRM único)
- Agendar consultas, validando se paciente e médico existem, se a data não é passada e se o médico está livre no horário
- Listar as consultas de um paciente ou de um médico
- Cancelar uma consulta ou marcá-la como realizada

## Tecnologias

- Java 17
- PostgreSQL e JDBC (driver `org.postgresql`)
- Maven
- JUnit 5
- Checkstyle

## Estrutura

```
clinica-medica/
├── src/main/java/clinica/
│   ├── Main.java            # menu do console
│   ├── modelo/              # Paciente, Medico, Consulta, StatusConsulta
│   └── dao/                 # Conexao, PacienteDAO, MedicoDAO, ConsultaDAO, DaoException
├── src/main/resources/schema.sql
├── src/test/java/clinica/dao/   # testes de integração dos DAOs
├── checkstyle.xml
└── pom.xml
```

Cada DAO cuida do SQL de uma tabela com `PreparedStatement`, e os erros de banco viram uma `DaoException`. Como a classe `Consulta` mostra o nome do paciente e do médico, o `ConsultaDAO` busca tudo com `JOIN`.

## Banco de dados

O arquivo `schema.sql` cria três tabelas: `paciente`, `medico` e `consulta`. Algumas regras ficam no próprio banco:

- CPF e CRM são `UNIQUE`.
- O status da consulta só aceita `AGENDADA`, `REALIZADA` ou `CANCELADA`.
- Um índice único parcial impede dois agendamentos do mesmo médico no mesmo horário. Consultas canceladas não entram nessa regra, então o horário volta a ficar livre.

## Como executar

Pré-requisitos: JDK 17+, Maven e PostgreSQL.

1. Crie o banco e as tabelas:

```bash
psql -U postgres -c "CREATE DATABASE clinica"
psql -U postgres -d clinica -f src/main/resources/schema.sql
```

2. Informe os dados de acesso por variáveis de ambiente (nenhuma senha fica no código):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/clinica
export DB_USER=postgres
export DB_PASSWORD=sua_senha
```

Se não definir nada, o sistema usa `localhost:5432/clinica`, usuário `postgres` e senha vazia.

3. Rode:

```bash
mvn compile exec:java
```

## Testes

Os testes de integração usam um banco separado, `clinica_teste`, para nunca apagar dados do banco principal. As tabelas são criadas pelos próprios testes a partir do `schema.sql`.

```bash
psql -U postgres -c "CREATE DATABASE clinica_teste"

export DB_URL_TESTE=jdbc:postgresql://localhost:5432/clinica_teste
export DB_USER_TESTE=postgres
export DB_PASSWORD_TESTE=sua_senha

mvn test
```

Sem um PostgreSQL de teste disponível, os testes de banco são ignorados em vez de falhar.

Os testes verificam, entre outras coisas: geração de id, busca por id/CPF/CRM, CPF e CRM repetidos, ordem das listagens, atualização, remoção, conflito de horário do médico, cancelamento liberando o horário e a proteção contra remover paciente que tem consulta.

## Qualidade de código

```bash
mvn verify
```

Roda os testes e o Checkstyle com as regras de `checkstyle.xml`.

## Autora

**Lilian Emilly** — [@LiyDev](https://github.com/LiyDev)
