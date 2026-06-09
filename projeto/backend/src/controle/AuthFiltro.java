package controle;

import util.JwtUtil;
import util.ResponseUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// ============================================================
//  CORS FILTER
//  Libera o frontend pra fazer requisicoes pra API
//
//  Por padrao o navegador bloqueia requisicoes entre origens
//  diferentes (ex: localhost:3000 -> localhost:8080)
//  Esses headers liberam isso
// ============================================================
@WebFilter("/*")
class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Libera qualquer origem - em producao coloque o dominio real
        response.setHeader("Access-Control-Allow-Origin",  "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age",       "3600");

        // Preflight: o navegador manda um OPTIONS antes de requisicoes POST/PUT
        // Respondemos 200 e retornamos sem ir ao controller
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(200);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override public void init(FilterConfig fc) {}
    @Override public void destroy() {}
}


// ============================================================
//  AUTH FILTER
//  Verifica o token JWT em todas as rotas protegidas
//
//  Intercepta toda requisicao pra /api/* e verifica
//  se tem um token JWT valido no header Authorization
//  Rotas publicas (login, cadastro) passam direto
// ============================================================
@WebFilter("/api/*")
class AuthFiltro implements Filter {

    // Rotas que nao precisam de token
    private static final String[] ROTAS_PUBLICAS = {
        "/api/usuarios/login",
        "/api/usuarios/cadastro",
        "/api/usuarios/recuperar-senha"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String caminho = request.getServletPath();

        // Verifica se e rota publica - se for, passa direto
        for (String rotaPublica : ROTAS_PUBLICAS) {
            if (caminho.startsWith(rotaPublica)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // Le o token do header Authorization: Bearer <token>
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth == null || !headerAuth.startsWith("Bearer ")) {
            ResponseUtil.naoAutorizado(response, "Token nao informado");
            return;
        }

        // Remove o "Bearer " (7 caracteres) pra pegar so o token
        String token = headerAuth.substring(7);

        try {
            // Valida o token e extrai o userId
            int userId = JwtUtil.extrairUserId(token);

            // Injeta o userId na requisicao pra os controllers usarem
            // Nos controllers: int userId = (int) req.getAttribute("userId")
            request.setAttribute("userId", userId);

            chain.doFilter(req, res);

        } catch (Exception e) {
            ResponseUtil.naoAutorizado(response, "Token invalido ou expirado");
        }
    }

    @Override public void init(FilterConfig fc) {}
    @Override public void destroy() {}
}
