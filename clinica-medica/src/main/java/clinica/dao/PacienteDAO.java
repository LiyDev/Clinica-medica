package clinica.dao;

import clinica.modelo.Paciente;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PacienteDAO {

    private static final String INSERIR =
            "INSERT INTO paciente (nome, cpf, data_nascimento, telefone) VALUES (?, ?, ?, ?)";
    private static final String BUSCAR_POR_ID =
            "SELECT id, nome, cpf, data_nascimento, telefone FROM paciente WHERE id = ?";
    private static final String BUSCAR_POR_CPF =
            "SELECT id, nome, cpf, data_nascimento, telefone FROM paciente WHERE cpf = ?";
    private static final String LISTAR =
            "SELECT id, nome, cpf, data_nascimento, telefone FROM paciente ORDER BY nome";
    private static final String ATUALIZAR =
            "UPDATE paciente SET nome = ?, telefone = ? WHERE id = ?";
    private static final String REMOVER = "DELETE FROM paciente WHERE id = ?";

    public void inserir(Paciente paciente) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(INSERIR, new String[] {"id"})) {
            stmt.setString(1, paciente.getNome());
            stmt.setString(2, paciente.getCpf());
            stmt.setDate(3, Date.valueOf(paciente.getDataNascimento()));
            stmt.setString(4, paciente.getTelefone());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    paciente.setId(chaves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erro ao inserir paciente", e);
        }
    }

    public Optional<Paciente> buscarPorId(int id) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(BUSCAR_POR_ID)) {
            stmt.setInt(1, id);
            return buscarUm(stmt);
        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar paciente por id", e);
        }
    }

    public Optional<Paciente> buscarPorCpf(String cpf) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(BUSCAR_POR_CPF)) {
            stmt.setString(1, cpf);
            return buscarUm(stmt);
        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar paciente por CPF", e);
        }
    }

    public List<Paciente> listar() {
        List<Paciente> pacientes = new ArrayList<>();
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(LISTAR);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                pacientes.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Erro ao listar pacientes", e);
        }
        return pacientes;
    }

    public boolean atualizar(Paciente paciente) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(ATUALIZAR)) {
            stmt.setString(1, paciente.getNome());
            stmt.setString(2, paciente.getTelefone());
            stmt.setInt(3, paciente.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar paciente", e);
        }
    }

    public boolean remover(int id) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(REMOVER)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Erro ao remover paciente (ele pode ter consultas)", e);
        }
    }

    private Optional<Paciente> buscarUm(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapear(rs));
            }
            return Optional.empty();
        }
    }

    private Paciente mapear(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getDate("data_nascimento").toLocalDate(),
                rs.getString("telefone"));
    }
}
