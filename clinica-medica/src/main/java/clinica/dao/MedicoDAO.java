package clinica.dao;

import clinica.modelo.Medico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicoDAO {

    private static final String INSERIR =
            "INSERT INTO medico (nome, crm, especialidade) VALUES (?, ?, ?)";
    private static final String BUSCAR_POR_ID =
            "SELECT id, nome, crm, especialidade FROM medico WHERE id = ?";
    private static final String BUSCAR_POR_CRM =
            "SELECT id, nome, crm, especialidade FROM medico WHERE crm = ?";
    private static final String LISTAR =
            "SELECT id, nome, crm, especialidade FROM medico ORDER BY nome";
    private static final String ATUALIZAR =
            "UPDATE medico SET especialidade = ? WHERE id = ?";
    private static final String REMOVER = "DELETE FROM medico WHERE id = ?";

    public void inserir(Medico medico) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(INSERIR, new String[] {"id"})) {
            stmt.setString(1, medico.getNome());
            stmt.setString(2, medico.getCrm());
            stmt.setString(3, medico.getEspecialidade());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    medico.setId(chaves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erro ao inserir medico", e);
        }
    }

    public Optional<Medico> buscarPorId(int id) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(BUSCAR_POR_ID)) {
            stmt.setInt(1, id);
            return buscarUm(stmt);
        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar medico por id", e);
        }
    }

    public Optional<Medico> buscarPorCrm(String crm) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(BUSCAR_POR_CRM)) {
            stmt.setString(1, crm);
            return buscarUm(stmt);
        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar medico por CRM", e);
        }
    }

    public List<Medico> listar() {
        List<Medico> medicos = new ArrayList<>();
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(LISTAR);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                medicos.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Erro ao listar medicos", e);
        }
        return medicos;
    }

    public boolean atualizar(Medico medico) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(ATUALIZAR)) {
            stmt.setString(1, medico.getEspecialidade());
            stmt.setInt(2, medico.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar medico", e);
        }
    }

    public boolean remover(int id) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(REMOVER)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Erro ao remover medico (ele pode ter consultas)", e);
        }
    }

    private Optional<Medico> buscarUm(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapear(rs));
            }
            return Optional.empty();
        }
    }

    private Medico mapear(ResultSet rs) throws SQLException {
        return new Medico(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("crm"),
                rs.getString("especialidade"));
    }
}
