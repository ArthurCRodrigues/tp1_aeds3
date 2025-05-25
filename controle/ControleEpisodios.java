package controle;

import modelo.Episodio;
import modelo.Serie;
import visao.VisaoEpisodios;
import util.*;
import index.*;
import java.util.*;

public class ControleEpisodios {

    private Arquivo<Episodio> arqEpisodios;
    private Arquivo<Serie> arqSeries;
    private ArvoreBMais<ParSerieEpisodio> indiceArvore;
    private VisaoEpisodios visaoE;
    private Scanner sc;
    private int idSerie;
    private ListaInvertida listaInvertida;
    private Indexador indexador;

    public ControleEpisodios(int idSerie) throws Exception {
        arqEpisodios = new Arquivo<>("Episodios", Episodio.class.getConstructor());
        arqSeries = new Arquivo<>("Series", Serie.class.getConstructor());
        indiceArvore = new ArvoreBMais<>(
                ParSerieEpisodio.class.getConstructor(),
                4,
                "dados/Episodios/serie_episodio.ind"
        );
        // Inicializa a lista invertida para os episódios
        listaInvertida = new ListaInvertida(10, "dados/Episodios/dicionario_episodios.dat", "dados/Episodios/blocos_episodios.dat");
        indexador = new Indexador(listaInvertida);
        visaoE = new VisaoEpisodios();
        sc = new Scanner(System.in);
        this.idSerie = idSerie;

        Serie s = arqSeries.read(idSerie);
        if (s == null) {
            throw new Exception("Série não encontrada. Não é possível gerenciar episódios.");
        }
    }

    public ControleEpisodios() throws Exception {
        arqEpisodios = new Arquivo<>("Episodios", Episodio.class.getConstructor());
        arqSeries = new Arquivo<>("Series", Serie.class.getConstructor());
        indiceArvore = new ArvoreBMais<>(
                ParSerieEpisodio.class.getConstructor(),
                4,
                "dados/Episodios/serie_episodio.ind"
        );
        // Inicializa a lista invertida para os episódios
        listaInvertida = new ListaInvertida(10, "dados/Episodios/dicionario_episodios.dat", "dados/Episodios/blocos_episodios.dat");
        indexador = new Indexador(listaInvertida);
        visaoE = new VisaoEpisodios();
        sc = new Scanner(System.in);
    }

    public void menu() {
        try {
            System.out.print("Informe o ID da série para gerenciar episódios: ");
            idSerie = Integer.parseInt(sc.nextLine());

            Serie s = arqSeries.read(idSerie);
            if (s == null) {
                System.out.println("❌ Série não encontrada. Não é possível gerenciar episódios.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Erro ao tentar acessar a série.");
            return;
        }

        int opc;
        do {
            System.out.println("\nPUCFlix 1.0");
            System.out.println("------------");
            System.out.println("> Início > Séries > Série: "+ idSerie);
            System.out.println("1. Incluir episódio");
            System.out.println("2. Buscar episódio por ID");
            System.out.println("3. Buscar episódio por termos");
            System.out.println("4. Atualizar episódio");
            System.out.println("5. Excluir episódio");
            System.out.println("6. Listar todos os episódios");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            opc = Integer.parseInt(sc.nextLine());

            try {
                switch (opc) {
                    case 1 -> incluirEpisodio();
                    case 2 -> buscarEpisodioPorId();
                    case 3 -> buscarEpisodioPorTermos();
                    case 4 -> atualizarEpisodio();
                    case 5 -> excluirEpisodio();
                    case 6 -> listarEpisodios();
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }

        } while (opc != 0);
    }

    public void incluirEpisodio() throws Exception {
        Episodio ep = visaoE.leEpisodio(idSerie);
        if (ep == null) return;

        int id = arqEpisodios.create(ep);
        indiceArvore.create(new ParSerieEpisodio(idSerie, id));
        
        // Indexa o título do episódio na lista invertida
        indexador.indexarTitulo(id, ep.getNome());
        
        System.out.println("Episódio incluído com ID: " + id);
    }

    public void buscarEpisodioPorId() throws Exception {
        System.out.print("ID do episódio: ");
        int id = Integer.parseInt(sc.nextLine());
        // Busca via índice invertido (não acessa o arquivo diretamente)
        ElementoLista[] termosDoEpisodio = listaInvertida.readAllTermsForDocument(id);
    
            if (termosDoEpisodio.length == 0) {
                System.out.println("Episódio não encontrado ou não pertence a esta série.");
                return;
            }

        // Verifica se o episódio pertence à série atual
        for (ElementoLista elemento : termosDoEpisodio) {
            Episodio ep = arqEpisodios.read(elemento.getId()); // Só acessa arquivo após validação
           
            if (ep != null && ep.getIdSerie() == idSerie) {
            visaoE.mostraEpisodio(ep);
            return;
           }
        }
    
     System.out.println("Episódio não pertence a esta série.");
    }


    public void buscarEpisodioPorTermos() throws Exception {
        System.out.print("Digite os termos de busca: ");
        String termos = sc.nextLine();
        
        // Normaliza e tokeniza os termos de busca
        List<String> termosBusca = TextoUtils.tokenizar(termos);
        
        if (termosBusca.isEmpty()) {
            System.out.println("Nenhum termo válido para busca.");
            return;
        }
        
        // Mapa para armazenar os scores TF-IDF por ID de episódio
        Map<Integer, Float> scores = new HashMap<>();
        int totalEpisodios = listaInvertida.numeroEntidades();
        
        for (String termo : termosBusca) {
            // Busca os episódios que contêm o termo
            ElementoLista[] resultados = listaInvertida.read(termo);
            
            if (resultados.length == 0) continue;
            
            // Calcula o IDF para o termo
            double idf = Math.log(totalEpisodios / (double)resultados.length) + 1;
            
            // Atualiza os scores para cada episódio encontrado
            for (ElementoLista el : resultados) {
                int idEpisodio = el.getId();
                float tfidf = (float)(el.getFrequencia() * idf);
                
                // Soma o score para episódios que aparecem em múltiplos termos
                scores.put(idEpisodio, scores.getOrDefault(idEpisodio, 0f) + tfidf);
            }
        }
        
        if (scores.isEmpty()) {
            System.out.println("Nenhum episódio encontrado com os termos informados.");
            return;
        }
        
        // Ordena os episódios por score (maior primeiro)
        List<Map.Entry<Integer, Float>> episodiosOrdenados = new ArrayList<>(scores.entrySet());
        episodiosOrdenados.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        // Mostra os resultados
        System.out.println("\nResultados da busca:");
        for (Map.Entry<Integer, Float> entry : episodiosOrdenados) {
            Episodio ep = arqEpisodios.read(entry.getKey());
            if (ep != null && ep.getIdSerie() == idSerie) {
                System.out.printf("Score: %.3f - ", entry.getValue());
                visaoE.mostraEpisodioResumido(ep);
            }
        }
    }

    public void atualizarEpisodio() throws Exception {
        System.out.print("ID do episódio: ");
        int id = Integer.parseInt(sc.nextLine());
        Episodio antigo = arqEpisodios.read(id);
        if (antigo == null || antigo.getIdSerie() != idSerie) {
            System.out.println("Episódio não encontrado ou não pertence a esta série.");
            return;
        }

        Episodio novo = visaoE.leEpisodio(idSerie);
        if (novo == null) return;

        novo.setId(id);
        arqEpisodios.update(novo);
        
        // Atualiza o índice invertido
        indexador.atualizarTitulo(id, antigo.getNome(), novo.getNome());
        
        System.out.println("Episódio atualizado.");
    }

    public void excluirEpisodio() throws Exception {
        System.out.print("ID do episódio: ");
        int id = Integer.parseInt(sc.nextLine());
        Episodio ep = arqEpisodios.read(id);
        if (ep == null || ep.getIdSerie() != idSerie) {
            System.out.println("Episódio não encontrado ou não pertence a esta série.");
            return;
        }

        arqEpisodios.delete(id);
        indiceArvore.delete(new ParSerieEpisodio(idSerie, id));
        
        // Remove do índice invertido
        indexador.removerDocumento(id, ep.getNome());
        
        System.out.println("Episódio excluído.");
    }

    public void listarEpisodios() throws Exception {
        ArrayList<ParSerieEpisodio> todos = indiceArvore.readAll();
        ArrayList<ParSerieEpisodio> pares = new ArrayList<>();
        for (ParSerieEpisodio par : todos) {
            if (par.getIdSerie() == idSerie) {
                pares.add(par);
            }
        }

        if (pares.isEmpty()) {
            System.out.println("Nenhum episódio encontrado para esta série.");
            return;
        }

        for (ParSerieEpisodio par : pares) {
            Episodio ep = arqEpisodios.read(par.getIdEpisodio());
            if (ep != null) visaoE.mostraEpisodio(ep);
        }
    }
}