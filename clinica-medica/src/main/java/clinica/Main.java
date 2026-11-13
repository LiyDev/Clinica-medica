package clinica;

import clinica.dao.ConsultaDAO;
import clinica.dao.DaoException;
import clinica.dao.MedicoDAO;
import clinica.dao.PacienteDAO;
import clinica.modelo.Consulta;
import clinica.modelo.Medico;
import clinica.modelo.Paciente;
import clinica.modelo.StatusConsulta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final PacienteDAO PACIENTES = new PacienteDAO();
    private static final MedicoDAO MEDICOS = new MedicoDAO();
    private static final ConsultaDAO CONSULTAS = new ConsultaDAO();

    public static void main(String[] args) {
        System.out.println("=== CLINICA MEDICA ===");

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opcao: ");

            try {
                executar(opcao);
            } catch (DaoException e) {
                System.out.println("\nErro: " + e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("Detalhe: " + e.getCause().getMessage());
                }
            }
        } while (opcao != 0);

        SCANNER.close();
    }

    private static void executar(int opcao) {
        switch (opcao) {
            case 1:
                cadastrarPaciente();
                break;
            case 2:
                listar(PACIENTES.listar());
                break;
            case 3:
                cadastrarMedico();
                break;
            case 4:
                listar(MEDICOS.listar());
                break;
            case 5:
                agendarConsulta();
                break;
            case 6:
                listar(CONSULTAS.listarPorPaciente(lerInteiro("ID do paciente: ")));
                break;
            case 7:
                listar(CONSULTAS.listarPorMedico(lerInteiro("ID do medico: ")));
                break;
            case 8:
                mudarStatus(StatusConsulta.CANCELADA);
                break;
            case 9:
                mudarStatus(StatusConsulta.REALIZADA);
                break;
            case 0:
                System.out.println("\nAte logo!");
                break;
            default:
                System.out.println("\nOpcao invalida.");
        }
    }

    private static void exibirMenu() {
        System.out.println("\n----- MENU -----");
        System.out.println("1. Cadastrar paciente");
        System.out.println("2. Listar pacientes");
        System.out.println("3. Cadastrar medico");
        System.out.println("4. Listar medicos");
        System.out.println("5. Agendar consulta");
        System.out.println("6. Consultas de um paciente");
        System.out.println("7. Consultas de um medico");
        System.out.println("8. Cancelar consulta");
        System.out.println("9. Marcar consulta como realizada");
        System.out.println("0. Sair");
    }

    private static void cadastrarPaciente() {
        String nome = lerTexto("Nome: ");
        String cpf = lerTexto("CPF (somente numeros): ");
        if (!cpf.matches("\\d{11}")) {
            System.out.println("CPF invalido: informe 11 numeros.");
            return;
        }
        LocalDate nascimento = lerData("Data de nascimento (dd/mm/aaaa): ");
        if (nascimento == null) {
            return;
        }
        String telefone = lerTexto("Telefone: ");

        Paciente paciente = new Paciente(nome, cpf, nascimento, telefone);
        PACIENTES.inserir(paciente);
        System.out.println("Paciente cadastrado com ID " + paciente.getId());
    }

    private static void cadastrarMedico() {
        String nome = lerTexto("Nome: ");
        String crm = lerTexto("CRM: ");
        String especialidade = lerTexto("Especialidade: ");

        Medico medico = new Medico(nome, crm, especialidade);
        MEDICOS.inserir(medico);
        System.out.println("Medico cadastrado com ID " + medico.getId());
    }

    private static void agendarConsulta() {
        Optional<Paciente> paciente = PACIENTES.buscarPorId(lerInteiro("ID do paciente: "));
        if (paciente.isEmpty()) {
            System.out.println("Paciente nao encontrado.");
            return;
        }

        Optional<Medico> medico = MEDICOS.buscarPorId(lerInteiro("ID do medico: "));
        if (medico.isEmpty()) {
            System.out.println("Medico nao encontrado.");
            return;
        }

        LocalDateTime dataHora = lerDataHora("Data e hora (dd/mm/aaaa hh:mm): ");
        if (dataHora == null) {
            return;
        }
        if (dataHora.isBefore(LocalDateTime.now())) {
            System.out.println("Nao e possivel agendar no passado.");
            return;
        }
        if (CONSULTAS.horarioOcupado(medico.get().getId(), dataHora)) {
            System.out.println("Esse medico ja tem consulta nesse horario.");
            return;
        }

        String motivo = lerTexto("Motivo: ");
        Consulta consulta = new Consulta(paciente.get(), medico.get(), dataHora, motivo);
        CONSULTAS.agendar(consulta);
        System.out.println("Consulta agendada com ID " + consulta.getId());
    }

    private static void mudarStatus(StatusConsulta novoStatus) {
        int id = lerInteiro("ID da consulta: ");
        Optional<Consulta> consulta = CONSULTAS.buscarPorId(id);
        if (consulta.isEmpty()) {
            System.out.println("Consulta nao encontrada.");
            return;
        }
        if (consulta.get().getStatus() != StatusConsulta.AGENDADA) {
            System.out.println("So e possivel alterar consultas que estao agendadas.");
            return;
        }
        CONSULTAS.atualizarStatus(id, novoStatus);
        System.out.println("Consulta marcada como " + novoStatus + ".");
    }

    private static <T> void listar(List<T> itens) {
        if (itens.isEmpty()) {
            System.out.println("\nNenhum registro encontrado.");
            return;
        }
        System.out.println();
        for (T item : itens) {
            System.out.println(item);
        }
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return SCANNER.nextLine().trim();
    }

    private static int lerInteiro(String mensagem) {
        System.out.print(mensagem);
        try {
            return Integer.parseInt(SCANNER.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static LocalDate lerData(String mensagem) {
        try {
            return LocalDate.parse(lerTexto(mensagem), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            System.out.println("Data invalida.");
            return null;
        }
    }

    private static LocalDateTime lerDataHora(String mensagem) {
        try {
            return LocalDateTime.parse(lerTexto(mensagem), FORMATO_DATA_HORA);
        } catch (DateTimeParseException e) {
            System.out.println("Data ou hora invalida.");
            return null;
        }
    }
}
