package clinica.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import clinica.modelo.Consulta;
import clinica.modelo.Medico;
import clinica.modelo.Paciente;
import clinica.modelo.StatusConsulta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultaDAOTest {

    private static final LocalDateTime HORARIO = LocalDateTime.of(2030, 1, 15, 9, 0);

    private final PacienteDAO pacientes = new PacienteDAO();
    private final MedicoDAO medicos = new MedicoDAO();
    private final ConsultaDAO dao = new ConsultaDAO();

    private Paciente paciente;
    private Medico medico;

    @BeforeAll
    static void prepararBanco() throws Exception {
        assumeTrue(BancoDeTeste.disponivel(), "PostgreSQL de teste nao esta disponivel");
        BancoDeTeste.criarTabelas();
    }

    @BeforeEach
    void prepararDados() throws Exception {
        BancoDeTeste.limparTabelas();

        paciente = new Paciente("Ana Souza", "11111111111", LocalDate.of(1998, 5, 20), "71999990000");
        pacientes.inserir(paciente);

        medico = new Medico("Marcos Pinto", "CRM-BA 12345", "Cardiologia");
        medicos.inserir(medico);
    }

    private Consulta agendar(LocalDateTime dataHora) {
        Consulta consulta = new Consulta(paciente, medico, dataHora, "Rotina");
        dao.agendar(consulta);
        return consulta;
    }

    @Test
    void deveAgendarEBuscarComDadosDePacienteEMedico() {
        Consulta consulta = agendar(HORARIO);

        Consulta encontrada = dao.buscarPorId(consulta.getId()).orElseThrow();

        assertEquals(HORARIO, encontrada.getDataHora());
        assertEquals(StatusConsulta.AGENDADA, encontrada.getStatus());
        assertEquals("Ana Souza", encontrada.getPaciente().getNome());
        assertEquals("Cardiologia", encontrada.getMedico().getEspecialidade());
    }

    @Test
    void deveListarConsultasPorPacienteEPorMedicoEmOrdemDeHorario() {
        agendar(HORARIO.plusHours(2));
        agendar(HORARIO);

        List<Consulta> doPaciente = dao.listarPorPaciente(paciente.getId());
        List<Consulta> doMedico = dao.listarPorMedico(medico.getId());

        assertEquals(2, doPaciente.size());
        assertEquals(HORARIO, doPaciente.get(0).getDataHora());
        assertEquals(2, doMedico.size());
    }

    @Test
    void deveIdentificarHorarioOcupado() {
        agendar(HORARIO);

        assertTrue(dao.horarioOcupado(medico.getId(), HORARIO));
        assertFalse(dao.horarioOcupado(medico.getId(), HORARIO.plusHours(1)));
    }

    @Test
    void bancoNaoDevePermitirDuasConsultasDoMesmoMedicoNoMesmoHorario() {
        agendar(HORARIO);

        assertThrows(DaoException.class, () -> agendar(HORARIO));
    }

    @Test
    void consultaCanceladaLiberaOHorario() {
        Consulta consulta = agendar(HORARIO);

        assertTrue(dao.atualizarStatus(consulta.getId(), StatusConsulta.CANCELADA));

        assertFalse(dao.horarioOcupado(medico.getId(), HORARIO));
        agendar(HORARIO);
        assertEquals(2, dao.listarPorMedico(medico.getId()).size());
    }

    @Test
    void deveMarcarConsultaComoRealizada() {
        Consulta consulta = agendar(HORARIO);

        dao.atualizarStatus(consulta.getId(), StatusConsulta.REALIZADA);

        assertEquals(StatusConsulta.REALIZADA, dao.buscarPorId(consulta.getId()).orElseThrow().getStatus());
    }

    @Test
    void naoDeveRemoverPacienteQueTemConsulta() {
        agendar(HORARIO);

        assertThrows(DaoException.class, () -> pacientes.remover(paciente.getId()));
    }

    @Test
    void buscarConsultaInexistenteDeveRetornarVazio() {
        assertTrue(dao.buscarPorId(999).isEmpty());
    }
}
