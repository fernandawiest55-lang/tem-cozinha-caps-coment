package model;

import java.time.LocalDateTime;

// Representa uma receita do sistema
// Cada campo corresponde a uma coluna da tabela "receita" no banco
public class Receita {

    private int id;
    private String titulo;
    private String descricao;
    private String ingredientesNecessarios; // armazenado como JSON: ["alho","tomate"]
    private String modoPreparo;
    private int tempoPreparo;               // em minutos
    private String dificuldade;             // "Facil", "Medio" ou "Dificil"
    private String categoria;               // "Almoco", "Jantar", etc
    private String tags;                    // separado por virgula: "brasileiro,tradicional"
    private double notaMedia;
    private int totalAvaliacoes;
    private LocalDateTime criadoEm;

    // Construtor vazio para o DAO
    public Receita() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getIngredientesNecessarios() { return ingredientesNecessarios; }
    public void setIngredientesNecessarios(String ingredientesNecessarios) {
        this.ingredientesNecessarios = ingredientesNecessarios;
    }

    public String getModoPreparo() { return modoPreparo; }
    public void setModoPreparo(String modoPreparo) { this.modoPreparo = modoPreparo; }

    public int getTempoPreparo() { return tempoPreparo; }
    public void setTempoPreparo(int tempoPreparo) { this.tempoPreparo = tempoPreparo; }

    public String getDificuldade() { return dificuldade; }
    public void setDificuldade(String dificuldade) { this.dificuldade = dificuldade; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(double notaMedia) { this.notaMedia = notaMedia; }

    public int getTotalAvaliacoes() { return totalAvaliacoes; }
    public void setTotalAvaliacoes(int totalAvaliacoes) { this.totalAvaliacoes = totalAvaliacoes; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    // Converte a receita para JSON
    public String toJson() {
        String ingredJson = ingredientesNecessarios != null ? ingredientesNecessarios : "[]";
        String tagsJson   = tagsParaJson(tags);

        return String.format(
            "{\"id\":%d,\"titulo\":\"%s\",\"descricao\":\"%s\"," +
            "\"ingredientesNecessarios\":%s,\"modoPreparo\":\"%s\"," +
            "\"tempoPreparo\":%d,\"dificuldade\":\"%s\"," +
            "\"categoria\":\"%s\",\"tags\":%s,\"nota\":%.1f,\"totalAvaliacoes\":%d}",
            id,
            escapar(titulo),
            escapar(descricao),
            ingredJson,
            escapar(modoPreparo),
            tempoPreparo,
            escapar(dificuldade),
            escapar(categoria),
            tagsJson,
            notaMedia,
            totalAvaliacoes
        );
    }

    // Converte "brasileiro,tradicional" para ["brasileiro","tradicional"]
    private String tagsParaJson(String tags) {
        if (tags == null || tags.isEmpty()) return "[]";

        String[] partes = tags.split(",");
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < partes.length; i++) {
            sb.append("\"").append(partes[i].trim()).append("\"");
            if (i < partes.length - 1) sb.append(",");
        }

        sb.append("]");
        return sb.toString();
    }

    private static String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
