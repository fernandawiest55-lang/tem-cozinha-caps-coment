package controle;

import dao.IngredienteDAO;
import model.Ingrediente;
import util.JsonParser;
import util.ResponseUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// CRUD completo de ingredientes:
// GET    /api/ingredientes       -> lista ingredientes do usuario
// POST   /api/ingredientes       -> adiciona ingrediente
// PUT    /api/ingredientes/{id}  -> edita ingrediente
// DELETE /api/ingredientes/{id}  -> remove ingrediente
@WebServlet("/api/ingredientes/*")
public class IngredienteControle extends HttpServlet {

    private IngredienteDAO ingredienteDAO = new IngredienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getAttribute("userId");

        try {
            List<Ingrediente> lista = ingredienteDAO.listar(userId);

            // Monta o array JSON manualmente
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
        int userId = (int) req.getAttribute("userId");

        try {
            Map<String, String> corpo = JsonParser.parse(req);
            String nome = corpo.get("nome");

            if (nome == null || nome.trim().isEmpty()) {
                ResponseUtil.requisicaoInvalida(res, "Nome e obrigatorio");
                return;
            }

            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setUsuarioId(userId);
            ingrediente.setNome(nome.trim());
            ingrediente.setQuantidade(JsonParser.parseInt(corpo, "quantidade", 1));
            ingrediente.setUnidade(corpo.getOrDefault("unidade", "unidades"));
            ingrediente.setLocal(corpo.getOrDefault("local", "Geladeira"));

            ingredienteDAO.salvar(ingrediente);

            ResponseUtil.criado(res, ingrediente.toJson());

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

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

            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setId(id);
            ingrediente.setUsuarioId(userId);
            ingrediente.setNome(corpo.getOrDefault("nome", ""));
            ingrediente.setQuantidade(JsonParser.parseInt(corpo, "quantidade", 1));
            ingrediente.setUnidade(corpo.getOrDefault("unidade", "unidades"));
            ingrediente.setLocal(corpo.getOrDefault("local", "Geladeira"));

            ingredienteDAO.atualizar(ingrediente);
            ResponseUtil.ok(res, ingrediente.toJson());

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
            ingredienteDAO.excluir(id, userId);
            ResponseUtil.semConteudo(res);

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    // Extrai o ID da URL: /api/ingredientes/42 -> 42
    // Retorna -1 se nao tiver ID ou se nao for numero
    private int pegarId(HttpServletRequest req) {
        String caminho = req.getPathInfo();

        if (caminho == null || caminho.equals("/")) return -1;

        try {
            return Integer.parseInt(caminho.substring(1)); // remove a / inicial
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
