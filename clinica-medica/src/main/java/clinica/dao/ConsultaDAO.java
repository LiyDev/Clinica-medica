package clinica.dao;

import clinica.modelo.Consulta;
import clinica.modelo.Medico;
import clinica.modelo.Paciente;
import clinica.modelo.StatusConsulta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConsultaDAO {

    private static final String INSERIR =
            "INSERT INTO consulta (paciente_id, medico_id, data_hora, motivo, status) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_COM_JOIN =
            "SELECT c.id, c.data_hora, c.motivo, c.status, "
            + "p.id AS paciente_id, p.nome AS paciente_nome, p.cpf, p.data_nascimento, p.telefone, "
            + "m.id AS medico_id, m.nome AS medico_nome, m.crm, m.especialidade "
            + "FROM consulta c "
            + "JOIN paciente p ON p.id = c.paciente_id "
            + "JOIN medico m ON m.id = c.medico_id ";

    private static final String BUSCAR_POR_ID = SELECT_COM_JOIN + "WHERE c.id = ?";
    private static final String LISTAR_POR_PACIENTE =
            SELECT_COM_JOIN + "WHERE c.paciente_id = ? ORDER BY c.data_hora";
    private static final String LISTAR_POR_MEDICO =
            SELECT_COM_JOIN + "WHERE c.medico_id = ? ORDER BY c.data_hora";
    private static final String HORARIO_OCUPADO =
            "SELECT 1 FROM consulta WHERE medico_id = ? AND data_hora = ? AND status <> 'CANCELADA'";
    private static final String ATUALIZAR_STATUS = "UPDATE consulta SET status = ? WHERE id = ?";

    public void agendar(Consulta consulta) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(INSERIR, new String[] {"id"})) {
            stmt.setInt(1, consulta.getPaciente().getId());
            stmt.setInt(2, consulta.getMedico().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(consulta.getDataHora()));
            stmt.setString(4, consulta.getMotivo());
            stmt.setString(5, consulta.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    consulta.setId(chaves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erro ao agendar consulta", e);
        }
    }

    public Optional<Consulta> buscarPorId(int id) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(BUSCAR_POR_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar consulta", e);
        }
    }

    public List<Consulta> listarPorPaciente(int pacienteId) {
        return listar(LISTAR_POR_PACIENTE, pacienteId);
    }

    public List<Consulta> listarPorMedico(int medicoId) {
        return listar(LISTAR_POR_MEDICO, medicoId);
    }

    public boolean horarioOcupado(int medicoId, LocalDateTime dataHora) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(HORARIO_OCUPADO)) {
            stmt.setInt(1, medicoId);
            stmt.setTimestamp(2, Timestamp.valueOf(dataHora));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Erro ao verificar horario do medico", e);
        }
    }

    public boolean atualizarStatus(int id, StatusConsulta status) {
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(ATUALIZAR_STATUS)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar status da consulta", e);
        }
    }

    private List<Consulta> listar(String sql, int id) {
        List<Consulta> consultas = new ArrayList<>();
        try (Connection conexao = Conexao.obter();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    consultas.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erro ao listar consultas", e);
        }
        return consultas;
    }

    private Consulta mapear(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente(
                rs.getInt("paciente_id"),
                rs.getString("paciente_nome"),
                rs.getString("cpf"),
                rs.getDate("data_nascimento").toLocalDate(),
                rs.getString("telefone"));
        Medico medico = new Medico(
                rs.getInt("medico_id"),
                rs.getString("medico_nome"),
                rs.getString("crm"),
                rs.getString("especialidade"));
        return new Consulta(
                rs.getInt("id"),
                paciente,
                medico,
                rs.getTimestamp("data_hora").toLocalDateTime(),
                rs.getString("motivo"),
                StatusConsulta.valueOf(rs.getString("status")));
    }
}
