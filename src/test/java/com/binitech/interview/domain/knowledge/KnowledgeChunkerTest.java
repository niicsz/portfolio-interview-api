package com.binitech.interview.domain.knowledge;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeChunkerTest {

  @Test
  void splitsBySectionAndPrefixesTitles() {
    KnowledgeDocument document =
        new KnowledgeDocument(
            "exp.md",
            "Experiência",
            "# Experiência\n\nIntro.\n\n## Btoken\n\nKafka.\n\n## DocMatch\n\nSerasa.\n");

    List<KnowledgeChunk> chunks = new KnowledgeChunker(200).chunk(document);

    assertThat(chunks)
        .extracting(KnowledgeChunk::section)
        .containsExactly("Experiência", "Btoken", "DocMatch");
    assertThat(chunks.get(1).content()).isEqualTo("Experiência — Btoken\nKafka.");
    assertThat(chunks).extracting(KnowledgeChunk::id).doesNotHaveDuplicates();
  }

  @Test
  void splitsLongSectionsByParagraph() {
    String paragraph = "x".repeat(150);
    KnowledgeDocument document =
        new KnowledgeDocument(
            "p.md", "Projetos", "## PDV\n\n" + paragraph + "\n\n" + paragraph + "\n\n" + paragraph);

    List<KnowledgeChunk> chunks = new KnowledgeChunker(200).chunk(document);

    assertThat(chunks).hasSize(3).allSatisfy(chunk -> assertThat(chunk.section()).isEqualTo("PDV"));
  }
}
