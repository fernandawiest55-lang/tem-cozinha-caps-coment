package dao;

import config.FatorConexao;
import model.Ingrediente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Faz todas as operacoes de banco dos ingredientes
// Todo metodo filtra por usuarioId pra garantir que cada usuario ve so os seus
public class IngredienteDAO {

    // Lista os ingredientes de um usuario ordenados por nome
    public List<Ingrediente> listar(int usuarioId) throws SQLException {
        List<Ingrediente> lista = new ArrayList<>();
        String sql = "SELECT * FROM ingrediente WHERE usuario_id = ? ORDER BY nome";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, usuarioId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }

        rs.close();
        ps.close();
        conn.close();

        return lista;
    }

    // Salva um ingrediente novo e retorna com o id gerado
    public Ingrediente salvar(Ingrediente ingrediente) throws SQLException {
        String sql = "INSERT INTO ingrediente (usuario_id, nome, quantidade, unidade, local) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, ingrediente.getUsuarioId());
        ps.setString(2, ingrediente.getNome());
        ps.setDouble(3, ingrediente.getQuantidade());
        ps.setString(4, ingrediente.getUnidade());
        ps.setString(5, ingrediente.getLocal());

        ResultSet rs = ps.executeQuery();
        if (rs.next()) ingrediente.setId(rs.getInt("id"));

        rs.close();
        ps.close();
        conn.close();

        return ingrediente;
    }

    // Atualiza os dados de um ingrediente
    // AND usuario_id garante que o usuario so edita os proprios ingredientes
    public void atualizar(Ingrediente ingrediente) throws SQLException {
        String sql = "UPDATE ingrediente " +
                     "SET nome = ?, quantidade = ?, unidade = ?, local = ?, atualizado_em = NOW() " +
                     "WHERE id = ? AND usuario_id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, ingrediente.getNome());
        ps.setDouble(2, ingrediente.getQuantidade());
        ps.setString(3, ingrediente.getUnidade());
        ps.setString(4, ingrediente.getLocal());
        ps.setInt(5, ingrediente.getId());
        ps.setInt(6, ingrediente.getUsuarioId());

        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // Exclui um ingrediente - verifica o usuarioId pra um usuario nao deletar do outro
    public void excluir(int id, int usuarioId) throws SQLException {
        String sql = "DELETE FROM ingrediente WHERE id = ? AND usuario_id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, id);
        ps.setInt(2, usuarioId);

        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // Retorna so os nomes dos ingredientes de um usuario
    // Usado pelo ReceitaDAO pra buscar sugestoes baseadas na despensa
    public List<String> listarNomes(int usuarioId) throws SQLException {
        List<String> nomes = new ArrayList<>();
        String sql = "SELECT nome FROM ingrediente WHERE usuario_id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, usuarioId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            nomes.add(rs.getString("nome"));
        }

        rs.close();
        ps.close();
        conn.close();

        return nomes;
    }

    // Converte uma linha do ResultSet em um objeto Ingrediente
    private Ingrediente mapear(ResultSet rs) throws SQLException {
        Ingrediente i = new Ingrediente();

        i.setId(rs.getInt("id"));
        i.setUsuarioId(rs.getInt("usuario_id"));
        i.setNome(rs.getString("nome"));
        i.setQuantidade(rs.getDouble("quantidade"));
        i.setUnidade(rs.getString("unidade"));
        i.setLocal(rs.getString("local"));

        return i;
    }
}
