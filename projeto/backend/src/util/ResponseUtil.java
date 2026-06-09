package util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Padroniza as respostas HTTP do servidor
// Sem essa classe cada controller teria que repetir setStatus + setContentType + write
public class ResponseUtil {

    // 200 OK - deu certo
    public static void ok(HttpServletResponse res, String json) throws IOException {
        res.setStatus(200);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(json);
    }

    // 201 Created - recurso criado com sucesso (cadastro, novo item, etc)
    public static void criado(HttpServletResponse res, String json) throws IOException {
        res.setStatus(201);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(json);
    }

    // 204 No Content - deu certo mas nao tem nada pra retornar (DELETE)
    public static void semConteudo(HttpServletResponse res) throws IOException {
        res.setStatus(204);
    }

    // 400 Bad Request - dados invalidos ou faltando na requisicao
    public static void requisicaoInvalida(HttpServletResponse res, String mensagem) throws IOException {
        res.setStatus(400);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(erro(mensagem));
    }

    // 401 Unauthorized - nao autenticado (token invalido ou ausente)
    public static void naoAutorizado(HttpServletResponse res, String mensagem) throws IOException {
        res.setStatus(401);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(erro(mensagem));
    }

    // 404 Not Found - recurso nao encontrado
    public static void naoEncontrado(HttpServletResponse res, String mensagem) throws IOException {
        res.setStatus(404);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(erro(mensagem));
    }

    // 500 Internal Server Error - erro inesperado no servidor
    public static void errointerno(HttpServletResponse res, String mensagem) throws IOException {
        res.setStatus(500);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(erro(mensagem));
    }

    // Monta um JSON de erro padrao
    // Ex: {"error":true,"message":"E-mail invalido"}
    public static String erro(String mensagem) {
        String msg = mensagem == null ? "" : mensagem.replace("\"", "\\\"");
        return "{\"error\":true,\"message\":\"" + msg + "\"}";
    }
}
