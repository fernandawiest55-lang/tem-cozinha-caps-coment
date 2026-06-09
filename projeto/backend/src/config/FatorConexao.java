package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Essa classe é responsável por conectar no banco de dados PostgreSQL
// Todo DAO chama ConnectionFactory.getConnection() pra abrir a conexão
public class FatorConexao {

    // Dados do banco - QUAL VAMOS USAR?
    private static final String URL    = "jdbc:postgresql://localhost:5432/tem_na_cozinha";
    private static final String USUARIO = "postgres";
    private static final String SENHA   = "usuario";

    // Esse bloco roda uma vez quando a classe é carregada
    // Ele carrega o driver do PostgreSQL na memória
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // Se o .jar do driver não estiver no classpath, vai cair aqui
            throw new RuntimeException("Driver PostgreSQL não encontrado. Adicione o postgresql-42.x.x.jar.", e);
        }
    }

    // Abre e retorna uma nova conexão com o banco
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    // Construtor privado: essa classe não precisa ser instanciada!!
    private FatorConexao() {}
}
