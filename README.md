# Структуры белков — приложение на Compose Multiplatform

Кроссплатформенное приложение для исследования пространственных структур белков и макромолекул на базе публичного API Protein Data Bank (RCSB PDB).  
**Веха В2:** интеграция с сетью (Ktor 3, kotlinx.serialization, пагинация, параллельная загрузка и кэширование).

---

## Предметная область

Каталог пространственных структур макромолекул. Источник данных — официальное REST API [RCSB Protein Data Bank](https://search.rcsb.org/index.html):
- **Search API v2:** `https://search.rcsb.org/rcsbsearch/v2/query` (полнотекстовый поиск, фильтрация по атрибутам, пагинация, полимерные сущности).
- **Data API v1 Core:** `https://data.rcsb.org/rest/v1/core` (детальные метаданные структур `entry/{id}` и полимерных сущностей `polymer_entity/{entry_id}/{entity_id}`).

### Сущности домена

| Сущность | Что описывает | Ключевые поля |
|---|---|---|
| `Protein` | Пространственная структура белка / макромолекулы | PDB ID, краткое и полное название, организм-источник, классификация, метод исследования, разрешение, масса (кДа), число мономеров, дата релиза, авторы, цитирование, связанные PDB ID |
| `ProteinMethod` | Метод экспериментального определения структуры | `XRAY` (Рентгеноструктурный анализ), `ELECTRON_MICROSCOPY` (Крио-ЭМ), `NMR` (ЯМР-спектроскопия), `NEUTRON` (Нейтронография), `OTHER` |

### Экраны приложения

- **Список белков (Каталог):**
  - Бесконечный скролл (пагинация) с динамической подгрузкой следующих страниц при прокрутке.
  - Поиск в реальном времени по PDB ID, названию белка или организму.
  - На карточке отображаются: PDB ID, человекочитаемое название, организм, классификация, масса, количество мономеров, цветной бейдж метода и бейдж разрешения со шкалой качества.
  - Состояния: начальная загрузка (`CircularProgressIndicator`), пустая поисковая выдача, индикатор подгрузки следующей страницы внизу списка, экран сетевой ошибки с кнопкой «Повторить».
- **Детали структуры (Карточка белка):**
  - Подробные параметры: PDB ID, полное наименование, классификация, организм, метод, разрешение (Å), молекулярная масса (кДа), количество аминокислотных остатков, дата депонирования.
  - Блок авторов исследования и название первичной публикации.
  - Блок «Связанные структуры» (полимерные сущности, полученные запросом `return_type: "polymer_entity"`): родственные субъединицы и комплексы, каждый из которых открывается как самостоятельный экран в бэкстеке навигации.
  - Обработка сетевых ошибок с возможностью повтора запроса.

---

## Архитектура и технические решения (Веха В2)

1. **Мультиплатформенный HTTP-клиент (Ktor Client 3):**
   - Механизм `expect`/`actual` для сборки под каждую целевую платформу:
     - **JVM (Desktop):** `OkHttp` (`io.ktor:ktor-client-okhttp`)
     - **Android:** `OkHttp` (`io.ktor:ktor-client-okhttp`) + сетевое разрешение `android.permission.INTERNET`
     - **Web (WasmJS / JS):** `Js` (`io.ktor:ktor-client-js`)
     - **iOS:** `Darwin` (`io.ktor:ktor-client-darwin`)
   - Единая фабрика клиента `HttpClientFactory` с установленным плагином `ContentNegotiation`.

2. **Сериализация и изоляция моделей (`kotlinx.serialization`):**
   - Модели сетевого ответа (DTO: `RcsbSearchResponseDto`, `RcsbEntryDto`, `RcsbPolymerEntityResponseDto`) строго изолированы от доменной модели `Protein` и UI-моделей экрана.
   - Парсер сконфигурирован с `ignoreUnknownKeys = true` и `isLenient = true`, защищая приложение от поломок при изменении и расширении схемы внешнего API RCSB.

3. **Корутины и безопасность отмены (`CancellationException`):**
   - Все сетевые операции оформлены как `suspend`-функции в интерфейсе `ProteinRepository`.
   - Исключение `CancellationException` нигде не проглатывается широкими блоками `catch (e: Exception)` и явно пробрасывается дальше (`throw e`). Быстрый ввод в строке поиска отменяет предыдущий Job без отображения ложной ошибки.

4. **Вынос тяжёлых операций с главного потока:**
   - Сетевые запросы, разбор JSON-ответов, маппинг и склейка страниц (`items + newItems`) выполняются на диспетчере `Dispatchers.Default` внутри `withContext`. Главный UI-поток не блокируется.

5. **Бесконечный список и пагинация:**
   - Постраничная загрузка (`page`, `pageSize`).
   - На первом экране выдаются канонические структуры предметной области; при прокрутке запрашиваются следующие страницы из базы RCSB.
   - При текстовом поиске пагинация запрашивает соответствующие страницы поисковой выдачи.

6. **Стабильность UI (`restartable skippable`):**
   - Классы состояний экранов (`ProteinListState`, `ProteinDetailState`) и UI-модели (`ProteinCardUi`, `ProteinDetailUi`) аннотированы `@Immutable`, гарантируя пропуск избыточных рекомпозиций Compose при частых обновлениях потока данных.

7. **Человекочитаемые наименования (Smart Name Cleaning):**
   - Алгоритм `cleanHumanName` очищает сырые формулы нуклеотидных цепочек (`5'-D(...)`), отсекает технические префиксы статей (*"The crystal structure of..."*, *"Cryo-EM structure of..."*) и суффиксы разрешения, приводя заголовки к аккуратному Title Case с сохранением биологических акронимов (*DNA*, *RNA*, *GFP*, *NMR*, *SARS*, *COVID*).
   - Синтетические конструкции корректно идентифицируются как `Synthetic construct`, без ложного отнесения к `Homo sapiens`.

8. **Конфигурация без захардкоженных адресов:**
   - Базовые URL API вынесены в `gradle.properties` (`rcsb.search.url`, `rcsb.data.url`).
   - На этапе сборки Gradle-задача `generateBuildConfig` формирует типобезопасный объект `BuildConfig` в `build/generated/buildconfig`.

9. **Кэширование в памяти:**
   - Потокобезопасный in-memory кэш на базе `Mutex` и `MutableMap<String, Protein>`. Данные на диск не сохраняются (в соответствии с ограничениями вехи).

10. **Интернационализация и темы:**
    - Строковые ресурсы в `composeResources` с паритетом ключей (RU / EN), переключение языка и темы (светлая/тёмная на токенах Material 3) на лету.

---

## Быстрый запуск

Требуется JDK 17 или выше (протестировано на JDK 17 и OpenJDK 21).

### 1. Запуск десктопного приложения (основной таргет)
```bash
./gradlew :desktopApp:run
```
*(на Windows: `.\gradlew.bat :desktopApp:run`)*

### 2. Запуск с английской локализацией (English)
```bash
./gradlew :desktopApp:run -Plocale=en
```

### 3. Запуск веб-приложения (WasmJS / WebAssembly)
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```
или на JS:
```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

### 4. Сборка Android-приложения (APK)
```bash
./gradlew :androidApp:assembleDebug
```
*Собранный APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`*

---

## Демонстрация работы

| WebApp |
| :---: |
|<img width="700" height="480" alt="web-" src="https://github.com/user-attachments/assets/7eb99266-548e-4ed5-a8cd-fb5bdb8e1ae5" />  |

| DesktopApp |
| :---: |
|<img width="640" height="400" alt="desctop-app" src="https://github.com/user-attachments/assets/fe3e2c49-c5b4-46f4-957d-71c53154adc5" />|

| AndroidApp |
| :---: |
|<img width="480" height="854" alt="android-app" src="https://github.com/user-attachments/assets/883b9797-f781-47c0-8e17-60d6f868cccb" />|

---

## Проверки и качество кода

Паритет строковых ресурсов в локалях (0 расхождений):
```bash
python tools/check-strings.py
```

Запуск unit-тестов (проверка DTO, `ignoreUnknownKeys`, маппинга, очистки названий и репозитория):
```bash
./gradlew :shared:jvmTest
```

Компиляция таргетов для проверки сборки:
```bash
./gradlew :shared:compileKotlinJvm :shared:compileAndroidMain :webApp:compileKotlinWasmJs
```

---

## Структура проекта

| Модуль / Каталог | Назначение |
|---|---|
| `shared/src/commonMain/kotlin/.../domain` | Сущности `Protein`, `ProteinMethod` и контракт репозитория `ProteinRepository` |
| `shared/src/commonMain/kotlin/.../data` | Реализация `ProteinRepositoryImpl` (сеть RCSB, пагинация, in-memory кэш) |
| `shared/src/commonMain/kotlin/.../data/network` | Фабрика `HttpClientFactory` и `expect fun createHttpClientEngine()` |
| `shared/src/*/kotlin/.../data/network` | Платформенные движки Ktor (`actual fun createHttpClientEngine()`: OkHttp / Js / Darwin) |
| `shared/src/commonMain/kotlin/.../data/dto` | DTO-модели (`RcsbSearchDto`, `RcsbEntryDto`, `RcsbPolymerEntityDto`) и мапперы `Mappers.kt` |
| `shared/src/commonMain/kotlin/.../list` | Состояние `ProteinListState`, намерения `ProteinListIntent` и `ProteinListViewModel` |
| `shared/src/commonMain/kotlin/.../detail` | Состояние `ProteinDetailState`, намерения `ProteinDetailIntent` и `ProteinDetailViewModel` |
| `shared/src/commonMain/kotlin/.../ui/screens` | Composable-экраны каталога и деталей с состояниями загрузки/ошибки |
| `shared/src/commonMain/kotlin/.../ui/navigation` | Навигация Navigation 3: маршруты `Screen`, бэкстек `Navigator`, хост `AppNavDisplay` |
| `shared/src/commonMain/kotlin/.../ui/model` | UI-модели `ProteinCardUi`, `ProteinDetailUi` (аннотированы `@Immutable`) |
| `shared/src/commonMain/kotlin/.../ui/components` | UI-компоненты: `AppScaffold`, карточки, `MethodBadge`, `ResolutionBadge`, секции |
| `shared/src/commonMain/kotlin/.../ui` | Тема `AppTheme` (Material 3 токены) и семантические цвета `SemanticColors` |
| `shared/src/commonMain/composeResources` | Ресурсы строк (`values/strings.xml`, `values-en/strings.xml`) и векторная графика |
| `androidApp/` | Точка входа Android (Activity, Manifest с разрешением `INTERNET`) |
| `desktopApp/` | Точка входа Desktop JVM (`Main.kt`) |
| `webApp/` | Точка входа WebAssembly WasmJS и JS (`main.kt`, `index.html`) |
| `tools/check-strings.py` | Скрипт верификации паритета локалей |

---

## Статус вехи В2 и дальнейшие шаги

- **Асинхронность на корутинах** (`suspend`-функции, `StateFlow`, без колбэков).
-  **`CancellationException` не проглочена** (отмена запроса не перехватывается как ошибка сети).
-  **Клиент собран под каждый таргет** (`expect`/`actual` Ktor-клиенты для JVM, Android, WasmJS, JS, iOS).
-  **Разбор ответа — `kotlinx.serialization`** (изолированные DTO, `ignoreUnknownKeys = true`).
-  **Тяжёлые вычисления вне UI-потока** (парсинг, маппинг и склейка страниц на `Dispatchers.Default`).
-  **Экраны `restartable skippable`** (`@Immutable` модели и состояния).
-  **Бесконечный скролл (пагинация)** каталога с индикатором дозагрузки.
-  **Кэш только в памяти** (без сохранения на диск, персистентность — задача В3).
- **Отсутствие хардкода адресов** (конфигурация через `gradle.properties` и автогенерация `BuildConfig`).

### Задачи следующей вехи (В3):
- Персистентность и офлайн-режим (локальная база данных Room / SQLDelight, синхронизация).
- Адаптивная верстка под ширину экрана (планшеты и десктопные окна с раздельными панелями Master-Detail).
- Интеграция глубоких ссылок (Deep Links) и адресной строки браузера.
