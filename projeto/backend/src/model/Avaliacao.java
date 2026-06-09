package model;

import java.time.LocalDateTime;

// Representa a avaliacao de um usuario para uma receita
// Nota vai de 1 a 5
public class Avaliacao {

    private int id;
    private int usuarioId;
    private int receitaId;
    private int nota;
    private String comentario;
    private LocalDateTime criadoEm;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getReceitaId() { return receitaId; }
    public void setReceitaId(int receitaId) { this.receitaId = receitaId; }

    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"receitaId\":%d,\"nota\":%d,\"comentario\":\"%s\"}",
            id, receitaId, nota,
            comentario != null ? comentario.replace("\"", "\\\"") : ""
        );
    }
}
