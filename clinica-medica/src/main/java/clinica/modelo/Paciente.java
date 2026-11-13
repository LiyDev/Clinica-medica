package clinica.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Paciente {

    private int id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String telefone;

    public Paciente(String nome, String cpf, LocalDate dataNascimento, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
    }

    public Paciente(int id, String nome, String cpf, LocalDate dataNascimento, String telefone) {
        this(nome, cpf, dataNascimento, telefone);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | CPF: %s | Nascimento: %s | Telefone: %s",
                id, nome, cpf, dataNascimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), telefone);
    }
}
