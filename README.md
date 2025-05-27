# 📺 PUCFlix 1.0 — Índice Invertido

Este terceiro trabalho prático da disciplina de AEDS 3 estende o sistema anterior de gerenciamento de séries, episódios e atores, adicionando **busca textual por termos** usando **Índice Invertido** e cálculo de **TF×IDF**. Agora é possível buscar entidades por palavras em títulos ou nomes, com resultados ordenados por relevância.

---

## ✅ Funcionalidades

- CRUD completo para Séries, Episódios e Atores.
- Relacionamento 1:N entre Séries e Episódios (Árvore B+).  
- Gerenciamento de elenco (índice secundário Ator↔Série).  
- **Busca textual** por termos em:
  - Títulos de Séries.
  - Títulos de Episódios.
  - Nomes de Atores.
- Cálculo de **TF** (Term Frequency) e **IDF** (Inverse Document Frequency) e ranqueamento por **TF×IDF**.
- Armazenamento de índice invertido em arquivos com `ListaInvertida`.

---

## 👥 Participantes e Responsabilidades

🧑‍💻 **Guilherme – Implementação da ListaInvertida**  
• Classe `ListaInvertida.java`: persistência em arquivos (dicionário e blocos).  
• Métodos: `create`, `read`, `update`, `delete`, `readAllTermsForDocument`, `numeroEntidades`.  
• Cálculo de TF, leitura de listas de documentos e manutenção de contagem N.  
• Classe `Indexador.java`: façade para indexar, atualizar, remover e buscar termos.

🧑‍💻 **Daniel Victor – Integração com Séries**  
• Extensão de `ControleSeries.java`: indexação de título em create/update/delete.  
• Método `buscarSeriePorTermos`: tokenização, consulta a `ListaInvertida`, cálculo de TF×IDF e exibição ordenada.

🧑‍💻 **Alice – Integração com Episódios**  
• Extensão de `ControleEpisodios.java`: indexação de título em create/update/delete.  
• Reuso de `Indexador` e `ListaInvertida`.  
• Método `buscarEpisodioPorTermos`: busca e ranqueamento por TF×IDF.

🧑‍💻 **Arthur – Integração com Atores + Interface + Documentação**  
• Extensão de `ControleAtores.java`: indexação de nome em create/update/delete.  
• Método `buscarAtorPorTermos`: busca textual de atores.  
• Adição de opções de menu para busca por termos.  
• Escrita deste README com descrição, estrutura, experiência e checklist.

---

## 📦 Estrutura de Classes e Métodos Principais

### index
- **ListaInvertida**: gerencia índice invertido em arquivo. Métodos: `create(term, ElementoLista)`, `read(term)`, `readAllTermsForDocument(docId)`, `numeroEntidades()`, `update(term, el)`, `delete(term, id)`.
- **ElementoLista**: par `(id, frequência)`.
- **Indexador**: façade para indexar títulos/nomes; métodos `indexarTitulo`, `removerDocumento`, `atualizarTitulo`, `buscar`.

### controle
- **ControleSeries**: CRUD de séries + integração com índice invertido + menu `Buscar série por termos`.
- **ControleEpisodios**: CRUD de episódios + integração com índice invertido + menu `Buscar episódio por termos`.
- **ControleAtores**: CRUD de atores + integração com índice invertido + menu `Buscar ator por termos`.

### modelo
- **Serie, Episodio, Ator**: entidades com serialização `toByteArray()`/`fromByteArray()`.
- Relacionamentos: `ParSerieEpisodio`, `SerieAtor`, `AtorSerie`.

### util
- **TextoUtils**: normalização, tokenização e cálculo de TF.  
- **HashExtensivel**, **ArvoreBMais**: índices secundários e relacionais.

---

## 🧠 Experiência de Desenvolvimento

Implementar o índice invertido e o cálculo de TF×IDF foi o maior desafio. A arquitetura em MVC e o uso de arquivos binários exigiram cuidado na persistência e atualização do índice. A divisão de tarefas em quatro partes permitiu foco na classe genérica de índice, integração em cada controle e documentação. Testes com dados reais confirmaram a eficiência das buscas.

---

## 📋 Checklist

- [x] O índice invertido com os termos dos títulos das séries foi criado usando a classe `ListaInvertida`?  
- [x] O índice invertido com os termos dos títulos dos episódios foi criado usando a classe `ListaInvertida`?  
- [x] O índice invertido com os termos dos nomes dos atores foi criado usando a classe `ListaInvertida`?  
- [x] É possível buscar séries por palavras usando o índice invertido?  
- [x] É possível buscar episódios por palavras usando o índice invertido?  
- [x] É possível buscar atores por palavras usando o índice invertido?  
- [x] O trabalho está completo?  
- [x] O trabalho é original e não a cópia de um trabalho de um colega?

---

## 🔗 Repositório

`https://github.com/alicesalim/tp2_aeds3.git`  

