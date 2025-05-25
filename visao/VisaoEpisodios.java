package visao;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import modelo.Episodio;

public class VisaoEpisodios {

    private Scanner sc = new Scanner(System.in);

    public Episodio leEpisodio(int idSerie) {
        try {
            System.out.print("Nome do episódio: ");
            String nome = sc.nextLine();

            System.out.print("Temporada: ");
            int temporada = Integer.parseInt(sc.nextLine());

            LocalDate dataLancamento = null;
            boolean dataValida = false;
            while (!dataValida) {
                System.out.print("Data de lançamento (AAAA-MM-DD): ");
                String dataStr = sc.nextLine();
                try {
                    dataLancamento = LocalDate.parse(dataStr); // formato ISO
                    dataValida = true;
                } catch (DateTimeParseException e) {
                    System.out.println("Data inválida! Tente novamente.");
                }
            }

            System.out.print("Duração (minutos): ");
            int duracao = Integer.parseInt(sc.nextLine());

            System.out.print("Sinopse: ");
            String sinopse = sc.nextLine();

            return new Episodio(idSerie, nome, temporada, dataLancamento, duracao, sinopse);

        } catch (Exception e) {
            System.out.println("Erro ao ler episódio: " + e.getMessage());
            return null;
        }
    }

    public void mostraEpisodio(Episodio e) {
        if (e != null) {
            System.out.println("\n--- Detalhes do Episódio ---");
            System.out.println(e.toString());
            System.out.println("----------------------------\n");
        } else {
            System.out.println("Episódio inválido.");
        }
    }

    
}
