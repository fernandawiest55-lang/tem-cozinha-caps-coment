package util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Le e 'parseia' o corpo JSON das requisicoes HTTP
// Funciona so pra JSON plano: {"nome":"Joao","email":"joao@email.com"}
public class JsonParser {

    // Le o body da requisicao e retorna um Map com os campos
    public static Map<String, String> parse(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String linha;

        while ((linha = reader.readLine()) != null) {
            sb.append(linha);
        }

        return parseString(sb.toString().trim());
    }

    // Converte uma string JSON em Map<String, String>
    // Ex: {"nome":"Joao","ativo":true} -> {nome=Joao, ativo=true}
    public static Map<String, String> parseString(String json) {
        Map<String, String> mapa = new HashMap<>();

        if (json == null || json.isEmpty()) return mapa;

        // Remove as chaves { e }
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))   json = json.substring(0, json.length() - 1);

        // Divide por virgula, ignorando virgulas dentro de aspas
        String[] pares = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String par : pares) {
            int doisPontos = par.indexOf(':');
            if (doisPontos == -1) continue;

            // Remove aspas da chave
            String chave = par.substring(0, doisPontos).trim().replaceAll("\"", "");

            // Le o valor
            String valor = par.substring(doisPontos + 1).trim();

            // Remove aspas do valor se for string
            if (valor.startsWith("\"") && valor.endsWith("\"")) {
                valor = valor.substring(1, valor.length() - 1);
            }

            // Desfaz escapes
            valor = valor.replace("\\\"", "\"")
                         .replace("\\n", "\n")
                         .replace("\\\\", "\\");

            mapa.put(chave, valor);
        }

        return mapa;
    }

    // Le um campo booleano do mapa
    public static boolean parseBoolean(Map<String, String> mapa, String chave, boolean padrao) {
        if (!mapa.containsKey(chave)) return padrao;
        return "true".equalsIgnoreCase(mapa.get(chave));
    }

    // Le um campo inteiro do mapa
    public static int parseInt(Map<String, String> mapa, String chave, int padrao) {
        try {
            return Integer.parseInt(mapa.getOrDefault(chave, String.valueOf(padrao)));
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
