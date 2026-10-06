# Diagrama de Classes

Camada Model do Sistema Hospitalar (renderizado automaticamente pelo GitHub).

```mermaid
classDiagram
    class Paciente {
        -Long id
        -String nome
        -String cpf
        -LocalDate dataNascimento
        -String telefone
        -String endereco
        -String email
    }
    class ProfissionalSaude {
        -Long id
        -String nome
        -String registroProfissional
        -String especialidade
        -String telefone
        -String email
        +estaDisponivel(data, horario) boolean
    }
    class Consulta {
        -Long id
        -LocalDate data
        -LocalTime horario
        -String motivo
        -String observacoesMedicas
        -StatusConsulta status
    }
    class Internacao {
        -Long id
        -LocalDate dataEntrada
        -LocalDate dataPrevistaAlta
        -LocalDate dataEfetivaAlta
        -String observacoes
        +darAlta(data) void
        +estaAtiva() boolean
    }
    class Quarto {
        -Long id
        -int numero
        -int andar
        -int capacidadeMaxima
        -SituacaoQuarto situacao
        +temVaga() boolean
        +ocupacaoAtual() int
    }
    class HistoricoMedico {
        -List~String~ informacoesRelevantes
        +adicionarInformacao(info) void
    }
    class StatusConsulta {
        <<enumeration>>
        AGENDADA
        REALIZADA
        CANCELADA
    }
    class SituacaoQuarto {
        <<enumeration>>
        DISPONIVEL
        OCUPADO
    }

    Paciente "1" --> "0..*" Consulta : realiza
    ProfissionalSaude "1" --> "0..*" Consulta : atende
    Paciente "1" --> "0..*" Internacao : possui
    ProfissionalSaude "1" --> "0..*" Internacao : responsável por
    Quarto "1" --> "0..*" Internacao : aloca
    Paciente "1" *-- "1" HistoricoMedico : possui
    HistoricoMedico o-- "0..*" Consulta : consultas realizadas
    HistoricoMedico o-- "0..*" Internacao : internações realizadas
    Consulta --> StatusConsulta
    Quarto --> SituacaoQuarto
```

## Regras de negócio refletidas no modelo

- Consulta exige paciente e profissional responsável (RN2); o profissional não pode ter dois atendimentos no mesmo horário (RN3, validada em `estaDisponivel`).
- Internação exige paciente e quarto disponível (RN4); o quarto não ultrapassa `capacidadeMaxima` (RN5, validada em `temVaga`).
- `HistoricoMedico` consolida consultas e internações do paciente (RN1 e RN6).
