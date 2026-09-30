package com.binitech.interview.domain.knowledge;

/** Trecho indexável de um documento. {@code section} é o título da seção de origem. */
public record KnowledgeChunk(String id, String source, String section, String content) {}
