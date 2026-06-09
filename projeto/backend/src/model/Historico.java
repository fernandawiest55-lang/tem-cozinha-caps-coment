package model;

import java.time.LocalDateTime;

// Representa um registro de acesso a uma receita
// Criado automaticamente quando o usuario abre uma receita
public class Historico {

    private int id;
    private int usuarioId;
    private int receitaId;
    private String tituloReceita;
    private String dificuldade;
    private double nota;
    private LocalDateTime dataAcesso;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getReceitaId() { return receitaId; }
    public void setReceitaId(int receitaId) { this.receitaId = receitaId; }

    public String getTituloReceita() { return tituloReceita; }
    public void setTituloReceita(String tituloReceita) { this.tituloReceita = tituloReceita; }

    public String getDificuldade() { return dificuldade; }
    public void setDificuldade(String dificuldade) { this.dificuldade = dificuldade; }

    public double getNota() { return nota; }
    public void setNota(double nota) { this.nota = nota; }

    public LocalDateTime getDataAcesso() { return dataAcesso; }
    public void setDataAcesso(LocalDateTime dataAcesso) { this.dataAcesso = dataAcesso; }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"receitaId\":%d,\"tituloReceita\":\"%s\",\"dificuldade\":\"%s\",\"nota\":%.1f,\"dataAcesso\":\"%s\"}",
            id,
            receitaId,
            tituloReceita != null ? tituloReceita.replace("\"", "\\\"") : "",
            dificuldade   != null ? dificuldade : "",
            nota,
            dataAcesso    != null ? dataAcesso.toString() : ""
        );
    }
}
