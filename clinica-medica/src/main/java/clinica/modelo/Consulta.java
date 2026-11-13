package clinica.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Consulta {

    private int id;
    private final Paciente paciente;
    private final Medico medico;
    private final LocalDateTime dataHora;
    private final String motivo;
    private StatusConsulta status;

    public Consulta(Paciente paciente, Medico medico, LocalDateTime dataHora, String motivo) {
        this.paciente = paciente;
        this.medico = medico;
        this.dataHora = dataHora;
        this.motivo = motivo;
        this.status = StatusConsulta.AGENDADA;
    }

    public Consulta(int id, Paciente paciente, Medico medico, LocalDateTime dataHora,
                    String motivo, StatusConsulta status) {
        this(paciente, medico, dataHora, motivo);
        this.id = id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Consulta #%d | %s | Paciente: %s | Medico: %s (%s) | %s | Motivo: %s",
                id, dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                paciente.getNome(), medico.getNome(), medico.getEspecialidade(), status, motivo);
    }
}
