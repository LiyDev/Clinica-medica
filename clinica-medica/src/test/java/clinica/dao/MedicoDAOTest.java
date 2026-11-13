package clinica.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import clinica.modelo.Medico;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedicoDAOTest {

    private final MedicoDAO dao = new MedicoDAO();

    @BeforeAll
    static void prepararBanco() throws Exception {
        assumeTrue(BancoDeTeste.disponivel(), "PostgreSQL de teste nao esta disponivel");
        BancoDeTeste.criarTabelas();
    }

    @BeforeEach
    void limpar() throws Exception {
        BancoDeTeste.limparTabelas();
    }

    @Test
    void deveInserirEBuscarPorCrm() {
        Medico medico = new Medico("Marcos Pinto", "CRM-BA 12345", "Cardiologia");
        dao.inserir(medico);

        Medico encontrado = dao.buscarPorCrm("CRM-BA 12345").orElseThrow();

        assertEquals(medico.getId(), encontrado.getId());
        assertEquals("Cardiologia", encontrado.getEspecialidade());
    }

    @Test
    void naoDevePermitirCrmRepetido() {
        dao.inserir(new Medico("Marcos Pinto", "CRM-BA 12345", "Cardiologia"));

        assertThrows(DaoException.class,
                () -> dao.inserir(new Medico("Outro Medico", "CRM-BA 12345", "Pediatria")));
    }

    @Test
    void deveListarEmOrdemAlfabetica() {
        dao.inserir(new Medico("Marcos Pinto", "CRM-BA 2", "Cardiologia"));
        dao.inserir(new Medico("Beatriz Rocha", "CRM-BA 1", "Pediatria"));

        List<Medico> medicos = dao.listar();

        assertEquals(2, medicos.size());
        assertEquals("Beatriz Rocha", medicos.get(0).getNome());
    }

    @Test
    void deveAtualizarEspecialidade() {
        Medico medico = new Medico("Marcos Pinto", "CRM-BA 12345", "Cardiologia");
        dao.inserir(medico);

        medico.setEspecialidade("Clinica Geral");
        assertTrue(dao.atualizar(medico));

        assertEquals("Clinica Geral", dao.buscarPorId(medico.getId()).orElseThrow().getEspecialidade());
    }

    @Test
    void deveRemoverMedico() {
        Medico medico = new Medico("Marcos Pinto", "CRM-BA 12345", "Cardiologia");
        dao.inserir(medico);

        assertTrue(dao.remover(medico.getId()));
        assertTrue(dao.buscarPorId(medico.getId()).isEmpty());
    }
}
