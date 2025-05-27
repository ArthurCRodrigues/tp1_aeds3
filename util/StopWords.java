package util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StopWords {

    private static final Set<String> stopWords = new HashSet<>(Arrays.asList(
        "a", "o", "e", "de", "do", "da", "dos", "das", "em", "um", "uma", "uns", "umas",
        "por", "com", "para", "na", "no", "nas", "nos", "que", "se", "ao", "à", "às", "aos",
        "é", "foi", "são", "ser", "tem", "têm", "há", "havia", "era", "estava", "estavam"
    ));

    public static boolean isStopWord(String palavra) {
        return stopWords.contains(palavra);
    }
}
