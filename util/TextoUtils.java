package util;

import java.text.Normalizer;
import java.util.*;

public class TextoUtils {

    // Remove acentos
    public static String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                         .replaceAll("[^\\p{ASCII}]", "");
    }

    // Converte para minúsculas e remove acentos
    public static String normalizar(String texto) {
        return removerAcentos(texto.toLowerCase());
    }

    // Tokeniza uma string em palavras, removendo stop words
    public static List<String> tokenizar(String texto) {
        List<String> tokens = new ArrayList<>();
        texto = normalizar(texto);
        for (String palavra : texto.split("\\W+")) {
            if (!palavra.isEmpty() && !StopWords.isStopWord(palavra)) {
                tokens.add(palavra);
            }
        }
        return tokens;
    }

    // Calcula TF (frequência de um termo no texto)
    public static Map<String, Integer> calcularTF(List<String> palavras) {
        Map<String, Integer> tf = new HashMap<>();
        for (String palavra : palavras) {
            tf.put(palavra, tf.getOrDefault(palavra, 0) + 1);
        }
        return tf;
    }

    // Calcula IDF de todos os termos a partir de uma lista de documentos (listas de palavras)
    public static Map<String, Double> calcularIDF(List<List<String>> documentos) {
        Map<String, Double> idf = new HashMap<>();
        int totalDocumentos = documentos.size();

        for (List<String> doc : documentos) {
            Set<String> termosUnicos = new HashSet<>(doc);
            for (String termo : termosUnicos) {
                idf.put(termo, idf.getOrDefault(termo, 0.0) + 1);
            }
        }

        for (Map.Entry<String, Double> entry : idf.entrySet()) {
            idf.put(entry.getKey(), Math.log(totalDocumentos / entry.getValue()));
        }

        return idf;
    }

    // Calcula TF×IDF
    public static Map<String, Double> calcularTFIDF(Map<String, Integer> tf, Map<String, Double> idf) {
        Map<String, Double> tfidf = new HashMap<>();
        for (String termo : tf.keySet()) {
            double tfidfValor = tf.get(termo) * idf.getOrDefault(termo, 0.0);
            tfidf.put(termo, tfidfValor);
        }
        return tfidf;
    }
}
