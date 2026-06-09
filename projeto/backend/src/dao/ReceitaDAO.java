package dao;

import config.FatorConexao;
import model.Receita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// Faz todas as operacoes de banco relacionadas a receitas
public class ReceitaDAO {

    // Lista todas as receitas ordenadas por melhor nota
    public List<Receita> listarTodas() throws SQLException {
        List<Receita> lista = new ArrayList<>();
        String sql = "SELECT * FROM receita ORDER BY nota_media DESC, id DESC";

        Connection conn = FatorConexao.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            lista.add(mapear(rs));
        }

        rs.close();
        st.close();
        conn.close();

        return lista;
    }

    // Busca uma receita pelo id - retorna null se nao encontrar
    public Receita buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM receita WHERE id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        Receita receita = null;

        if (rs.next()) {
            receita = mapear(rs);
        }

        rs.close();
        ps.close();
        conn.close();

        return receita;
    }

    // Sugere receitas baseadas nos ingredientes que o usuario tem na despensa
    // Para cada ingrediente faz um LIKE no campo ingredientes_necessarios
    // Se nao achar nada, retorna todas as receitas
    public List<Receita> buscarSugestoes(List<String> nomes) throws SQLException {
        if (nomes == null || nomes.isEmpty()) return listarTodas();

        // Monta a query com um LIKE para cada ingrediente
        StringBuilder sql = new StringBuilder("SELECT * FROM receita WHERE ");
        for (int i = 0; i < nomes.size(); i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("LOWER(ingredientes_necessarios) LIKE ?");
        }
        sql.append(" ORDER BY nota_media DESC LIMIT 10");

        List<Receita> lista = new ArrayList<>();
        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString());

        // Preenche cada ? com o ingrediente em minusculo entre %
        for (int i = 0; i < nomes.size(); i++) {
            ps.setString(i + 1, "%" + nomes.get(i).toLowerCase() + "%");
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }

        rs.close();
        ps.close();
        conn.close();

        // Se nao achou nada com os ingredientes, retorna tudo
        if (lista.isEmpty()) return listarTodas();
        return lista;
    }

    // Salva uma receita nova e retorna com o id gerado
    public Receita salvar(Receita receita) throws SQLException {
        String sql = "INSERT INTO receita " +
                     "(titulo, descricao, ingredientes_necessarios, modo_preparo, " +
                     "tempo_preparo, dificuldade, categoria, tags) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, receita.getTitulo());
        ps.setString(2, receita.getDescricao());
        ps.setString(3, receita.getIngredientesNecessarios());
        ps.setString(4, receita.getModoPreparo());
        ps.setInt(5, receita.getTempoPreparo());
        ps.setString(6, receita.getDificuldade());
        ps.setString(7, receita.getCategoria());
        ps.setString(8, receita.getTags());

        ResultSet rs = ps.executeQuery();
        if (rs.next()) receita.setId(rs.getInt("id"));

        rs.close();
        ps.close();
        conn.close();

        return receita;
    }

    // Recalcula a nota media da receita usando AVG direto no banco
    public void atualizarNota(int receitaId) throws SQLException {
        String sql = "UPDATE receita SET " +
                     "nota_media = (SELECT AVG(nota) FROM avaliacao WHERE receita_id = ?), " +
                     "total_avaliacoes = (SELECT COUNT(*) FROM avaliacao WHERE receita_id = ?) " +
                     "WHERE id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, receitaId);
        ps.setInt(2, receitaId);
        ps.setInt(3, receitaId);

        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // Converte uma linha do ResultSet em um objeto Receita
    private Receita mapear(ResultSet rs) throws SQLException {
        Receita r = new Receita();

        r.setId(rs.getInt("id"));
        r.setTitulo(rs.getString("titulo"));
        r.setDescricao(rs.getString("descricao"));
        r.setIngredientesNecessarios(rs.getString("ingredientes_necessarios"));
        r.setModoPreparo(rs.getString("modo_preparo"));
        r.setTempoPreparo(rs.getInt("tempo_preparo"));
        r.setDificuldade(rs.getString("dificuldade"));
        r.setCategoria(rs.getString("categoria"));
        r.setTags(rs.getString("tags"));
        r.setNotaMedia(rs.getDouble("nota_media"));
        r.setTotalAvaliacoes(rs.getInt("total_avaliacoes"));

        Timestamp ts = rs.getTimestamp("criado_em");
        if (ts != null) r.setCriadoEm(ts.toLocalDateTime());

        return r;
    }
}
