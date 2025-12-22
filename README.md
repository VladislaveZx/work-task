1. Общие сведения
Прототип распределенной системы обработки заявок (тикетов), реализованный в виде двух независимых микросервисов на основе Java 21 и Spring Boot 3. Система демонстрирует архитектурные паттерны для построения событийно-ориентированных приложений с использованием реактивного стека технологий.

2. Архитектурный обзор
2.1. Компоненты системы
Компонент	Назначение	Порт	База данных
ticket-api-service	Предоставление REST API для управления жизненным циклом заявок	8081	tickets_db
ticket-processor-service	Асинхронная обработка заявок, расчет бизнес-метрик	8082	processed_tickets_db

4. Технологический стек
3.1. Основные технологии
Java 21

Spring Boot 4.0.0

Spring WebFlux (реактивные REST endpoints)

Spring Data R2DBC (реактивный доступ к данным)

Apache Kafka (асинхронная коммуникация)

PostgreSQL (реляционное хранение данных)

3.2. Инструменты мониторинга и наблюдения
Micrometer + Spring Boot Actuator (сбор метрик)

Log4j2 с JSON-форматированием (структурированное логирование)

Spring AOP (аспектно-ориентированное программирование)

Docker Compose (оркестрация контейнеров)

4. Функциональные спецификации
4.1. Сервис ticket-api-service
4.1.1. REST API endpoints
Создание заявки

http
POST /api/v1/tickets
Content-Type: application/json

{
  "title": "Сбой в работе системы",
  "description": "Описание проблемы",
  "category": "TECH"
}
Параметры валидации:

Поле title: обязательное, максимальная длина 255 символов

Поле description: необязательное, максимальная длина 2000 символов

Поле category: допустимые значения: TECH, PAYMENT, OTHER

Получение заявки по идентификатору

http
GET /api/v1/tickets/{id}
Постраничное получение заявок с фильтрацией

http
GET /api/v1/tickets?status=NEW&page=0&size=20
4.1.2. Поток обработки
Валидация входящих данных

Сохранение в PostgreSQL с использованием R2DBC

Публикация события TicketCreated в топик Kafka tickets.created

Возврат ответа клиенту

4.2. Сервис ticket-processor-service
4.2.1. Обработка событий
Подписка на топик Kafka tickets.created

Расчет атрибутов на основе категории:

Приоритет: HIGH (TECH), CRITICAL (PAYMENT), LOW (OTHER)

Время SLA: 24 часа (TECH), 4 часа (PAYMENT), 48 часов (OTHER)

Сохранение обогащенной сущности в отдельную схему данных

4.2.2. REST API endpoints
Получение агрегированной статистики

http
GET /api/v1/stats/summary
Формат ответа:

json
{
  "totalTickets": 150,
  "byStatus": {
    "NEW": 25,
    "IN_PROGRESS": 75,
    "DONE": 45,
    "REJECTED": 5
  }
}

Получение обработанных заявок

http
GET /api/v1/tickets/processed?status=IN_PROGRESS&page=0&size=10
5. Конфигурация и развертывание
5.1. Предварительные требования

Docker Compose 2.20+

JDK 21

Apache Maven 4.0.0

5.2. Процедура развертывания
Шаг 1: Клонирование репозитория

bash
git clone <repository-url>
cd distributed-ticket-system
Шаг 2: Сборка артефактов

bash
mvn clean package
Шаг 3: Запуск инфраструктурных сервисов

bash
docker-compose up -d 
Шаг 4: Запуск микросервисов

bash
java -jar ticket-api-service/target/ticket-api-service.jar
java -jar ticket-processor-service/target/ticket-processor-service.jar

6. Мониторинг и диагностика
6.1. Endpoints для мониторинга
Endpoint	Назначение	Доступность
/actuator/health	
/actuator/metrics	
/actuator/prometheus

6.2. Кастомные метрики
ticket-api-service:

ticket_api_tickets_created_total (Counter) — количество созданных тикетов

ticket_api_request_latency_seconds (Timer) — гистограмма времени ответа

ticket_api_tickets_new_gauge (Gauge) — количество тикетов в статусе NEW

ticket-processor-service:

ticket_processor_messages_consumed_total (Counter) — обработанные сообщения

ticket_processor_messages_failed_total (Counter) — ошибки обработки

ticket_processor_kafka_available (Gauge) — доступность Kafka-кластера

6.3. Система сквозной идентификации (Correlation ID)
Реализация:

Веб-фильтр перехватывает входящие HTTP-запросы

Извлекает заголовок X-Correlation-Id или генерирует новый UUID

Помещает идентификатор в MDC (Mapped Diagnostic Context)

Пропагирует через HTTP-заголовки и Kafka-сообщения

Автоматически включает в структурированные логи

Пример логической записи:

json
{
  "timestamp": "2024-01-15T14:30:15.123Z",
  "level": "INFO",
  "logger": "com.example.ticketapi.controller.TicketController",
  "service": "ticket-api-service",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "message": "Обработан HTTP-запрос",
  "httpMethod": "POST",
  "httpPath": "/api/v1/tickets",
  "durationMs": 156,
  "statusCode": 201
}
7. Аспектно-ориентированное программирование
7.1. Аннотация @LogExecution
java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecution {
    String value() default "";
    boolean trackMetrics() default true;
}
7.2. Аспект LoggingAspect
Выполняет следующие функции:

Логирование начала выполнения метода с параметрами

Измерение времени выполнения

Обработка и классификация исключений

Интеграция с метриками Micrometer

Поддержка реактивных типов (Mono/Flux)

7.3. Применение в коде
java
@Service
public class TicketService {
    
    @LogExecution(value = "Создание тикета", trackMetrics = true)
    public Mono<Ticket> createTicket(TicketRequest request) {
        // Реализация бизнес-логики
    }
}
8. Структура данных
8.1. Схема tickets (ticket-api-service)
sql
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL CHECK (category IN ('TECH', 'PAYMENT', 'OTHER')),
    status VARCHAR(50) NOT NULL DEFAULT 'NEW' 
        CHECK (status IN ('NEW', 'IN_PROGRESS', 'DONE', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT title_length CHECK (length(title) <= 255)
);
8.2. Схема processed_tickets (ticket-processor-service)
sql
CREATE TABLE processed_tickets (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    priority VARCHAR(50) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    sla_hours INTEGER NOT NULL CHECK (sla_hours > 0),
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(36)
);
9. Форматы сообщений Kafka
9.1. Событие TicketCreated
json
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
  },
  "metadata": {
    "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "sourceService": "ticket-api-service",
    "version": "1.0"
  }
}
10. Тестирование
10.1. Стратегия тестирования
Модульные тесты: покрытие критической бизнес-логики (JUnit 5, Mockito)

Интеграционные тесты: взаимодействие с БД и Kafka (Testcontainers)

Реактивные тесты: валидация потоков данных (StepVerifier)
