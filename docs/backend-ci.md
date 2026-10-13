# Backend CI

Проверка в `.github/workflows/postgres-migrations.yml` запускает **все**
бэкенд-тесты, а не только миграции: при push в любую ветку, при pull request
и вручную через Actions → PostgreSQL migrations → Run workflow.
Прежние имя workflow и job `migrations` сохранены для существующих проверок PR.

Используются Java 17, Maven Wrapper проекта и профиль `test`.
Команда `bash ./mvnw --batch-mode --no-transfer-progress clean verify` выполняет
тесты и проверку сборки без фильтра `-Dtest`.

Docker проверяется перед тестами. PostgreSQL 16 создаётся Testcontainers
в отдельных временных контейнерах; локальная и рабочая БД не используются.
Тестовый JWT-ключ берётся из `application-test.yml`, секреты репозитория не нужны.

После запуска CI проверяет отчёты: пустой набор, ошибки или пропуски тестов
завершают проверку неуспешно. Оба PostgreSQL-теста должны присутствовать.
Отчёты Surefire/Failsafe доступны в артефакте `backend-test-reports` 14 дней,
включая неуспешные запуски. Повторный push отменяет устаревший запуск для
той же ветки или pull request.

Для проверки локально на Java 17: `./mvnw.cmd clean verify` на Windows или
`bash ./mvnw clean verify` на Linux. Нужен запущенный Docker для PostgreSQL-тестов.
Проверка полноты отчётов: `python .github/scripts/verify_test_reports.py`.

Конфигурация подготовлена локально. Чтобы она начала работать в GitHub,
нужно отправить изменения в репозиторий. Обязательность этой проверки перед
слиянием настраивается отдельно в правилах ветки GitHub.
