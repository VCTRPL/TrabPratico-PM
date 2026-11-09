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
        <<abstract>>
        -Long id
        -String nome
        -String registroProfissional
        -String telefone
        -String email
        +getTipo()* TipoProfissional
        +getAreaAtuacao()* String
        +definirAreaAtuacao(area)* void
        +atuaNaArea(area) boolean
    }
    class MedicoClinicoGeral {
        +encaminhar(paciente, especialidadeDestino, motivo) Encaminhamento
    }
    class MedicoEspecialista {
        -String especialidade
    }
    class Cirurgiao {
        -String areaCirurgica
    }
    class Encaminhamento {
        -Long id
        -String especialidadeDestino
        -String motivo
        -LocalDate dataEncaminhamento
    }
    class TipoProfissional {
        <<enumeration>>
        CLINICO_GERAL
        ESPECIALISTA
        CIRURGIAO
    }
    class Consulta {
        -Long id
        -LocalDate data
        -LocalTime horario
        -String motivo
        -String observacoesMedicas
        -StatusConsulta status
    }
    class Atendimento {
        <<abstract>>
        -Long id
        -LocalDate data
        -LocalTime horario
    }
    class ProcedimentoCirurgico {
        -String descricao
        -String tipoCirurgia
        -String observacoesPreOperatorias
        -String observacoesPosOperatorias
        -LocalDateTime terminoPrevisto
        -LocalDateTime inicioEfetivo
        -LocalDateTime terminoEfetivo
        -StatusProcedimento status
        +iniciar(momento) void
        +concluir(momento, observacoesPosOperatorias) void
        +cancelar() void
        +getInicioPrevisto() LocalDateTime
    }
    class StatusProcedimento {
        <<enumeration>>
        AGENDADO
        EM_ANDAMENTO
        CONCLUIDO
        CANCELADO
    }
    class Internacao {
        -Long id
        -LocalDate dataEntrada
        -LocalDate dataPrevistaAlta
        -LocalDate dataEfetivaAlta
        -String observacoes
        +atualizarPrevisao(dataPrevistaAlta, observacoes) void
        +darAlta(data) void
        +estaAtiva() boolean
    }
    class Quarto {
        -Long id
        -int numero
        -int andar
        -TipoQuarto tipo
        -int capacidadeMaxima
        -int ocupacaoAtual
        -SituacaoQuarto situacao
        +temVaga() boolean
        +ocupacaoAtual() int
    }
    class TipoQuarto {
        <<enumeration>>
        ENFERMARIA
        APARTAMENTO
        UTI
        ISOLAMENTO
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

    ProfissionalSaude <|-- MedicoClinicoGeral
    ProfissionalSaude <|-- MedicoEspecialista
    ProfissionalSaude <|-- Cirurgiao
    ProfissionalSaude --> TipoProfissional
    MedicoClinicoGeral "1" --> "0..*" Encaminhamento : emite
    Encaminhamento "0..*" --> "1" Paciente : encaminha
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
    Quarto --> TipoQuarto
    Atendimento <|-- ProcedimentoCirurgico
    Atendimento "0..*" --> "1" Paciente : atende
    Atendimento "0..*" --> "1" ProfissionalSaude : realizado por
    Cirurgiao "1" --> "0..*" ProcedimentoCirurgico : realiza
    ProcedimentoCirurgico --> StatusProcedimento
```

## Regras de negócio refletidas no modelo

- Consulta exige paciente e profissional responsável (RN2); o profissional não pode ter dois atendimentos no mesmo horário (RN3, verificada pela interface `AgendaProfissional` da camada Service).
- Internação exige paciente e quarto disponível (RN4); o quarto não ultrapassa `capacidadeMaxima` (RN5, validada em `temVaga`). Só a `Internacao` ocupa ou libera vaga do `Quarto`, e a situação do quarto é recalculada a cada mudança. Um paciente não tem duas internações ativas ao mesmo tempo.
- `ProcedimentoCirurgico` herda de `Atendimento` e só é realizado por `Cirurgiao`. O status segue AGENDADO → EM_ANDAMENTO → CONCLUIDO; não é possível concluir um procedimento que não foi iniciado, nem cancelar um já concluído.
- `HistoricoMedico` consolida consultas e internações do paciente (RN1 e RN6).
- Só o médico clínico geral emite `Encaminhamento`. O médico especialista exige especialidade e o cirurgião exige área cirúrgica.
