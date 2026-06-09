package controle;

import dao.UsuarioDAO;
import model.Usuario;
import util.BCryptUtil;
import util.JwtUtil;
import util.JsonParser;
import util.ResponseUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

// Cuida de todas as rotas de usuario:
// POST /api/usuarios/login           -> fazer login
// POST /api/usuarios/cadastro        -> criar conta
// POST /api/usuarios/recuperar-senha -> solicitar recuperacao
// GET  /api/usuarios/me              -> buscar dados do usuario logado
// PUT  /api/usuarios/me              -> atualizar nome e email
// PUT  /api/usuarios/me/senha        -> alterar senha
// PUT  /api/usuarios/me/perfil-alimentar -> atualizar restricoes e dieta
@WebServlet("/api/usuarios/*")
public class UsuarioController extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String caminho = req.getPathInfo();
        if (caminho == null) caminho = "/";

        Map<String, String> corpo = JsonParser.parse(req);

        try {
            if ("/login".equals(caminho)) {
                fazerLogin(corpo, res);

            } else if ("/cadastro".equals(caminho)) {
                fazerCadastro(corpo, res);

            } else if ("/recuperar-senha".equals(caminho)) {
                recuperarSenha(corpo, res);

            } else {
                ResponseUtil.naoEncontrado(res, "Rota nao encontrada");
            }

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String caminho = req.getPathInfo();

        if ("/me".equals(caminho)) {
            try {
                int userId = (int) req.getAttribute("userId");
                Usuario usuario = usuarioDAO.buscarPorId(userId);

                if (usuario == null) {
                    ResponseUtil.naoEncontrado(res, "Usuario nao encontrado");
                    return;
                }

                ResponseUtil.ok(res, usuario.toJson());

            } catch (Exception e) {
                ResponseUtil.errointerno(res, e.getMessage());
            }
        } else {
            ResponseUtil.naoEncontrado(res, "Rota nao encontrada");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String caminho = req.getPathInfo();
        Map<String, String> corpo = JsonParser.parse(req);
        int userId = (int) req.getAttribute("userId");

        try {
            if ("/me".equals(caminho)) {
                // Atualiza nome e/ou email
                Usuario usuario = usuarioDAO.buscarPorId(userId);

                if (usuario == null) {
                    ResponseUtil.naoEncontrado(res, "Usuario nao encontrado");
                    return;
                }

                if (corpo.containsKey("nome"))  usuario.setNome(corpo.get("nome"));
                if (corpo.containsKey("email")) usuario.setEmail(corpo.get("email"));

                usuarioDAO.atualizar(usuario);
                ResponseUtil.ok(res, usuario.toJson());

            } else if ("/me/senha".equals(caminho)) {
                alterarSenha(userId, corpo, res);

            } else if ("/me/perfil-alimentar".equals(caminho)) {
                String restricoes = corpo.getOrDefault("restricoes", "");
                String dieta      = corpo.getOrDefault("dieta", "");

                usuarioDAO.atualizarPerfilAlimentar(userId, restricoes, dieta);
                ResponseUtil.ok(res, "{\"message\":\"Preferencias atualizadas\"}");

            } else {
                ResponseUtil.naoEncontrado(res, "Rota nao encontrada");
            }

        } catch (Exception e) {
            ResponseUtil.errointerno(res, e.getMessage());
        }
    }

    // Faz login: verifica email, compara a senha com o hash e gera o token
    private void fazerLogin(Map<String, String> corpo, HttpServletResponse res) throws Exception {
        String email = corpo.get("email");
        String senha  = corpo.get("senha");

        if (email == null || senha == null) {
            ResponseUtil.requisicaoInvalida(res, "E-mail e senha sao obrigatorios");
            return;
        }

        Usuario usuario = usuarioDAO.buscarPorEmail(email);

        // Mensagem vaga de proposito: nao revela se o email existe ou nao
        if (usuario == null || !BCryptUtil.verificar(senha, usuario.getSenha())) {
            ResponseUtil.naoAutorizado(res, "E-mail ou senha invalidos");
            return;
        }

        String token = JwtUtil.gerarToken(usuario.getId());

        ResponseUtil.ok(res, String.format(
            "{\"token\":\"%s\",\"usuario\":%s}",
            token,
            usuario.toJson()
        ));
    }

    // Cria uma nova conta
    // Verifica se o email ja existe antes de salvar
    private void fazerCadastro(Map<String, String> corpo, HttpServletResponse res) throws Exception {
        String nome  = corpo.get("nome");
        String email = corpo.get("email");
        String senha  = corpo.get("senha");

        if (nome == null || email == null || senha == null) {
            ResponseUtil.requisicaoInvalida(res, "Nome, e-mail e senha sao obrigatorios");
            return;
        }

        // Verifica se o email ja esta cadastrado
        if (usuarioDAO.buscarPorEmail(email) != null) {
            ResponseUtil.requisicaoInvalida(res, "E-mail ja cadastrado");
            return;
        }

        // Salva com a senha ja em hash
        Usuario usuario = new Usuario(nome, email, BCryptUtil.hash(senha));
        usuario.setRestricoes(corpo.getOrDefault("restricoes", ""));
        usuario.setDieta(corpo.getOrDefault("dieta", ""));

        usuarioDAO.salvar(usuario);

        String token = JwtUtil.gerarToken(usuario.getId());

        ResponseUtil.criado(res, String.format(
            "{\"token\":\"%s\",\"usuario\":%s}",
            token,
            usuario.toJson()
        ));
    }

    // Troca a senha: verifica a senha atual antes de mudar
    private void alterarSenha(int userId, Map<String, String> corpo, HttpServletResponse res) throws Exception {
        String senhaAtual = corpo.get("senhaAtual");
        String novaSenha  = corpo.get("novaSenha");

        if (senhaAtual == null || novaSenha == null) {
            ResponseUtil.requisicaoInvalida(res, "Informe senhaAtual e novaSenha");
            return;
        }

        Usuario usuario = usuarioDAO.buscarPorId(userId);

        if (!BCryptUtil.verificar(senhaAtual, usuario.getSenha())) {
            ResponseUtil.requisicaoInvalida(res, "Senha atual incorreta");
            return;
        }

        usuarioDAO.atualizarSenha(userId, BCryptUtil.hash(novaSenha));
        ResponseUtil.ok(res, "{\"message\":\"Senha alterada com sucesso\"}");
    }

    // Simula o envio de email de recuperacao
    // A resposta e sempre a mesma pra nao revelar se o email existe
    private void recuperarSenha(Map<String, String> corpo, HttpServletResponse res) throws Exception {
        String email = corpo.get("email");

        if (email == null) {
            ResponseUtil.requisicaoInvalida(res, "E-mail obrigatorio");
            return;
        }

        ResponseUtil.ok(res, "{\"message\":\"Se o e-mail estiver cadastrado, voce recebera as instrucoes.\"}");
    }
}
