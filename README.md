# Sistema de Informação Hospitalar

Trabalho Prático de Programação Modular — Engenharia de Software, PUC Minas.

Sistema para gerenciar pacientes, profissionais da saúde, consultas, internações e quartos de um hospital de médio porte.

## Etapa atual

| Entrega | Local |
|---|---|
| Diagrama de classes | [`docs/diagrama-de-classes.md`](docs/diagrama-de-classes.md) |
| Cartões CRC | [`docs/cartoes-crc.docx`](docs/cartoes-crc.docx) |
| Front-end (telas sem funcionalidade) | [`frontend/`](frontend/index.html) |
| API REST em Spring Boot | [`sistema-hospitalar/`](sistema-hospitalar/pom.xml) |

## Arquitetura

Camadas Controller → Service → Repository → Model, com API REST em Spring Boot e persistência em banco relacional. O banco relacional foi escolhido porque o domínio é fortemente associado (paciente, profissional, consulta, internação e quarto) e depende de integridade referencial. Em desenvolvimento é usado o H2 em memória, então os dados são apagados quando a aplicação para.

## Como executar a API

É necessário ter o JDK 17 ou superior e o Maven instalados. A API sobe em `http://localhost:8080`.

```bash
cd sistema-hospitalar
mvn spring-boot:run
```

## Integrantes

- Victor Reis Silva de Paula
- João Pedro Silva Dantas
- José Victor Uliana
