package model;

import java.time.LocalDateTime;

// Representa um ingrediente da despensa de um usuario
// Cada ingrediente pertence a um usuario (campo usuarioId)
public class Ingrediente {

    private int id;
    private int usuarioId;      // chave estrangeira para a tabela usuario
    private String nome;
    private double quantidade;
    private String unidade;     // ex: "kg", "unidades", "ml"
    private String local;       // ex: "Geladeira", "Armario"
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Ingrediente() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"nome\":\"%s\",\"quantidade\":%.2f,\"unidade\":\"%s\",\"local\":\"%s\"}",
            id,
            escapar(nome),
            quantidade,
            escapar(unidade),
            escapar(local)
        );
    }

    private static String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
