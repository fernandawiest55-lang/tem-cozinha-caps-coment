package dao;

import config.FatorConexao;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

// Faz todas as operacoes de banco relacionadas ao usuario
// Sempre usamos PreparedStatement com ? pra evitar SQL Injection
public class UsuarioDAO {

    // Salva um novo usuario no banco e retorna o objeto com o id gerado
    public Usuario salvar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, senha, restricoes, dieta) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id, criado_em";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, usuario.getNome());
        ps.setString(2, usuario.getEmail());
        ps.setString(3, usuario.getSenha());
        ps.setString(4, usuario.getRestricoes() != null ? usuario.getRestricoes() : "");
        ps.setString(5, usuario.getDieta()      != null ? usuario.getDieta()      : "");

        ResultSet rs = ps.executeQuery();

        // RETURNING faz o banco devolver o id e a data criados automaticamente
        if (rs.next()) {
            usuario.setId(rs.getInt("id"));
            usuario.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        }

        rs.close();
        ps.close();
        conn.close();

        return usuario;
    }

    // Busca um usuario pelo email
    // Retorna null se o email nao existir ou se o usuario estiver desativado
    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE email = ? AND ativo = TRUE";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();
        Usuario usuario = null;

        if (rs.next()) {
            usuario = mapear(rs);
        }

        rs.close();
        ps.close();
        conn.close();

        return usuario;
    }

    // Busca um usuario pelo id
    // Usado nas rotas que ja tem o userId vindo do token JWT
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ? AND ativo = TRUE";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        Usuario usuario = null;

        if (rs.next()) {
            usuario = mapear(rs);
        }

        rs.close();
        ps.close();
        conn.close();

        return usuario;
    }

    // Atualiza nome e email do usuario
    public void atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nome = ?, email = ? WHERE id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, usuario.getNome());
        ps.setString(2, usuario.getEmail());
        ps.setInt(3, usuario.getId());

        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // Atualiza as restricoes alimentares e dieta do usuario
    public void atualizarPerfilAlimentar(int id, String restricoes, String dieta) throws SQLException {
        String sql = "UPDATE usuario SET restricoes = ?, dieta = ? WHERE id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, restricoes);
        ps.setString(2, dieta);
        ps.setInt(3, id);

        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // Atualiza a senha - a senha ja deve chegar como hash
    public void atualizarSenha(int id, String senhaHash) throws SQLException {
        String sql = "UPDATE usuario SET senha = ? WHERE id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, senhaHash);
        ps.setInt(2, id);

        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // Converte uma linha do ResultSet em um objeto Usuario
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();

        u.setId(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setSenha(rs.getString("senha"));
        u.setRestricoes(rs.getString("restricoes"));
        u.setDieta(rs.getString("dieta"));
        u.setAtivo(rs.getBoolean("ativo"));

        Timestamp ts = rs.getTimestamp("criado_em");
        if (ts != null) u.setCriadoEm(ts.toLocalDateTime());

        return u;
    }
}
