package com.binitech.interview.adapters.outbound.knowledge;

import com.binitech.interview.application.ports.outbound.KnowledgeSourcePort;
import com.binitech.interview.domain.knowledge.KnowledgeDocument;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/** Lê os documentos Markdown de {@code classpath:knowledge/}. O título é a primeira linha "# ". */
@Component
public class ClasspathKnowledgeSourceAdapter implements KnowledgeSourcePort {

  private static final String LOCATION = "classpath:knowledge/*.md";

  private final PathMatchingResourcePatternResolver resolver =
      new PathMatchingResourcePatternResolver();

  @Override
  public List<KnowledgeDocument> loadAll() {
    try {
      return Arrays.stream(resolver.getResources(LOCATION))
          .sorted(Comparator.comparing(resource -> Objects.requireNonNull(resource.getFilename())))
          .map(this::toDocument)
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException("Falha ao ler a base de conhecimento", e);
    }
  }

  private KnowledgeDocument toDocument(Resource resource) {
    try {
      String content = resource.getContentAsString(StandardCharsets.UTF_8);
      String source = resource.getFilename();
      String title =
          content
              .lines()
              .filter(line -> line.startsWith("# "))
              .map(line -> line.substring(2).strip())
              .findFirst()
              .orElse(source);
      return new KnowledgeDocument(source, title, content);
    } catch (IOException e) {
      throw new UncheckedIOException("Falha ao ler " + resource.getFilename(), e);
    }
  }
}
