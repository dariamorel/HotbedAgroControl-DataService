# HotbedAgroControl-DataService

DataService - это backend-сервис, написанный на Kotlin и Spring Boot для сбора данных с устройства HotbedAgroControl по протоколу MQTT. Сервис сохраняет историю в PostgreSQL  табличку и выдает данные по REST API. Контракт API описан в OpenAPI и используется для генерации моделей и интерфейсов при сборке.

## Реализовано:

- **REST API**:
  - регистрация пользователя/устройства с параметрами MQTT (`POST /users`)
  - удаление пользователя (`DELETE /users/{id}`)
  - получение истории измерений по датчику и периоду (`GET /history/{id}`)
- **MQTT**: подключение к брокеру по данным из БД, подписка на топики, разбор сообщений и запись в таблицу истории
- **PostgreSQL** + **Flyway**: таблица и миграции к ней
- **Swagger UI**
- сборка через **docker-compose** приложения, БД и pgAdmin.

## Запуск через Docker Compose

Из корня репозитория:

```bash
docker compose up -d --build
```

Остановка (данные в БД сохраняются):

```bash
docker compose down
```

Полная остановка с удалением данных из БД:

```bash
docker compose down -v
```

### Swagger UI

Swagger UI доступен по адресу:

**http://localhost:8081/swagger-ui.html**

### pgAdmin

1. PgAdmin доступен по адресу: **http://localhost:5050**
2. Вход с учётными данными из `docker-compose.yml`
3. Сервер PostgreSQL:
   - **Host**: `postgres`
   - **Port**: `5432`
   - **Username / Password**: `postgres`
   - **Database**: `history_db`
