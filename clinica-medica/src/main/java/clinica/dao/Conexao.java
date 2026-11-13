package clinica.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class Conexao {

    private static String url = lerVariavel("DB_URL", "jdbc:postgresql://localhost:5432/clinica");
    private static String usuario = lerVariavel("DB_USER", "postgres");
    private static String senha = lerVariavel("DB_PASSWORD", "");

    private Conexao() {
    }

    public static Connection obter() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }

  
    public static void configurar(String novaUrl, String novoUsuario, String novaSenha) {
        url = novaUrl;
        usuario = novoUsuario;
        senha = novaSenha;
    }

    private static String lerVariavel(String nome, String padrao) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isEmpty()) {
            return padrao;
        }
        return valor;
    }
}
