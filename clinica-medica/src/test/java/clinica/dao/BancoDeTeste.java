package clinica.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

final class BancoDeTeste {

    private BancoDeTeste() {
    }

    static void configurar() {
        String url = valor("DB_URL_TESTE", "jdbc:postgresql://localhost:5432/clinica_teste");
        String usuario = valor("DB_USER_TESTE", "postgres");
        String senha = valor("DB_PASSWORD_TESTE", "");
        Conexao.configurar(url, usuario, senha);
    }

    static boolean disponivel() {
        configurar();
        try (Connection conexao = Conexao.obter()) {
            return conexao.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    static void criarTabelas() throws SQLException, IOException {
        try (InputStream entrada = BancoDeTeste.class.getResourceAsStream("/schema.sql")) {
            String script = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conexao = Conexao.obter(); Statement stmt = conexao.createStatement()) {
                for (String comando : script.split(";")) {
                    if (!comando.trim().isEmpty()) {
                        stmt.execute(comando);
                    }
                }
            }
        }
    }

    static void limparTabelas() throws SQLException {
        try (Connection conexao = Conexao.obter(); Statement stmt = conexao.createStatement()) {
            stmt.execute("TRUNCATE consulta, paciente, medico RESTART IDENTITY CASCADE");
        }
    }

    private static String valor(String nome, String padrao) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isEmpty()) {
            return padrao;
        }
        return valor;
    }
}
