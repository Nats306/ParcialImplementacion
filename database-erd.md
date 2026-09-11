# Diagrama Entidad-Relación — Camel vs Dwarf Racing System

> `User` y `Role` no son entidades de este esquema: viven en Keycloak (identity provider externo). La app nunca persiste usuarios ni contraseñas propias; `AuditLog.username` guarda el username como texto plano (no hay FK a una tabla de usuarios porque no existe en esta base de datos).

```mermaid
erDiagram
    TEAM ||--o{ COMPETITOR : "tiene miembros"
    RACE ||--o{ RACE_REGISTRATION : "recibe inscripciones"
    COMPETITOR ||--o{ RACE_REGISTRATION : "se inscribe"
    TEAM ||--o{ RACE_REGISTRATION : "se inscribe (carreras TEAM)"
    RACE_REGISTRATION ||--o| RACE_RESULT : "produce"
    RACE ||--o{ RACE_RESULT : "tiene resultados"

    TEAM {
        bigint id PK
        varchar name UK "unique, not null"
        varchar description
        date creation_date "not null"
        varchar coach "not null"
        varchar status "enum: TeamStatus, not null"
        int victories "not null"
        int defeats "not null"
    }

    COMPETITOR {
        uuid id PK
        varchar name "not null"
        varchar nickname UK "unique, not null"
        varchar competitor_type "enum: CompetitorType, not null"
        int age "not null"
        double weight "not null"
        double height "not null"
        varchar country "not null"
        varchar current_status "enum: CompetitorStatus, not null"
        date registration_date "not null"
        bigint team_id FK "nullable"
        int victories
        int defeats
        int completed_races
    }

    RACE {
        bigint id PK
        varchar name "not null"
        varchar description
        datetime scheduled_date_time "not null"
        varchar start_location "not null"
        varchar finish_location "not null"
        double distance_meters "not null"
        int max_participants "not null"
        varchar race_type "enum: RaceType, not null"
        varchar race_status "enum: RaceStatus, not null"
        varchar organizer "not null"
        datetime registration_deadline "not null"
        datetime creation_date "not null"
        datetime last_modification_date "not null"
    }

    RACE_REGISTRATION {
        bigint id PK
        bigint race_id FK "not null"
        uuid competitor_id FK "nullable, según race_type"
        bigint team_id FK "nullable, según race_type"
        datetime registration_date "not null"
        varchar status "enum: RegistrationStatus, not null"
        int starting_position "nullable, unique junto a race_id"
        varchar validation_notes
        varchar registered_by "not null"
    }

    RACE_RESULT {
        bigint id PK
        bigint race_id FK "not null"
        bigint registration_id FK "unique, not null (1:1)"
        int starting_position "not null"
        int final_position
        bigint completion_time_millis
        bigint penalty_time_millis "not null"
        varchar result_status "enum: ResultStatus, not null"
        varchar notes
        varchar recorded_by "not null"
        datetime recorded_at "not null"
    }

    AUDIT_LOG {
        bigint id PK
        varchar username "not null (referencia externa, sin FK — el usuario vive en Keycloak)"
        varchar action "enum: AuditAction, not null"
        varchar entity_type "not null"
        varchar entity_id
        datetime timestamp "not null"
        varchar description
        text previous_value "opcional, no usado actualmente"
        text new_value
    }
```

## Notas

- **User / Role**: intencionalmente fuera de este diagrama. Keycloak es la única fuente de verdad para usuarios, roles y contraseñas; la app solo valida JWTs. `AuditLog.username` es un valor de texto capturado del token, no una FK.
- **RACE_REGISTRATION** también tiene tres unique constraints compuestas (todas empiezan por `race_id`): `(race_id, competitor_id)`, `(race_id, team_id)` y `(race_id, starting_position)` — evitan doble inscripción del mismo competidor/equipo en la misma carrera y posiciones de salida repetidas.
- **Índices ya agregados** (commiteados a tu proyecto) en las 6 tablas — `competitors`, `teams`, `races`, `race_registrations`, `race_results` y `audit_logs`: cada FK simple que Postgres no indexa solo (porque no es PK ni cabeza de una unique constraint) y cada columna usada como filtro común (status, type, fechas) ahora tiene su `@Index`.
- Este archivo es Markdown puro con un bloque ` ```mermaid `: si lo pegas en tu `README.md` de GitHub, el diagrama se renderiza solo — GitHub soporta Mermaid nativamente.
