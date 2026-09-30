package com.binitech.interview.application.ports.outbound;

import com.binitech.interview.domain.knowledge.KnowledgeDocument;
import java.util.List;

public interface KnowledgeSourcePort {

  List<KnowledgeDocument> loadAll();
}
