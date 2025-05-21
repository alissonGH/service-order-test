# Order Management Service

## Visão Geral

Aplicação Spring Boot para gerenciamento de pedidos que recebe pedidos de um sistema externo, calcula o valor total dos produtos, armazena no banco de dados PostgreSQL, mantém cache em Redis e publica eventos no Kafka para sistemas downstream.

## Regra base

### Order
A classe Order representa uma entidade de domínio no contexto de DDD (Domain-Driven Design). Ela encapsula regras e comportamentos relacionados ao conceito de pedido dentro do domínio da aplicação. Essa classe é o agregado raiz (Aggregate Root) de um agregado que inclui uma lista de produtos (Product), garantindo a consistência das invariantes do pedido como um todo.

A classe também implementa comportamentos fundamentais do domínio:

calculateTotal(): método que aplica uma regra de negócio de validação e cálculo do valor total do pedido, assegurando que nenhum produto tenha preço ou quantidade negativos.

updateStatus(String): permite a mudança do status do pedido, respeitando o ciclo de vida da entidade.

Através de suas operações e atributos, a entidade Order centraliza as decisões de negócio relacionadas a um pedido e é responsável por manter sua integridade, de acordo com os princípios do DDD. Isso promove um modelo rico de domínio, onde as regras e comportamentos estão próximos dos dados que manipulam, ao invés de serem dispersos em serviços externos.

### OrderService
A classe OrderService é responsável pela lógica de negócio relacionada ao processamento e recuperação de pedidos (Order). Ela atua integrando o banco de dados, o cache Redis e o sistema de mensageria Kafka para garantir o processamento eficiente e a persistência dos pedidos.

O método processOrder(Order order) verifica se um pedido com o mesmo externalId já está presente no cache Redis. Caso esteja, registra essa informação no log e interrompe o processamento para evitar duplicidade. Caso contrário, o pedido é salvo no cache e enviado para um tópico Kafka, por meio do OrderProducer, para processamento assíncrono.

O método processOrderAsync(Order order) é anotado com @Transactional para garantir a atomicidade da operação. Ele verifica se o pedido já está registrado no banco de dados por meio do externalId. Se existir, apenas registra essa informação no log e não faz nada. Caso contrário, calcula o valor total do pedido, atualiza o status para "PROCESSADO", converte o domínio Order para a entidade JPA OrderEntity (incluindo a validação e busca dos produtos no banco), salva o pedido no banco e também no cache Redis. Em caso de erro, lança uma exceção interna customizada para sinalizar falha no processamento.

O método toEntity(Order order) faz a conversão do objeto de domínio Order para a entidade OrderEntity para persistência. Ele garante que todos os produtos associados ao pedido existam no banco, buscando-os via ProductRepository. Caso algum produto não seja encontrado, uma exceção é lançada.

Por fim, o método getOrderByExternalId(String externalId) realiza a busca do pedido primeiro no cache Redis. Se o pedido não for encontrado no cache, busca no banco de dados e, caso exista, o converte para domínio, salva no cache e o retorna. Se não encontrado em nenhum dos dois locais, lança uma exceção informando que o pedido não foi encontrado.

Essa classe é fundamental para garantir a consistência dos dados, performance no acesso via cache e integração com o sistema de mensagens para processamento assíncrono dos pedidos.


---

## Tecnologias Utilizadas

- Java 21 / Spring Boot 3.4.5
- PostgreSQL (via Docker Compose)
- Redis (via Docker Compose)
- Kafka + Zookeeper (via Docker Compose)
- Docker Compose para orquestração local dos serviços externos
- Kubernetes para deploy e balanceamento da aplicação Spring Boot
- JUnit 5 + Mockito para testes unitários
- Jackson para serialização/deserialização JSON
- Lombok para redução de boilerplate

---

## Estrutura do Projeto

- **domain/** - Classes de domínio (modelo de negócio)
- **model/** - Entidades JPA mapeadas para banco
- **repository/** - Interfaces Spring Data para acesso a dados
- **service/** - Lógica de negócio e integração com Kafka e Redis
- **controller/** - Endpoints REST
- **mapper/** - Conversores entre entidades, domínio e DTOs

---

## Configuração

### Docker Compose (Ambiente local)

Contém os serviços externos:

- PostgreSQL rodando na porta 5432
- Redis na porta 6379
- Kafka e Zookeeper (KAFKA na 9092)

Arquivo: `docker-compose.yml`

```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  orderdb:
    image: postgres:16.3-alpine
    environment:
      POSTGRES_USER: user
      POSTGRES_PASSWORD: pass
      POSTGRES_DB: orderdb
    ports:
      - "5432:5432"
    volumes:
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
