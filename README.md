# Структуры белков — каркас на Compose Multiplatform

Учебное кроссплатформенное приложение по базе данных пространственных структур белков и макромолекул (RCSB PDB).
Веха В1: каркас приложения на Compose Multiplatform, без сети.

## Предметная область

Каталог пространственных структур белков. Источник данных — [search.rcsb.org](https://search.rcsb.org/rcsbsearch/v2/query) (RCSB Protein Data Bank), публичное REST API без авторизации.

Сущности домена:

| Сущность | Что описывает | Ключевые поля |
|---|---|---|
| `Protein` | пространственная структура белка / макромолекулы | PDB ID, краткое и полное название, организм-источник, классификация, метод исследования, разрешение, масса (кДа), число мономеров, дата релиза, авторы, связанные PDB ID |
| `ProteinMethod` | метод экспериментального определения структуры | X-ray diffraction, Cryo-Electron Microscopy, Solution NMR, Neutron diffraction, Other |

Экраны:

- **Список белков** — 24 записи, поиск по PDB ID, названию и организму. У карточки видны PDB ID, краткое название, организм, бейдж метода и разрешение со цветовой шкалой качества.
- **Карточка белка** — подробное досье: полное название, PDB ID, классификация, организм, метод исследования, разрешение (Å), молекулярная масса, число мономеров/остатков, дата депонирования и авторы. Ниже блок «Связанные структуры»: родственные и похожие PDB-записи, каждая из которых открывается как новый экран со своим шагом в бэкстеке.

В В1 сети нет: данные лежат в моках в форме ответов API RCSB PDB Search v2 / Data API. В В2 моки заменит сеть, а домен и UI останутся прежними.

## Быстрый запуск

Нужен JDK 17 или выше (протестировано на JDK 17 и 21). Остальное Gradle скачает сам, первый sync может занять некоторое время.

### 1. Запуск десктопного приложения (основной таргет)
```bash
./gradlew :desktopApp:run
```
*(на Windows: `.\gradlew.bat :desktopApp:run`)*

### 2. Запуск с английской локализацией (English)
```bash
./gradlew :desktopApp:run -Plocale=en
```

### 3. Запуск веб-приложения (Wasm / JS)
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```
или на JS:
```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

### 4. Сборка Android-приложения (нужен настроенный Android SDK)
```bash
./gradlew :androidApp:assembleDebug
```

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

## Проверки

Паритет ключей в локалях:

```bash
python tools/check-strings.py
```

Отчёт компилятора Compose о стабильности и пропуске рекомпозиции (после сборки лежит в `shared/build/compose_compiler/`):

```bash
./gradlew :shared:compileKotlinJvm
```

Запуск unit-тестов репозитория и навигации:

```bash
./gradlew :shared:jvmTest
```

## Структура

| Путь | Что внутри |
|---|---|
| `shared/src/commonMain/kotlin/.../domain` | сущности `Protein`, `ProteinMethod` и интерфейс `ProteinRepository` без деталей транспорта |
| `shared/src/commonMain/kotlin/.../data` | моки (24 структуры в форме ответа RCSB PDB) и реализация репозитория |
| `shared/src/commonMain/kotlin/.../list`, `.../detail` | состояние, намерения и ViewModel экранов (MVI) |
| `shared/src/commonMain/kotlin/.../ui/screens` | composable-экраны списка и детали, рисуются из состояния |
| `shared/src/commonMain/kotlin/.../ui/navigation` | маршруты `Screen`, бэкстек `Navigator` и контейнер переходов `AppNavDisplay` |
| `shared/src/commonMain/kotlin/.../ui/model` | UI-модели карточки и деталей, мапперы из домена |
| `shared/src/commonMain/kotlin/.../ui/components` | переиспользуемые компоненты (шапка со слотом действий, бейджи, подложки) |
| `shared/src/commonMain/kotlin/.../ui` | тема `AppTheme` на токенах Material 3 и семантические цвета `SemanticColors` |
| `shared/src/.../Platform.*.kt` | платформенные реализации определения и смены локали (включая JS/Wasm интероп) |
| `shared/src/commonMain/composeResources` | строковые ресурсы интерфейса (`values/strings.xml`, `values-en/strings.xml`) и векторные иконки |
| `tools/check-strings.py` | сверка ключей локалей |

## Технические решения

- **Навигация** — Navigation 3, мультиплатформенная сборка UI-части (`org.jetbrains.androidx.navigation3:navigation3-ui`). Маршрут — объект: `sealed interface Screen` (`Screen.List`, `Screen.Detail`). Бэкстек — реактивный список в `StateFlow` внутри `Navigator`. Заданы анимации перехода и возврата (`popEnterTransition`, `popExitTransition`).
- **Состояние** — MVI: вниз в composable уходят только состояние и колбэк намерений, ViewModel целиком не передаётся (сохраняет функции `restartable skippable`).
- **Тема** — токены `MaterialTheme`, светлая и тёмная схемы. Ни одного зашитого hex-цвета в поверхностях. Семантические цвета (метод исследования и шкала разрешения) не зависят от темы.
- **Подписи** — `composeResources`, две локали (`ru`, `en`). Язык и тема переключаются в шапке и живут в памяти сессии. Для Web реализован динамический JS-интероп для актуализации `navigator.languages`.
- **Данные каталога не переводятся**: названия белков, PDB ID и организмы остаются на английском, как в базе RCSB PDB.
