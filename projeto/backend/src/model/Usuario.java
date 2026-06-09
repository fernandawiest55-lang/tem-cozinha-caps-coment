package model;

import java.time.LocalDateTime;

// Representa um usuario do sistema
// Cada campo corresponde a uma coluna da tabela "usuario" no banco
public class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senha;        // armazenada como hash, nunca texto puro
    private String restricoes;   // ex: "gluten,lactose"
    private String dieta;        // ex: "vegetariano"
    private boolean ativo;
    private LocalDateTime criadoEm;

    // Construtor vazio - o DAO usa esse pra criar objetos
    public Usuario() {}

    // Construtor rapido usado no cadastro
    public Usuario(String nome, String email, String senha) {
        this.nome  = nome;
        this.email = email;
        this.senha = senha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getRestricoes() { return restricoes; }
    public void setRestricoes(String restricoes) { this.restricoes = restricoes; }

    public String getDieta() { return dieta; }
    public void setDieta(String dieta) { this.dieta = dieta; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    // Converte o usuario pra JSON manualmente (sem biblioteca)
    // A senha nao e incluida por seguranca
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"nome\":\"%s\",\"email\":\"%s\",\"restricoes\":\"%s\",\"dieta\":\"%s\"}",
            id,
            escapar(nome),
            escapar(email),
            restricoes != null ? escapar(restricoes) : "",
            dieta      != null ? escapar(dieta)      : ""
        );
    }

    // Escapa aspas dentro de strings pra nao quebrar o JSON
    private static String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
