FROM eclipse-temurin:25-jdk AS backend-build

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src/ src/

RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:25-jre AS model

WORKDIR /models
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl && \
    rm -rf /var/lib/apt/lists/*
COPY scripts/download-embedding-model.sh /download-embedding-model.sh
RUN sh /download-embedding-model.sh /models/multilingual-e5-small

FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl && \
    update-ca-certificates && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=model --chown=appuser:appgroup /models /app/models
COPY --from=backend-build --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser

ENV PORT=8080 \
    EMBEDDING_MODEL_PATH=/app/models/multilingual-e5-small/model_quantized.onnx \
    EMBEDDING_TOKENIZER_PATH=/app/models/multilingual-e5-small/tokenizer.json \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -fsS http://localhost:${PORT}/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
