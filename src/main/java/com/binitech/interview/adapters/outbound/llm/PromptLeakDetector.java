package com.binitech.interview.adapters.outbound.llm;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

final class PromptLeakDetector {

  private static final int SHINGLE_SIZE = 6;
  private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{N}]+");
  private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

  private final String canary;
  private final Set<String> promptShingles;

  PromptLeakDetector(String systemPrompt, String canary) {
    this.canary = canary.toLowerCase(Locale.ROOT);
    this.promptShingles = shingles(systemPrompt);
  }

  boolean leaks(String answer) {
    if (answer.toLowerCase(Locale.ROOT).contains(canary)) {
      return true;
    }
    Set<String> answerShingles = shingles(answer);
    answerShingles.retainAll(promptShingles);
    return !answerShingles.isEmpty();
  }

  private static Set<String> shingles(String text) {
    String normalized =
        DIACRITICS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
    List<String> words =
        List.of(
            NON_WORD
                .matcher(normalized.toLowerCase(Locale.ROOT))
                .replaceAll(" ")
                .strip()
                .split(" "));
    Set<String> shingles = new HashSet<>();
    for (int i = 0; i + SHINGLE_SIZE <= words.size(); i++) {
      shingles.add(String.join(" ", words.subList(i, i + SHINGLE_SIZE)));
    }
    return shingles;
  }
}
