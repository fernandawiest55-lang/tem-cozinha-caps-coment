package controle;

import dao.AvaliacaoDAO;
import dao.FavoritoDAO;
import dao.HistoricoDAO;
import dao.ListaComprasDAO;
import dao.ReceitaDAO;
import model.Avaliacao;
import model.Historico;
import model.ListaCompras;
import model.Receita;
import util.JsonParser;
import util.ResponseUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// ============================================================
//  FAVORITO CONTROLLER
//  GET    /api/favoritos      -> lista favoritos do usuario
//  POST   /api/favoritos      -> favorita uma receita
//  DELETE /api/favoritos/{id} -> desfavorita uma receita
// ============================================================
@WebServlet("/api/favoritos/*")
class FavoritoController extends HttpServlet {

    private FavoritoDAO favoritoDAO = new FavoritoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");

        try {
            List<Receita> lista = favoritoDAO.listar(userId);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                sb.append(lista.get(i).toJson());
                if (i < lista.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.ok(res, sb.toString());

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    // Corpo esperado: {"receitaId": 42}
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");

        try {
            Map<String, String> corpo = JsonParser.parse(req);
            int receitaId = JsonParser.parseInt(corpo, "receitaId", 0);

            if (receitaId == 0) {
                ResponseUtil.requisicaoInvalida(res, "receitaId obrigatorio");
                return;
            }

            favoritoDAO.salvar(userId, receitaId);
            ResponseUtil.criado(res, "{\"message\":\"Favoritado\"}");

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    // DELETE /api/favoritos/42 -> remove a receita 42 dos favoritos
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");
        String caminho = req.getPathInfo();

        if (caminho == null || caminho.length() < 2) {
            ResponseUtil.requisicaoInvalida(res, "ID obrigatorio");
            return;
        }

        try {
            int receitaId = Integer.parseInt(caminho.substring(1));
            favoritoDAO.excluir(userId, receitaId);
            ResponseUtil.semConteudo(res);

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }
}


// ============================================================
//  HISTORICO CONTROLLER
//  GET /api/historico -> lista o historico de receitas vistas
// ============================================================
@WebServlet("/api/historico/*")
class HistoricoController extends HttpServlet {

    private HistoricoDAO historicoDAO = new HistoricoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");

        try {
            List<Historico> lista = historicoDAO.listar(userId);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                sb.append(lista.get(i).toJson());
                if (i < lista.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.ok(res, sb.toString());

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }
}


// ============================================================
//  AVALIACAO CONTROLLER
//  POST /api/avaliacoes -> salva avaliacao com nota e comentario
// ============================================================
@WebServlet("/api/avaliacoes/*")
class AvaliacaoController extends HttpServlet {

    private AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
    private ReceitaDAO   receitaDAO   = new ReceitaDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");

        try {
            Map<String, String> corpo = JsonParser.parse(req);

            int receitaId = JsonParser.parseInt(corpo, "receitaId", 0);
            int nota      = JsonParser.parseInt(corpo, "nota", 0);

            if (receitaId == 0 || nota < 1 || nota > 5) {
                ResponseUtil.requisicaoInvalida(res, "receitaId e nota (1-5) sao obrigatorios");
                return;
            }

            Avaliacao avaliacao = new Avaliacao();
            avaliacao.setUsuarioId(userId);
            avaliacao.setReceitaId(receitaId);
            avaliacao.setNota(nota);
            avaliacao.setComentario(corpo.getOrDefault("comentario", ""));

            avaliacaoDAO.salvar(avaliacao);

            // Recalcula a nota media da receita apos a nova avaliacao
            receitaDAO.atualizarNota(receitaId);

            ResponseUtil.criado(res, "{\"message\":\"Avaliacao salva\"}");

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }
}


// ============================================================
//  LISTA COMPRAS CONTROLLER
//  GET    /api/lista-compras              -> lista os itens
//  POST   /api/lista-compras             -> adiciona item manual
//  POST   /api/lista-compras/receita/{id} -> adiciona ingredientes de receita
//  PUT    /api/lista-compras/{id}         -> marca/desmarca como comprado
//  DELETE /api/lista-compras/{id}         -> remove item
// ============================================================
@WebServlet("/api/lista-compras/*")
class ListaComprasController extends HttpServlet {

    private ListaComprasDAO listaDAO   = new ListaComprasDAO();
    private ReceitaDAO      receitaDAO = new ReceitaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");

        try {
            List<ListaCompras> lista = listaDAO.listar(userId);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                sb.append(lista.get(i).toJson());
                if (i < lista.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.ok(res, sb.toString());

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int    userId = (int) req.getAttribute("userId");
        String caminho = req.getPathInfo();

        try {
            // Rota especial: adiciona todos os ingredientes de uma receita
            // POST /api/lista-compras/receita/42
            if (caminho != null && caminho.startsWith("/receita/")) {
                int receitaId = Integer.parseInt(caminho.substring(9));

                Receita receita = receitaDAO.buscarPorId(receitaId);
                if (receita == null) {
                    ResponseUtil.naoEncontrado(res, "Receita nao encontrada");
                    return;
                }

                List<String> ingredientes = extrairLista(receita.getIngredientesNecessarios());
                listaDAO.adicionarDeReceita(userId, ingredientes);

                ResponseUtil.criado(res, "{\"message\":\"Ingredientes adicionados\",\"quantidade\":" + ingredientes.size() + "}");
                return;
            }

            // Rota normal: adiciona um item manualmente
            Map<String, String> corpo = JsonParser.parse(req);
            String nome = corpo.get("nome");

            if (nome == null || nome.trim().isEmpty()) {
                ResponseUtil.requisicaoInvalida(res, "Nome obrigatorio");
                return;
            }

            ListaCompras item = new ListaCompras();
            item.setUsuarioId(userId);
            item.setNome(nome.trim());
            item.setQuantidade(corpo.getOrDefault("quantidade", ""));
            item.setCategoria(corpo.getOrDefault("categoria", "Geral"));

            listaDAO.salvar(item);
            ResponseUtil.criado(res, item.toJson());

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    // Marca ou desmarca como comprado
    // Corpo: {"comprado": true}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");
        int id = pegarId(req);

        if (id == -1) {
            ResponseUtil.requisicaoInvalida(res, "ID invalido");
            return;
        }

        try {
            Map<String, String> corpo = JsonParser.parse(req);
            boolean comprado = JsonParser.parseBoolean(corpo, "comprado", false);

            listaDAO.atualizarComprado(id, userId, comprado);
            ResponseUtil.ok(res, "{\"message\":\"Atualizado\"}");

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");
        int id = pegarId(req);

        if (id == -1) {
            ResponseUtil.requisicaoInvalida(res, "ID invalido");
            return;
        }

        try {
            listaDAO.excluir(id, userId);
            ResponseUtil.semConteudo(res);

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    // Extrai o ID do caminho: /42 -> 42
    private int pegarId(HttpServletRequest req) {
        String caminho = req.getPathInfo();
        if (caminho == null || caminho.equals("/")) return -1;

        try {
            return Integer.parseInt(caminho.substring(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Extrai strings de um array JSON: ["tomate","cebola"] -> [tomate, cebola]
    private List<String> extrairLista(String json) {
        List<String> lista = new ArrayList<>();
        if (json == null || json.isEmpty()) return lista;

        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]"))   json = json.substring(0, json.length() - 1);

        for (String item : json.split(",")) {
            String limpo = item.trim().replaceAll("^\"|\"$", "");
            if (!limpo.isEmpty()) lista.add(limpo);
        }

        return lista;
    }
}
