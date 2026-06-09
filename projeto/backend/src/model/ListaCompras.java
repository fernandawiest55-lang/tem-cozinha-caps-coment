package model;

import java.time.LocalDateTime;

// Representa um item da lista de compras do usuario
public class ListaCompras {

    private int id;
    private int usuarioId;
    private String nome;
    private String quantidade;  // String pq pode ser "2 ducias", "1 maco", etc
    private String categoria;   // "Geral", "Receita", "Verduras", etc
    private boolean comprado;
    private LocalDateTime criadoEm;

    public ListaCompras() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getQuantidade() { return quantidade; }
    public void setQuantidade(String quantidade) { this.quantidade = quantidade; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isComprado() { return comprado; }
    public void setComprado(boolean comprado) { this.comprado = comprado; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"nome\":\"%s\",\"quantidade\":\"%s\",\"categoria\":\"%s\",\"comprado\":%b}",
            id,
            escapar(nome),
            escapar(quantidade),
            escapar(categoria),
            comprado
        );
    }

    private static String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
