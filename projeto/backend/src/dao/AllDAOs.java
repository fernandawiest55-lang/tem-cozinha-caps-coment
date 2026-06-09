package dao;

import config.FatorConexao;
import model.Avaliacao;
import model.Historico;
import model.ListaCompras;
import model.Receita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


//  FAVORITO DAO

class FavoritoDAO {

    // Lista as receitas favoritas de um usuario
    // JOIN com receita pra trazer os dados junto
    public List<Receita> listar(int usuarioId) throws SQLException {
        List<Receita> lista = new ArrayList<>();
        String sql = "SELECT r.* FROM receita r " +
                     "JOIN favorito f ON f.receita_id = r.id " +
                     "WHERE f.usuario_id = ? " +
                     "ORDER BY f.criado_em DESC";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, usuarioId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Receita r = new Receita();
            r.setId(rs.getInt("id"));
            r.setTitulo(rs.getString("titulo"));
            r.setDificuldade(rs.getString("dificuldade"));
            r.setTempoPreparo(rs.getInt("tempo_preparo"));
            r.setNotaMedia(rs.getDouble("nota_media"));
            lista.add(r);
        }

        rs.close();
        ps.close();
        conn.close();

        return lista;
    }

    // Favorita uma receita
    // ON CONFLICT DO NOTHING ignora se ja favoritou antes
    public void salvar(int usuarioId, int receitaId) throws SQLException {
        String sql = "INSERT INTO favorito (usuario_id, receita_id) " +
                     "VALUES (?, ?) ON CONFLICT DO NOTHING";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, usuarioId);
        ps.setInt(2, receitaId);
        ps.executeUpdate();

        ps.close();
        conn.close();
    }

    // Remove uma receita dos favoritos
    public void excluir(int usuarioId, int receitaId) throws SQLException {
        String sql = "DELETE FROM favorito WHERE usuario_id = ? AND receita_id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, usuarioId);
        ps.setInt(2, receitaId);
        ps.executeUpdate();

        ps.close();
        conn.close();
    }
}



//  HISTORICO DAO

class HistoricoDAO {

    // Lista o historico do usuario - JOIN com receita pra pegar o titulo
    public List<Historico> listar(int usuarioId) throws SQLException {
        List<Historico> lista = new ArrayList<>();
        String sql = "SELECT h.*, r.titulo AS titulo_receita, r.dificuldade, r.nota_media " +
                     "FROM historico h " +
                     "JOIN receita r ON r.id = h.receita_id " +
                     "WHERE h.usuario_id = ? " +
                     "ORDER BY h.data_acesso DESC " +
                     "LIMIT 50";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, usuarioId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Historico h = new Historico();
            h.setId(rs.getInt("id"));
            h.setUsuarioId(usuarioId);
            h.setReceitaId(rs.getInt("receita_id"));
            h.setTituloReceita(rs.getString("titulo_receita"));
            h.setDificuldade(rs.getString("dificuldade"));
            h.setNota(rs.getDouble("nota_media"));

            Timestamp ts = rs.getTimestamp("data_acesso");
            if (ts != null) h.setDataAcesso(ts.toLocalDateTime());

            lista.add(h);
        }

        rs.close();
        ps.close();
        conn.close();

        return lista;
    }

    // Registra que o usuario acessou uma receita
    public void salvar(int usuarioId, int receitaId) throws SQLException {
        String sql = "INSERT INTO historico (usuario_id, receita_id) VALUES (?, ?)";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, usuarioId);
        ps.setInt(2, receitaId);
        ps.executeUpdate();

        ps.close();
        conn.close();
    }
}



//  AVALIACAO DAO

class AvaliacaoDAO {

    // Salva uma avaliacao
    // ON CONFLICT DO UPDATE: se o usuario ja avaliou essa receita, atualiza a nota
    public void salvar(Avaliacao avaliacao) throws SQLException {
        String sql = "INSERT INTO avaliacao (usuario_id, receita_id, nota, comentario) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (usuario_id, receita_id) " +
                     "DO UPDATE SET nota = EXCLUDED.nota, comentario = EXCLUDED.comentario";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, avaliacao.getUsuarioId());
        ps.setInt(2, avaliacao.getReceitaId());
        ps.setInt(3, avaliacao.getNota());
        ps.setString(4, avaliacao.getComentario());

        ps.executeUpdate();
        ps.close();
        conn.close();
    }
}



//  LISTA COMPRAS DAO

class ListaComprasDAO {

    // Lista os itens do usuario ordenados por categoria e status
    public List<ListaCompras> listar(int usuarioId) throws SQLException {
        List<ListaCompras> lista = new ArrayList<>();
        String sql = "SELECT * FROM lista_compras " +
                     "WHERE usuario_id = ? " +
                     "ORDER BY categoria, comprado, nome";

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

    // Adiciona um item manualmente na lista
    public ListaCompras salvar(ListaCompras item) throws SQLException {
        String sql = "INSERT INTO lista_compras (usuario_id, nome, quantidade, categoria) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, item.getUsuarioId());
        ps.setString(2, item.getNome());
        ps.setString(3, item.getQuantidade());
        ps.setString(4, item.getCategoria() != null ? item.getCategoria() : "Geral");

        ResultSet rs = ps.executeQuery();
        if (rs.next()) item.setId(rs.getInt("id"));

        rs.close();
        ps.close();
        conn.close();

        return item;
    }

    // Marca ou desmarca um item como comprado
    public void atualizarComprado(int id, int usuarioId, boolean comprado) throws SQLException {
        String sql = "UPDATE lista_compras SET comprado = ? WHERE id = ? AND usuario_id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setBoolean(1, comprado);
        ps.setInt(2, id);
        ps.setInt(3, usuarioId);

        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // Remove um item da lista
    public void excluir(int id, int usuarioId) throws SQLException {
        String sql = "DELETE FROM lista_compras WHERE id = ? AND usuario_id = ?";

        Connection conn = FatorConexao.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, id);
        ps.setInt(2, usuarioId);

        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // Adiciona os ingredientes de uma receita na lista de compras
    // ON CONFLICT DO NOTHING evita duplicatas
    public void adicionarDeReceita(int usuarioId, List<String> ingredientes) throws SQLException {
        String sql = "INSERT INTO lista_compras (usuario_id, nome, categoria) " +
                     "VALUES (?, ?, 'Receita') ON CONFLICT DO NOTHING";

        Connection conn = FatorConexao.getConnection();

        for (String ingrediente : ingredientes) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            ps.setString(2, ingrediente);
            ps.executeUpdate();
            ps.close();
        }

        conn.close();
    }

    // Converte uma linha do ResultSet em um objeto ListaCompras
    private ListaCompras mapear(ResultSet rs) throws SQLException {
        ListaCompras item = new ListaCompras();

        item.setId(rs.getInt("id"));
        item.setUsuarioId(rs.getInt("usuario_id"));
        item.setNome(rs.getString("nome"));
        item.setQuantidade(rs.getString("quantidade"));
        item.setCategoria(rs.getString("categoria"));
        item.setComprado(rs.getBoolean("comprado"));

        Timestamp ts = rs.getTimestamp("criado_em");
        if (ts != null) item.setCriadoEm(ts.toLocalDateTime());

        return item;
    }
}
