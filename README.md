**1\. Общие сведения**

Прототип распределенной системы обработки заявок (тикетов), реализованный в виде двух независимых микросервисов на основе Java 21 и Spring Boot 4. Система демонстрирует архитектурные паттерны для построения событийно-ориентированных приложений с использованием реактивного стека технологий.

**2\. Архитектурный обзор**

**2\.1. Компоненты системы**

| **Компонент**                | **Назначение**                                                 | **Порт** | **База данных**      |
|------------------------------|----------------------------------------------------------------|----------|----------------------|
| **ticket-api-service**       | Предоставление REST API для управления жизненным циклом заявок | 8081     | tickets_db           |
| **ticket-processor-service** | Асинхронная обработка заявок, расчет бизнес-метрик             | 8082     | processed_tickets_db |

**3\. Технологический стек**

**3\.1. Основные технологии**

-  Java 21

-  Spring Boot 4.0.0

-  Spring WebFlux (реактивные REST endpoints)

-  Spring Data R2DBC (реактивный доступ к данным)

-  Apache Kafka (асинхронная коммуникация)

-  PostgreSQL  (реляционное хранение данных)

**3\.2. Инструменты мониторинга и наблюдения**

-  Micrometer + Spring Boot Actuator (сбор метрик)

-  Log4j2 с JSON-форматированием (структурированное логирование)

-  Spring AOP (аспектно-ориентированное программирование)

-  Docker Compose (оркестрация контейнеров)

**4\. Функциональные спецификации**

**4\.1. Сервис ticket-api-service**

**4\.1.1. REST API endpoints**

**Создание заявки**

```
POST /api/v1/tickets
Content-Type: application/json

{
  "title": "Сбой в работе системы",
  "description": "Описание проблемы",
  "category": "TECH"
}
```

**Параметры валидации:**

-  Поле `title`: обязательное, максимальная длина 255 символов

-  Поле `description`: необязательное, максимальная длина 2000 символов

-  Поле `category`: допустимые значения: TECH, PAYMENT, OTHER

**Получение заявки по идентификатору**

```
GET /api/v1/tickets/{id}
```

**Постраничное получение заявок с фильтрацией**

```
GET /api/v1/tickets?status=NEW&page=0&size=20
```

**4\.1.2. Поток обработки**

1. Валидация входящих данных

2. Сохранение в PostgreSQL с использованием R2DBC

3. Публикация события `TicketCreated` в топик Kafka `tickets.created`

4. Возврат ответа клиенту

**4\.2. Сервис ticket-processor-service**

**4\.2.1. Обработка событий**

-  Подписка на топик Kafka `tickets.created`

-  Расчет атрибутов на основе категории:

   -  **Приоритет**: HIGH (TECH), CRITICAL (PAYMENT), LOW (OTHER)

   -  **Время SLA**: 24 часа (TECH), 4 часа (PAYMENT), 48 часов (OTHER)

-  Сохранение обогащенной сущности в отдельную схему данных

**4\.2.2. REST API endpoints**

**Получение агрегированной статистики**

```
GET /api/v1/stats/summary
```

Формат ответа:

```
{
  "totalTickets": 150,
  "byStatus": {
    "NEW": 25,
    "IN_PROGRESS": 75,
    "DONE": 45,
    "REJECTED": 5
  },
  "avgProcessingTimeSeconds": 2345.67
}
```

**Получение обработанных заявок**

```
GET /api/v1/tickets/processed?status=IN_PROGRESS&page=0&size=10
```

**5\. Конфигурация и развертывание**

**5\.1. Предварительные требования**

-  Docker Compose

-  JDK 21

-  Apache Maven 4.0.0

**5\.2. Процедура развертывания**

**Шаг 1: Клонирование репозитория**

```
git clone <repository-url>
cd distributed-ticket-system
```

**Шаг 2: Сборка артефактов**

```
mvn clean package -DskipTests
```

**Шаг 3: Запуск инфраструктурных сервисов**

```
docker-compose up -d postgres-kafka
```

**Шаг 4: Запуск микросервисов**

```
java -jar ticket-api-service/target/ticket-api-service-1.0.0.jar
java -jar ticket-processor-service/target/ticket-processor-service-1.0.0.jar
```

**6\. Мониторинг и диагностика**

**6\.1. Endpoints для мониторинга**

| **Endpoint**           | **Назначение**                | **Доступность**    |
|------------------------|-------------------------------|--------------------|
| `/actuator/health`     | Состояние здоровья приложения | Все среды          |
| `/actuator/metrics`    | Доступные метрики             | Только development |
| `/actuator/prometheus` | Метрики в формате Prometheus  | Все среды          |

**6\.2. Кастомные метрики**

**ticket-api-service:**

-  `ticket_api_tickets_created_total` (Counter) -- количество созданных тикетов

-  `ticket_api_request_latency_seconds` (Timer) -- гистограмма времени ответа

-  `ticket_api_tickets_new_gauge` (Gauge) -- количество тикетов в статусе NEW

**ticket-processor-service:**

-  `ticket_processor_messages_consumed_total` (Counter) -- обработанные сообщения

-  `ticket_processor_messages_failed_total` (Counter) -- ошибки обработки

-  `ticket_processor_kafka_available` (Gauge) -- доступность Kafka-кластера

**6\.3. Система сквозной идентификации (Correlation ID)**

**Реализация:**

1. Веб-фильтр перехватывает входящие HTTP-запросы

2. Извлекает заголовок `X-Correlation-Id` или генерирует новый UUID

3. Помещает идентификатор в MDC (Mapped Diagnostic Context)

4. Пропагирует через HTTP-заголовки и Kafka-сообщения

5. Автоматически включает в структурированные логи

**Пример логической записи:**

```
{
  "timestamp": "2024-01-15T14:30:15.123Z",
  "level": "INFO",
  "logger": "com.example.ticketapi.controller.TicketController",
  "service": "ticket-api-service",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "message": "Обработан HTTP-запрос",
  "httpMethod": "POST",
  "httpPath": "/api/v1/tickets",
  "statusCode": 201
}
```

**7\. Аспектно-ориентированное программирование**

**7\.1. Аннотация @LogExecution**

```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecution {
    String value() default "";
    boolean trackMetrics() default true;
}
```

**7\.2. Аспект LoggingAspect**

Выполняет следующие функции:

-  Логирование начала выполнения метода с параметрами

-  Измерение времени выполнения

-  Интеграция с метриками Micrometer

-  Поддержка реактивных типов (Mono/Flux)

**7\.3. Применение в коде**

```
@Service
public class TicketService {
    
    @LogExecution(value = "Создание тикета", trackMetrics = true)
    public Mono<Ticket> createTicket(TicketRequest request) {
        // Реализация бизнес-логики
    }
}
```

**8\. Структура данных**

**8\.1. Схема tickets (ticket-api-service)**

```
create table tickets (
                         id uuid primary key default gen_random_uuid(),
                         title varchar(255) not null,
                         description text,
                         category varchar(50) not null,
                         status varchar(50) not null,
                         created_at timestamptz not null
);
```

**8\.2. Схема processed_tickets (ticket-processor-service)**

```
create table processed_tickets (
        id uuid primary key,
        title varchar(255) not null,
        description text,
        category varchar(50) not null,
        status varchar(50) not null,
        created_at timestamptz not null,
        priority varchar(50) not null,
        sla_hours int not null,
        processed_at timestamptz not null
);
```

**9\. Форматы сообщений Kafka**

**9\.1. Событие TicketCreated**

```
{
  "eventId": "event-uuid",
  "eventType": "TicketCreated",
  "timestamp": "2024-01-15T14:30:15.123Z",
  "payload": {
    "ticketId": "ticket-uuid",
    "title": "Заголовок заявки",
    "description": "Подробное описание",
    "category": "TECH",
    "createdAt": "2024-01-15T14:30:15.123Z"
}
```

**10\. Тестирование**

**10\.1. Стратегия тестирования**

-  **Интеграционные тесты**: взаимодействие с БД и Kafka (Testcontainers)

-  **Реактивные тесты**: валидация потоков данных (StepVerifier)
