# 🎤 Portfolio Interview API

API do **"Interview me"** do [portfólio](https://github.com/niicsz/portfolio-v2): recrutadores e visitantes fazem perguntas sobre a carreira do Nicolas e recebem respostas geradas pelo **Claude** (Anthropic) com **RAG** sobre uma base de conhecimento curada. Perguntas fora desse escopo e tentativas de **prompt injection** são recusadas.

Desenvolvida com **API first** (contrato OpenAPI → interfaces geradas) e **arquitetura hexagonal**, em **Java 21** e **Spring Boot 3**.

---

## 🧭 Fluxo de uma pergunta

Cada pergunta passa por camadas da mais barata para a mais cara. A maioria dos ataques e das perguntas fora do escopo para antes de gastar tokens.

```
POST /api/interview/questions
  │
  ├─ CORS (só a origem do portfólio) + rate limit por IP e teto global diário ─► 429
  ├─ Validação do contrato (3–500 caracteres) ────────────────────────────────► 400
  ├─ Normalização: NFKC, remove caracteres invisíveis/bidi, achata quebras de linha
  ├─ Heurísticas de prompt injection (PT/EN, leetspeak, tags, base64) ────────► REJECTED
  ├─ RAG: embedding local da pergunta → busca top-k → limiar de relevância ──► OUT_OF_SCOPE
  ├─ Classificador Claude com saída estruturada (enum) ───────────────────────► OUT_OF_SCOPE / REJECTED
  ├─ Gerador Claude com saída estruturada, só com o contexto recuperado
  └─ Verificação de saída: canário, trechos do system prompt, tamanho ────────► REJECTED
                                                                              ► ANSWERED
```

## 🛡️ Defesas contra prompt injection

| Camada | O que impede |
|---|---|
| Sem histórico enviado pelo cliente | Forjar turnos anteriores do assistente |
| Normalização da pergunta | Texto invisível (zero-width), overrides bidirecionais, letras de largura total, falsas linhas `SYSTEM:` |
| Heurísticas (`PromptInjectionDetector`) | Ataques conhecidos sem custo de token: "ignore as instruções", troca de papel, extração de prompt, delimitadores de chat, blocos base64 |
| Pergunta escapada dentro de `<pergunta>` | A pergunta não consegue fechar a tag nem abrir uma seção nova |
| Classificador isolado com saída em enum | Mesmo uma injeção bem-sucedida não produz texto livre nesse passo, e na dúvida ele recusa (fail closed) |
| Gerador com schema `{answerable, answer}` | Resposta sempre estruturada, e o modelo pode se recusar a responder |
| Canário aleatório no system prompt + detecção de trechos copiados | Vazamento das instruções internas |
| Sem ferramentas, sem acesso a rede ou arquivos | Não há o que um atacante acionar, mesmo que manipule o modelo |
| Rate limit por IP (lendo o `X-Forwarded-For` pela direita) + teto global diário | Abuso e custo descontrolado; o teto global limita o gasto máximo por dia |

## 🏗️ Arquitetura

```
src/main/java/com/binitech/interview
├── domain                      # Java puro: Question, Answer, guard, knowledge
├── application
│   ├── ports/inbound           # AskQuestionUseCasePort, IndexKnowledgeUseCasePort
│   ├── ports/outbound          # Embedding, VectorStore, ScopeClassifier, AnswerGenerator, KnowledgeSource
│   └── usecases                # AskQuestionUseCase, IndexKnowledgeUseCase
├── adapters
│   ├── inbound/web             # Controller (implementa a interface gerada), rate limit, erros
│   ├── inbound/startup         # Indexação da base na subida
│   └── outbound                # Claude (SDK oficial), ONNX, vector store em memória, Markdown
└── config                      # BeanConfig, CORS, Anthropic, propriedades
```

As regras de dependência são verificadas pelo `ArchitectureTest` (ArchUnit): o domínio só usa Java, a aplicação só usa o domínio, e só o adaptador `llm` conhece o SDK da Anthropic.

- **Contrato:** `src/main/resources/openapi/swagger.yaml` (Swagger UI em `/swagger-ui.html`)
- **Base de conhecimento:** `src/main/resources/knowledge/*.md`, um arquivo por tema, com seções `##`. Para atualizar o que o assistente sabe, edite esses arquivos e faça o deploy.
- **Embeddings:** [multilingual-e5-small](https://huggingface.co/Xenova/multilingual-e5-small) em ONNX, rodando na própria JVM. Não precisa de outra chave de API e nenhum dado sai do servidor. O modelo é baixado por `scripts/download-embedding-model.sh`, fixado em um commit e com o SHA-256 conferido.
- **Vector store:** em memória. São cerca de 30 trechos reindexados a cada subida, então busca exata é mais simples que um banco vetorial. Para crescer, basta trocar o adaptador por um de pgvector.

> **Sobre o limiar de relevância:** com o e5, os scores ficam comprimidos (~0,75–0,90) e perguntas dentro e fora do escopo se sobrepõem. O limiar padrão (0,75) só descarta o que está muito longe; quem decide o escopo de fato é o classificador.
