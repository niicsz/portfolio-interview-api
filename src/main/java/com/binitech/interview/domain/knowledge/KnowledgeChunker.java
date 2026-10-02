package com.binitech.interview.domain.knowledge;

import java.util.ArrayList;
import java.util.List;

public final class KnowledgeChunker {

  private final int maxChunkChars;

  public KnowledgeChunker(int maxChunkChars) {
    if (maxChunkChars < 200) {
      throw new IllegalArgumentException("maxChunkChars deve ser pelo menos 200");
    }
    this.maxChunkChars = maxChunkChars;
  }

  public List<KnowledgeChunk> chunk(KnowledgeDocument document) {
    List<KnowledgeChunk> chunks = new ArrayList<>();
    String section = document.title();
    StringBuilder body = new StringBuilder();

    for (String line : document.content().split("\\R")) {
      if (line.startsWith("# ")) {
        continue;
      }
      if (line.startsWith("## ")) {
        flush(document, section, body, chunks);
        section = line.substring(3).strip();
        continue;
      }
      body.append(line).append('\n');
    }
    flush(document, section, body, chunks);
    return chunks;
  }

  private void flush(
      KnowledgeDocument document, String section, StringBuilder body, List<KnowledgeChunk> chunks) {
    String text = body.toString().strip();
    body.setLength(0);
    if (text.isEmpty()) {
      return;
    }
    String header = document.title().equals(section) ? section : document.title() + " — " + section;
    for (String part : splitByParagraph(text)) {
      String id = document.source() + "#" + chunks.size();
      chunks.add(new KnowledgeChunk(id, document.source(), section, header + "\n" + part));
    }
  }

  private List<String> splitByParagraph(String text) {
    if (text.length() <= maxChunkChars) {
      return List.of(text);
    }
    List<String> parts = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    for (String paragraph : text.split("\\n\\s*\\n")) {
      if (current.length() > 0 && current.length() + paragraph.length() > maxChunkChars) {
        parts.add(current.toString().strip());
        current.setLength(0);
      }
      current.append(paragraph).append("\n\n");
    }
    if (current.length() > 0) {
      parts.add(current.toString().strip());
    }
    return parts;
  }
}
