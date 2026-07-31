# Cloud IoT House Simulation

Командный full-stack проект для управления виртуальным умным домом и моделирования IoT-устройств. Пользователь создаёт устройства и группы, назначает действия и расписания, а backend выполняет команды через MQTT и интеграции Yandex Cloud.

Ярослав Абузов отвечал за backend; клиентская часть реализована на React в рамках командного проекта.

## Возможности

- регистрация, JWT-аутентификация и роли пользователей;
- CRUD для устройств, типов, групп и доступных действий;
- расписания и периодические сценарии на Quartz;
- запуск и остановка симуляции;
- выполнение команд через MQTT;
- генерация и распознавание речи через SpeechKit;
- отправка логов в Yandex Cloud Logging;
- React-интерфейс и reverse proxy на Nginx;
- PostgreSQL для пользователей, конфигурации и расписаний;
- OpenAPI и Spring REST Docs.

## Архитектура

```text
React UI
   ↓
Nginx
   ├──→ Auth Service ──→ PostgreSQL
   └──→ Control Service ──→ PostgreSQL / Quartz
                         ├──→ Yandex IoT Core (MQTT)
                         ├──→ SpeechKit
                         └──→ Cloud Logging
```

Backend разделён на два Spring Boot-приложения:

- `facc-auth` — пользователи, роли, JWT и управление профилем;
- `facc-control` — устройства, группы, действия, расписания и облачные интеграции.

## Стек

Java 17, Spring Boot 3, Spring Security, Spring Data JPA, Quartz, PostgreSQL, MQTT, Yandex Cloud SDK, React 18, Vite, Material UI, Nginx и Docker Compose.

## Локальная сборка

Создайте `.env` из шаблона и задайте собственные значения:

```bash
cp .env.example .env
```

Для локального режима облачные интеграции можно оставить отключёнными. Для MQTT, SpeechKit и Cloud Logging заполните соответствующие `YANDEX_*` переменные и смонтируйте авторизованный key-файл вне Git.

Backend:

```bash
cd facc-back
mvn test
```

Frontend:

```bash
cd facc-front
npm install
npm run build
```

`docker-compose.yml` отражает исходное deployment-окружение и использует опубликованные командой Docker images. Для полностью локального запуска следует добавить PostgreSQL и заменить image-секции backend-сервисов на `build`.

## Основные API

- `/api/v1/auth/*` — регистрация, вход и управление пользователями;
- `/api/v1/device/devices` — устройства;
- `/api/v1/device/groups` — группы;
- `/api/v1/device/actions` — действия;
- `/api/v1/scheduler/timetable-entries` — расписание;
- `/api/v1/scheduler/simulation/state` — состояние симуляции.

## Безопасность

Все секреты вынесены в environment variables, `.env` исключён из Git. Ключи Yandex Cloud и JWT, ранее присутствовавшие в истории репозитория, необходимо отозвать и перевыпустить до публикации. При развёртывании используйте HTTPS, отдельный service account с минимальными правами и managed secret storage.

## Статус

Учебный командный проект. Он демонстрирует backend-интеграцию с IoT, планировщиком и облачными API; compose-конфигурация требует адаптации для воспроизводимого публичного запуска.
