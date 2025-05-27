package controle;

import modelo.Ator;
import modelo.Serie;
import util.*;
import visao.VisaoAtores;
import index.ListaInvertida;
import index.Indexador;
import index.ElementoLista;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class ControleAtores {
    private Arquivo<Ator> arqAtores;
    private Arquivo<Serie> arqSeries;
    private ArvoreBMais<AtorSerie> indiceAtorSerie;
    private ArvoreBMais<SerieAtor> indiceSerieAtor;
    private VisaoAtores visao;
    private Scanner sc;
    private ListaInvertida listaInvertida;
    private Indexador indexador;

    public ControleAtores() throws Exception {
        arqAtores = new Arquivo<>("Atores", Ator.class.getConstructor());
        arqSeries = new Arquivo<>("Series", Serie.class.getConstructor());
        indiceAtorSerie = new ArvoreBMais<>(AtorSerie.class.getConstructor(), 4, "dados/Atores/ator_serie.ind");
        indiceSerieAtor = new ArvoreBMais<>(SerieAtor.class.getConstructor(), 4, "dados/Series/serie_ator.ind");
        listaInvertida = new ListaInvertida(10, "dados/Atores/dicionario_atores.dat", "dados/Atores/blocos_atores.dat");
        indexador = new Indexador(listaInvertida);
        visao = new VisaoAtores();
        sc = new Scanner(System.in);
    }

    public void menu() {
        int op;
        do {
            System.out.println("\nPUCFlix 1.0");
            System.out.println("-----------");
            System.out.println("1. Cadastrar ator");
            System.out.println("2. Buscar ator");
            System.out.println("3. Atualizar ator");
            System.out.println("4. Excluir ator");
            System.out.println("5. Listar todos os atores");
            System.out.println("6. Buscar ator por termos");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            op = Integer.parseInt(sc.nextLine());

            try {
                switch (op) {
                    case 1 -> cadastrarAtor();
                    case 2 -> buscarAtor();
                    case 3 -> atualizarAtor();
                    case 4 -> excluirAtor();
                    case 5 -> listarAtores();
                    case 6 -> buscarAtorPorTermos();
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }

        } while (op != 0);
    }

    private void cadastrarAtor() throws Exception {
        Ator a = visao.leAtor(sc);
        if (a == null) return;

        int id = arqAtores.create(a);
        indexador.indexarTitulo(id, a.getNome());
        System.out.println("✅ Ator cadastrado com ID: " + id);
    }

    private void buscarAtor() throws Exception {
        System.out.print("ID do ator: ");
        int id = Integer.parseInt(sc.nextLine());
        Ator a = arqAtores.read(id);

        if (a != null) {
            visao.mostraAtor(a);
            visao.mostraSeriesVinculadas(getNomesSeriesVinculadas(id));
        } else {
            System.out.println("❌ Ator não encontrado.");
        }
    }

    private void atualizarAtor() throws Exception {
        System.out.print("ID do ator: ");
        int id = Integer.parseInt(sc.nextLine());
        Ator antigo = arqAtores.read(id);
        if (antigo == null) {
            System.out.println("❌ Ator não encontrado.");
            return;
        }

        Ator novo = visao.leAtor(sc);
        if (novo == null) return;
        novo.setID(id);

        arqAtores.update(novo);
        indexador.atualizarTitulo(id, antigo.getNome(), novo.getNome());
        System.out.println("✅ Ator atualizado.");
    }

    private void excluirAtor() throws Exception {
        System.out.print("ID do ator: ");
        int id = Integer.parseInt(sc.nextLine());
        Ator a = arqAtores.read(id);
        if (a == null) {
            System.out.println("❌ Ator não encontrado.");
            return;
        }

        ArrayList<AtorSerie> vinculados = indiceAtorSerie.read(new AtorSerie(id, -1));
        if (!vinculados.isEmpty()) {
            System.out.println("❌ Este ator está vinculado a uma ou mais séries. Remova os vínculos antes de excluir.");
            return;
        }

        indexador.removerDocumento(id, a.getNome());
        arqAtores.delete(id);
        System.out.println("✅ Ator excluído.");
    }

    private void listarAtores() throws Exception {
        int ultimoId = arqAtores.getUltimoID();
        boolean encontrou = false;

        for (int i = 0; i <= ultimoId; i++) {
            Ator a = arqAtores.read(i);
            if (a != null) {
                encontrou = true;
                visao.mostraAtor(a);
                visao.mostraSeriesVinculadas(getNomesSeriesVinculadas(a.getID()));
                System.out.println("-----------------------");
            }
        }

        if (!encontrou) {
            System.out.println("Nenhum ator cadastrado.");
        }
    }

    private List<String> getNomesSeriesVinculadas(int idAtor) throws Exception {
        ArrayList<AtorSerie> pares = indiceAtorSerie.read(new AtorSerie(idAtor, -1));
        List<String> nomes = new ArrayList<>();
        for (AtorSerie par : pares) {
            Serie s = arqSeries.read(par.getIdSerie());
            if (s != null) nomes.add(s.getNome());
        }
        return nomes;
    }

    public Ator ler(int idAtor) throws Exception {
        return arqAtores.read(idAtor);
    }

    public void vincularAtorSerie(int idAtor, int idSerie) throws Exception {
        indiceAtorSerie.create(new AtorSerie(idAtor, idSerie));
        indiceSerieAtor.create(new SerieAtor(idSerie, idAtor));
    }

    public void deletarVinculo(int idAtor, int idSerie) throws Exception {
        indiceAtorSerie.delete(new AtorSerie(idAtor, idSerie));
        indiceSerieAtor.delete(new SerieAtor(idSerie, idAtor));
    }

    public void buscarAtorPorTermos() throws Exception {
        System.out.print("Digite os termos de busca: ");
        String termos = sc.nextLine();
        List<String> termosBusca = TextoUtils.tokenizar(termos);
        if (termosBusca.isEmpty()) {
            System.out.println("Nenhum termo válido para busca.");
            return;
        }
        Map<Integer, Float> scores = new HashMap<>();
        int totalAtores = listaInvertida.numeroEntidades();
        for (String termo : termosBusca) {
            ElementoLista[] resultados = listaInvertida.read(termo);
            if (resultados.length == 0) continue;
            double idf = Math.log(totalAtores / (double)resultados.length) + 1;
            for (ElementoLista el : resultados) {
                int idAtor = el.getId();
                float tfidf = (float)(el.getFrequencia() * idf);
                scores.put(idAtor, scores.getOrDefault(idAtor, 0f) + tfidf);
            }
        }
        if (scores.isEmpty()) {
            System.out.println("Nenhum ator encontrado com os termos informados.");
            return;
        }
        List<Map.Entry<Integer, Float>> atoresOrdenados = new ArrayList<>(scores.entrySet());
        atoresOrdenados.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        System.out.println("\nResultados da busca:");
        for (Map.Entry<Integer, Float> entry : atoresOrdenados) {
            Ator a = arqAtores.read(entry.getKey());
            if (a != null) {
                System.out.printf("Score: %.3f - ", entry.getValue());
                visao.mostraAtor(a);
            }
        }
    }
}
