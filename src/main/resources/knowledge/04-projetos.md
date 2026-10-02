# Projetos pessoais do Nicolas

## Resilience Lab

Laboratório de resiliência em arquitetura hexagonal com Java 25, Spring Boot 4 e Resilience4j. Demonstra seis padrões de resiliência, medidos no Grafana e no Prometheus com testes de carga em k6 e dependências simuladas com WireMock. Tem testes de arquitetura com ArchUnit. Repositório: github.com/niicsz/resilience-lab-hexagonal.

## BiniTech PDV

Sistema de Ponto de Venda (PDV) SaaS multi-tenant, focado na gestão de caixa. Back-end em Java 21 e Spring Boot com arquitetura hexagonal e abordagem API first (OpenAPI). Usa MongoDB, Redis, RabbitMQ para envio assíncrono de e-mails e Stripe para assinaturas e cobrança. Tem CI com build, testes e análise de dependências com OWASP Dependency Check. Repositório: github.com/niicsz/BiniTech-PDV.

## BiniTech PDV Frontend

SPA em Angular 21 do BiniTech PDV, com fluxos de venda, gestão, relatórios e assinatura via Stripe. Repositório: github.com/niicsz/BiniTech-PDV-frontend.

## BiniTech Auth

Serviço reutilizável de autenticação com arquitetura hexagonal, Java 21, Spring Boot, MongoDB, JWT e hash de senhas com Argon2. É usado pelo BiniTech PDV. Repositório: github.com/niicsz/BiniTech-Auth.

## Logística CEP API

API REST para consulta de CEPs com abordagem API first, padrões de resiliência, testes de integração e infraestrutura na AWS provisionada com Terraform. Repositório: github.com/niicsz/cep-api.

## URL Shortener

Encurtador de URLs rápido e escalável construído com Java e banco de dados relacional. Repositório: github.com/niicsz/url-shortener.

## Portfolio Interview API

Esta própria API: o assistente "Interview me" do portfólio, em Java 25 e Spring Boot com arquitetura hexagonal e API first. Usa RAG com embeddings locais (multilingual-e5-small em ONNX) e o Claude, da Anthropic, para responder perguntas sobre a carreira do Nicolas, com várias camadas de proteção contra prompt injection e perguntas fora do escopo.
