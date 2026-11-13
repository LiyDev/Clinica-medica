CREATE TABLE IF NOT EXISTS paciente (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    telefone VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS medico (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    crm VARCHAR(20) NOT NULL UNIQUE,
    especialidade VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS consulta (
    id SERIAL PRIMARY KEY,
    paciente_id INTEGER NOT NULL REFERENCES paciente (id),
    medico_id INTEGER NOT NULL REFERENCES medico (id),
    data_hora TIMESTAMP NOT NULL,
    motivo VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADA'
        CHECK (status IN ('AGENDADA', 'REALIZADA', 'CANCELADA'))
);

-- um medico nao pode ter duas consultas no mesmo horario (consulta cancelada libera o horario)
CREATE UNIQUE INDEX IF NOT EXISTS ux_consulta_medico_horario
    ON consulta (medico_id, data_hora)
    WHERE status <> 'CANCELADA';
