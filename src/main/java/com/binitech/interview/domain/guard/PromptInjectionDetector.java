package com.binitech.interview.domain.guard;

import com.binitech.interview.domain.Question;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PromptInjectionDetector {

  private static final List<Pattern> PATTERNS =
      List.of(
          Pattern.compile(
              "\\b(ignor\\w*|disregard\\w*|desconsider\\w*|esquec\\w*|forget\\w*|overrid\\w*|sobrescrev\\w*|bypass\\w*|burl\\w*)"
                  + "\\b.{0,40}\\b(instruc\\w*|instruct\\w*|regras?|rules?|prompt\\w*|orientac\\w*|diretriz\\w*|guidelines?|restric\\w*|restrict\\w*|acima|above|previous|anterior\\w*|prior)\\b"),
          Pattern.compile("\\b(system|sistema)\\s+(prompt|message|mensagem|instruc\\w*)\\b"),
          Pattern.compile(
              "\\b(prompt|instruc\\w*|mensagem|configurac\\w*)\\s+(do|de)\\s+(sistema|system)\\b"),
          Pattern.compile(
              "\\b(initial|inicia\\w*|hidden|ocult\\w*|secret\\w*|confidencia\\w*)\\s+(prompt|instruc\\w*|instruct\\w*)"),
          Pattern.compile(
              "\\b(you are now|you're now|now you are|from now on|a partir de agora|voce agora e|agora voce e|voce e agora)\\b"),
          Pattern.compile(
              "\\b(pretend|finja|fingir|roleplay|role-play|role play|faca de conta|simule ser|imagine que voce)\\b"),
          Pattern.compile(
              "^(please\\s+|por favor\\s+)?(act as|aja como|atue como|comporte-se como)\\b"),
          Pattern.compile(
              "\\b(voce|you)\\b.{0,20}\\b(act as|aja como|atue como|comporte-se como|se comporte como)\\b"),
          Pattern.compile(
              "\\b(jailbreak\\w*|dan mode|developer mode|modo desenvolvedor|modo dev|god mode|sudo mode|modo admin\\w*|admin mode)\\b"),
          Pattern.compile(
              "\\b(reveal|revel\\w*|show|mostr\\w*|print|imprim\\w*|repeat|repit\\w*|repet\\w*|leak|vaz\\w*|output|exib\\w*|list\\w*|dump)\\b"
                  + ".{0,40}\\b(prompt\\w*|texto acima|text above|above text|primeira mensagem|first message)\\b"),
          Pattern.compile(
              "\\b(suas|tuas|your)\\s+(regras|rules|instruc\\w*|instruct\\w*|diretriz\\w*)\\b"),
          Pattern.compile(
              "</?\\s*(system|assistant|user|human|contexto|pergunta|documento|instructions?)\\b"),
          Pattern.compile("\\[/?\\s*(inst|system|sys)\\s*\\]|<\\|[^|]{0,30}\\|>|<<\\s*sys"),
          Pattern.compile("(^|\\s)(system|assistant|sistema|assistente|human|humano)\\s*:"),
          Pattern.compile("#{2,}\\s*(instruc\\w*|instruct\\w*|system|sistema)"),
          Pattern.compile("```"),
          Pattern.compile(
              "\\b(api[\\s_-]?keys?|chaves? (de |da )?api|variave(l|is) de ambiente|environment variables?|env vars?)\\b"),
          Pattern.compile("[a-z0-9+/=]{60,}"));

  private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

  public boolean isSuspicious(Question question) {
    String normalized = normalizeForMatching(question.text());
    return PATTERNS.stream().anyMatch(pattern -> pattern.matcher(normalized).find());
  }

  static String normalizeForMatching(String text) {
    String withoutAccents =
        DIACRITICS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
    StringBuilder builder = new StringBuilder(withoutAccents.length());
    for (char c : withoutAccents.toLowerCase(Locale.ROOT).toCharArray()) {
      builder.append(
          switch (c) {
            case '0' -> 'o';
            case '1', '!' -> 'i';
            case '3' -> 'e';
            case '4', '@' -> 'a';
            case '5', '$' -> 's';
            case '7' -> 't';
            default -> c;
          });
    }
    return builder.toString();
  }
}
