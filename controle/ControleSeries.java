package controle;

import modelo.Serie;
import modelo.Episodio;
import modelo.Ator;
import util.*;
import visao.VisaoSeries;
import visao.VisaoEpisodios;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import index.ListaInvertida;
import index.Indexador;
import index.ElementoLista;
import java.util.HashMap;
import java.util.Map;

public class ControleSeries {
    private Arquivo<Serie> arqSeries;
    private Arquivo<Episodio> arqEpisodios;
    private ArvoreBMais<ParSerieEpisodio> indiceArvore;
    private ArvoreBMais<SerieAtor> indiceSerieAtor;
    private ArvoreBMais<AtorSerie> indiceAtorSerie;
    private Arquivo<Ator> arqAtores;
    private VisaoSeries visaoS;
    private VisaoEpisodios visaoE;
    private Scanner sc;
    private ListaInvertida listaInvertida;
    private Indexador indexador;

    public ControleSeries() throws Exception {
        arqSeries = new Arquivo<>("Series", Serie.class.getConstructor());
        arqEpisodios = new Arquivo<>("Episodios", Episodio.class.getConstructor());
        arqAtores = new Arquivo<>("Atores", Ator.class.getConstructor());
        indiceArvore = new ArvoreBMais<>(ParSerieEpisodio.class.getConstructor(), 4, "dados/Episodios/serie_episodio.ind");
        indiceSerieAtor = new ArvoreBMais<>(SerieAtor.class.getConstructor(), 4, "dados/Series/serie_ator.ind");
        indiceAtorSerie = new ArvoreBMais<>(AtorSerie.class.getConstructor(), 4, "dados/Atores/ator_serie.ind");
        listaInvertida = new ListaInvertida(10, "dados/Series/dicionario_series.dat", "dados/Series/blocos_series.dat");
        indexador = new Indexador(listaInvertida);
        visaoS = new VisaoSeries();
        visaoE = new VisaoEpisodios();
        sc = new Scanner(System.in);
    }

    public void menu() {
        int opc;
        do {
            System.out.println("\nPUCFlix 1.0");
            System.out.println("-----------");
            System.out.println("> Início > Séries");
            System.out.println("1. Inserir série");
            System.out.println("2. Buscar série");
            System.out.println("3. Atualizar série");
            System.out.println("4. Excluir série");
            System.out.println("5. Visualizar episódios por temporada");
            System.out.println("6. Gerenciar elenco");
            System.out.println("7. Buscar série por termos");
            System.out.println("0. Voltar");

            System.out.print("Opção: ");
            opc = Integer.parseInt(sc.nextLine());

            try {
                switch (opc) {
                    case 1 -> inserirSerie();
                    case 2 -> buscarSerie();
                    case 3 -> atualizarSerie();
                    case 4 -> excluirSerie();
                    case 5 -> visualizarEpisodiosPorTemporada();
                    case 6 -> gerenciarElenco();
                    case 7 -> buscarSeriePorTermos();
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (opc != 0);
    }

    private void inserirSerie() throws Exception {
        Serie nova = visaoS.leSerie(sc);
        int id = arqSeries.create(nova);
        indexador.indexarTitulo(id, nova.getNome());
        System.out.println("Série salva com ID: " + id);
    }

    private void buscarSerie() throws Exception {
        System.out.print("ID da série: ");
        int id = Integer.parseInt(sc.nextLine());
        Serie s = arqSeries.read(id);
        if (s != null) {
            visaoS.mostraSerie(s);
            listarAtoresDaSerie(id);
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void atualizarSerie() throws Exception {
        System.out.print("ID da série para atualizar: ");
        int id = Integer.parseInt(sc.nextLine());
        Serie existente = arqSeries.read(id);
        if (existente != null) {
            Serie nova = visaoS.leSerie(sc);
            nova.setId(id);
            if (arqSeries.update(nova)) {
                indexador.atualizarTitulo(id, existente.getNome(), nova.getNome());
                System.out.println("Série atualizada com sucesso.");
            } else {
                System.out.println("Erro ao atualizar série.");
            }
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void excluirSerie() throws Exception {
        System.out.print("ID da série para excluir: ");
        int id = Integer.parseInt(sc.nextLine());

        ArrayList<ParSerieEpisodio> relacionados = indiceArvore.read(new ParSerieEpisodio(id, -1));
        if (!relacionados.isEmpty()) {
            System.out.println("Não é possível excluir. Existem episódios vinculados à série.");
            return;
        }

        ArrayList<SerieAtor> vinculos = indiceSerieAtor.read(new SerieAtor(id, -1));
        for (SerieAtor v : vinculos) {
            indiceSerieAtor.delete(v);
            indiceAtorSerie.delete(new AtorSerie(v.getIdAtor(), v.getIdSerie()));
        }

        Serie antiga = arqSeries.read(id);
        if (antiga != null) {
            indexador.removerDocumento(id, antiga.getNome());
        }

        if (arqSeries.delete(id)) {
            System.out.println("Série excluída com sucesso.");
        } else {
            System.out.println("Série não encontrada ou erro ao excluir.");
        }
    }

    private void visualizarEpisodiosPorTemporada() throws Exception {
        System.out.print("ID da série: ");
        int idSerie = Integer.parseInt(sc.nextLine());
        Serie s = arqSeries.read(idSerie);

        if (s == null) {
            System.out.println("Série não encontrada.");
            return;
        }

        ArrayList<ParSerieEpisodio> todos = indiceArvore.readAll();
        ArrayList<ParSerieEpisodio> pares = new ArrayList<>();
        for (ParSerieEpisodio par : todos) {
            if (par.getIdSerie() == idSerie) {
                pares.add(par);
            }
        }

        ArrayList<Episodio> episodios = new ArrayList<>();
        for (ParSerieEpisodio par : pares) {
            Episodio ep = arqEpisodios.read(par.getIdEpisodio());
            if (ep != null) episodios.add(ep);
        }

        System.out.print("Temporada para filtrar: ");
        int temporada = Integer.parseInt(sc.nextLine());

        boolean encontrados = false;
        for (Episodio ep : episodios) {
            if (ep.getTemporada() == temporada) {
                visaoE.mostraEpisodio(ep);
                encontrados = true;
            }
        }

        if (!encontrados) {
            System.out.println("Nenhum episódio encontrado para essa temporada.");
        }
    }

    private void gerenciarElenco() throws Exception {
        System.out.print("\nID da série: ");
        int idSerie = Integer.parseInt(sc.nextLine());

        Serie s = arqSeries.read(idSerie);
        if (s == null) {
            System.out.println("Série não encontrada!");
            return;
        }

        int opcao;
        do {
            System.out.println("\nGERENCIAR ELENCO - SÉRIE " + s.getNome());
            System.out.println("1) Listar atores");
            System.out.println("2) Adicionar ator");
            System.out.println("3) Remover ator");
            System.out.println("0) Voltar");
            System.out.print("\nOpção: ");

            opcao = Integer.parseInt(sc.nextLine());

            switch (opcao) {
                case 1 -> listarAtoresDaSerie(idSerie);
                case 2 -> adicionarAtorNaSerie(idSerie);
                case 3 -> removerAtorDaSerie(idSerie);
            }
        } while (opcao != 0);
    }

    private void listarAtoresDaSerie(int idSerie) throws Exception {
        System.out.println("\nATORES DA SÉRIE:");

        ArrayList<SerieAtor> vinculos = indiceSerieAtor.read(new SerieAtor(idSerie, -1));

        if (vinculos.isEmpty()) {
            System.out.println("Nenhum ator vinculado a esta série.");
            return;
        }

        for (SerieAtor vinculo : vinculos) {
            Ator a = arqAtores.read(vinculo.getIdAtor());
            if (a != null) {
                System.out.println("ID: " + a.getID() + " | Nome: " + a.getNome());
            }
        }
    }

    private void adicionarAtorNaSerie(int idSerie) throws Exception {
        System.out.print("\nID do ator a ser vinculado: ");
        int idAtor = Integer.parseInt(sc.nextLine());

        if (arqAtores.read(idAtor) == null) {
            System.out.println("Ator não encontrado!");
            return;
        }

        SerieAtor novo = new SerieAtor(idSerie, idAtor);
        if (!indiceSerieAtor.read(novo).isEmpty()) {
            System.out.println("Este ator já está vinculado à série!");
            return;
        }

        indiceSerieAtor.create(novo);
        indiceAtorSerie.create(new AtorSerie(idAtor, idSerie));
        System.out.println("Ator vinculado com sucesso!");
    }

    private void removerAtorDaSerie(int idSerie) throws Exception {
        System.out.print("\nID do ator a ser desvinculado: ");
        int idAtor = Integer.parseInt(sc.nextLine());

        SerieAtor vinculo = new SerieAtor(idSerie, idAtor);
        if (indiceSerieAtor.read(vinculo).isEmpty()) {
            System.out.println("Este ator não está vinculado à série!");
            return;
        }

        indiceSerieAtor.delete(vinculo);
        indiceAtorSerie.delete(new AtorSerie(idAtor, idSerie));
        System.out.println("Ator desvinculado com sucesso!");
    }

    public void buscarSeriePorTermos() throws Exception {
        System.out.print("Digite os termos de busca: ");
        String termos = sc.nextLine();
        List<String> termosBusca = TextoUtils.tokenizar(termos);
        if (termosBusca.isEmpty()) {
            System.out.println("Nenhum termo válido para busca.");
            return;
        }
        Map<Integer, Float> scores = new HashMap<>();
        int totalSeries = listaInvertida.numeroEntidades();
        for (String termo : termosBusca) {
            ElementoLista[] resultados = listaInvertida.read(termo);
            if (resultados.length == 0) continue;
            double idf = Math.log(totalSeries / (double)resultados.length) + 1;
            for (ElementoLista el : resultados) {
                int idSerie = el.getId();
                float tfidf = (float)(el.getFrequencia() * idf);
                scores.put(idSerie, scores.getOrDefault(idSerie, 0f) + tfidf);
            }
        }
        if (scores.isEmpty()) {
            System.out.println("Nenhuma série encontrada com os termos informados.");
            return;
        }
        List<Map.Entry<Integer, Float>> seriesOrdenadas = new ArrayList<>(scores.entrySet());
        seriesOrdenadas.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        System.out.println("\nResultados da busca:");
        for (Map.Entry<Integer, Float> entry : seriesOrdenadas) {
            Serie s = arqSeries.read(entry.getKey());
            if (s != null) {
                System.out.printf("Score: %.3f - ", entry.getValue());
                visaoS.mostraSerie(s);
            }
        }
    }
}
