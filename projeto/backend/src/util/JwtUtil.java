package util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// Gera e valida tokens JWT manualmente, sem biblioteca externa
// Estrutura do JWT: header.payload.assinatura (cada parte em Base64URL)
public class JwtUtil {

    // Chave usada pra assinar o token - em producao coloque isso em variavel de ambiente
    private static final String CHAVE = "TemNaCozinhaSecretKey2024#TheFuriousFive";

    // Tempo de expiracao: 7 dias em milissegundos
    private static final long EXPIRACAO_MS = 7L * 24 * 60 * 60 * 1000;

    // Gera um token JWT pra um usuario
    // O token carrega o userId e a data de expiracao
    public static String gerarToken(int userId) {
        try {
            long agora     = System.currentTimeMillis();
            long expiracao = agora + EXPIRACAO_MS;

            // Header: informa o algoritmo
            String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");

            // Payload: os dados do token (JWT usa segundos, por isso /1000)
            String payload = base64Url(String.format(
                "{\"sub\":%d,\"iat\":%d,\"exp\":%d}",
                userId,
                agora / 1000,
                expiracao / 1000
            ));

            // Assinatura: garante que o token nao foi alterado
            String assinatura = assinar(header + "." + payload);

            return header + "." + payload + "." + assinatura;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }

    // Valida o token e retorna o userId
    // Lanca excecao se o token for invalido ou expirado
    public static int extrairUserId(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token nao informado");
        }

        // JWT tem 3 partes separadas por ponto
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new RuntimeException("Token invalido");
        }

        try {
            // Verifica se a assinatura confere
            String assinaturaEsperada = assinar(partes[0] + "." + partes[1]);
            if (!assinaturaEsperada.equals(partes[2])) {
                throw new RuntimeException("Assinatura invalida");
            }

            // Decodifica o payload
            String payloadJson = new String(
                Base64.getUrlDecoder().decode(partes[1]),
                StandardCharsets.UTF_8
            );

            // Verifica se nao expirou
            long exp = lerCampoLong(payloadJson, "exp");
            if (exp * 1000 < System.currentTimeMillis()) {
                throw new RuntimeException("Token expirado");
            }

            // Retorna o userId (campo "sub")
            return (int) lerCampoLong(payloadJson, "sub");

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar token", e);
        }
    }

    // Retorna 'true' se o token for valido, false se nao for
    public static boolean eValido(String token) {
        try {
            extrairUserId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Codifica uma 'string' em Base64URL (sem o = de padding)
    private static String base64Url(String texto) {
        return Base64.getUrlEncoder()
                     .withoutPadding()
                     .encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    // Assina os dados com HMAC-SHA256 usando a chave secreta
    private static String assinar(String dados) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
            CHAVE.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        ));
        byte[] bytes = mac.doFinal(dados.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Extrai um campo numerico do JSON do payload
    // Ex: {"sub":42,"exp":1700000} -> lerCampoLong(json, "sub") -> 42
    private static long lerCampoLong(String json, String campo) {
        String chave = "\"" + campo + "\":";
        int inicio = json.indexOf(chave);

        if (inicio == -1) throw new RuntimeException("Campo '" + campo + "' nao encontrado no token");

        inicio += chave.length();
        int fim = inicio;

        while (fim < json.length() && (Character.isDigit(json.charAt(fim)) || json.charAt(fim) == '-')) {
            fim++;
        }

        return Long.parseLong(json.substring(inicio, fim));
    }
}
