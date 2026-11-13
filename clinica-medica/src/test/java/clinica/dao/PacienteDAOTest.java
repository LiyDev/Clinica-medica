package clinica.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import clinica.modelo.Paciente;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PacienteDAOTest {

    private final PacienteDAO dao = new PacienteDAO();

    @BeforeAll
    static void prepararBanco() throws Exception {
        assumeTrue(BancoDeTeste.disponivel(), "PostgreSQL de teste nao esta disponivel");
        BancoDeTeste.criarTabelas();
    }

    @BeforeEach
    void limpar() throws Exception {
        BancoDeTeste.limparTabelas();
    }

    private Paciente novoPaciente(String nome, String cpf) {
        return new Paciente(nome, cpf, LocalDate.of(1998, 5, 20), "71999990000");
    }

    @Test
    void deveInserirEGerarId() {
        Paciente paciente = novoPaciente("Ana Souza", "11111111111");

        dao.inserir(paciente);

        assertNotEquals(0, paciente.getId());
    }

    @Test
    void deveBuscarPorIdECpf() {
        Paciente paciente = novoPaciente("Ana Souza", "11111111111");
        dao.inserir(paciente);

        Paciente porId = dao.buscarPorId(paciente.getId()).orElseThrow();
        Paciente porCpf = dao.buscarPorCpf("11111111111").orElseThrow();

        assertEquals("Ana Souza", porId.getNome());
        assertEquals(LocalDate.of(1998, 5, 20), porId.getDataNascimento());
        assertEquals(paciente.getId(), porCpf.getId());
    }

    @Test
    void buscarInexistenteDeveRetornarVazio() {
        assertTrue(dao.buscarPorId(999).isEmpty());
        assertTrue(dao.buscarPorCpf("00000000000").isEmpty());
    }

    @Test
    void naoDevePermitirCpfRepetido() {
        dao.inserir(novoPaciente("Ana Souza", "11111111111"));

        assertThrows(DaoException.class, () -> dao.inserir(novoPaciente("Outra Ana", "11111111111")));
    }

    @Test
    void deveListarEmOrdemAlfabetica() {
        dao.inserir(novoPaciente("Carlos Lima", "33333333333"));
        dao.inserir(novoPaciente("Ana Souza", "11111111111"));
        dao.inserir(novoPaciente("Bruno Alves", "22222222222"));

        List<Paciente> pacientes = dao.listar();

        assertEquals(3, pacientes.size());
        assertEquals("Ana Souza", pacientes.get(0).getNome());
        assertEquals("Bruno Alves", pacientes.get(1).getNome());
        assertEquals("Carlos Lima", pacientes.get(2).getNome());
    }

    @Test
    void deveAtualizarNomeETelefone() {
        Paciente paciente = novoPaciente("Ana Souza", "11111111111");
        dao.inserir(paciente);

        paciente.setNome("Ana Souza Lima");
        paciente.setTelefone("71988887777");
        assertTrue(dao.atualizar(paciente));

        Paciente atualizado = dao.buscarPorId(paciente.getId()).orElseThrow();
        assertEquals("Ana Souza Lima", atualizado.getNome());
        assertEquals("71988887777", atualizado.getTelefone());
    }

    @Test
    void deveRemoverPaciente() {
        Paciente paciente = novoPaciente("Ana Souza", "11111111111");
        dao.inserir(paciente);

        assertTrue(dao.remover(paciente.getId()));

        assertTrue(dao.buscarPorId(paciente.getId()).isEmpty());
        assertFalse(dao.remover(paciente.getId()));
    }
}
