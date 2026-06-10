# SoulHolo

**Парящий текст над твоей базой — без табличек, без мусора, без чужих рук.**

SoulHolo — это твои личные голограммы на **своей** земле. Магазин у входа в приват, табличка «добро пожаловать» у клан-холла, красивый прайс вместо стены из досок — всё это делается за минуту, прямо в игре.

Ты **владелец** региона WorldGuard? Значит, можешь ставить. Чужая территория — нет. Никто не сотрёт и не подменит твою голограмму: она привязана к тебе и к твоему региону.

### Что умеешь ты (игрок)

- **`/dholo gui`** — всё через меню: список голограмм, строки, цвета, фон, размер, положение
- **Цвета и hex** — `&6`, `&#FF5555`, MiniMessage: текст выглядит как на нормальном сервере, не как серая простыня
- **Строки** — добавить, изменить, удалить, переставить; не нравится GUI — те же действия командами (`add`, `edit`, `remove`, `line`)
- **Двигать не ломая** — сдвиг вверх/вниз/влево/вправо, голограмма не вылезет за границы твоего региона
- **Подпись владельца** — на каждой голограмме видно, чья она (защита от «кто это поставил?»)
- **Донат = больше** — больше голограмм на регион, больше строк, длиннее текст (tier выдаёт админ через LuckPerms)

### Быстрый старт

```
/dholo create shop          ← создать у себя под ногами
/dholo gui                  ← открыть список и крутить настройки
```

Или сразу текстом:

```
/dholo add &#FFD700Магазин &7открыт 24/7
/dholo add &fЛучшие цены на спавне
```

Открой **`/dholo gui`**, кликни по голограмме — там кнопки **«Текст»** и **«Положение»**. Большинству игроков команд кроме `create` и `gui` хватает с запасом.

---

*Ниже — для тех, кто настраивает сервер (зависимости, config, админ-команды).*

## Зависимости

| Плагин | Обязательность |
|--------|----------------|
| [Paper](https://papermc.io/) 1.21+ | да |
| [WorldGuard](https://enginehub.org/worldguard) | да — регион и проверка owner |
| [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) | опционально |
| [DecentHolograms](https://github.com/DecentSoftware-eu/DecentHolograms) / [FancyHolograms](https://modrinth.com/plugin/fancyholograms) | опционально — только если `hologramBackend: decent` или `fancy` |

**По умолчанию (`auto` / `paper`)** голограммы — встроенные **TextDisplay** (Paper API). Сторонние плагины голограмм **не нужны**.

Данные хранятся в `plugins/SoulHolo/` — чужие storage Decent/Fancy не используются. При первом старте Elytrium создаёт `config.yml` и `gui/general.yml`; тексты — `messages.yml`.

## Команды

| Команда | Описание |
|---------|----------|
| `/dholo gui [название]` | Список голограмм или сразу настройки указанной |
| `/dholo create <название>` | Создать голограмму на текущей позиции (в регионе, где вы owner) |
| `/dholo add [текст]` | Добавить строку к **активной** голограмме |
| `/dholo remove <номер>` | Удалить строку |
| `/dholo edit <номер> <текст>` | Изменить строку |
| `/dholo line <up\|down> <номер>` | Переставить строку выше/ниже (те же права, что GUI строк) |
| `/dholo move <up\|down\|left\|right>` | Сдвинуть голограмму в пределах региона |
| `/dholo setting <настройка> [+/-]` | Переключить display-настройку (как кнопки в GUI) |
| `/dholo reload` | Перезагрузить config, gui layout, messages (`soulholo.admin`) |

После `create` голограмма становится **активной** для `add` / `remove` / `edit` / `move` / `line` / `setting` (или берётся ближайшая в радиусе из config).

### GUI (`/dholo gui`)

- Список **своих** голограмм с пагинацией
- Клик по голограмме → экран настроек display entity
- **Текст голограммы** — добавить/изменить/удалить/переставить строки (дублирует `add`, `remove`, `edit`, `line`)
- **Положение** — сдвиг по сторонам света (дублирует `move`)
- Настройки display — дублируют `/dholo setting`
- Каждая настройка и каждый пресет фона — **отдельный permission** в `config.yml` → `gui.settings` / `gui.background-presets`
- **Backend `paper` (по умолчанию):** все настройки display через TextDisplay
- **Backend `decent` / `fancy`:** опционально; у Decent — только enabled + billboard

Layout слотов и материалов — `plugins/SoulHolo/gui/general.yml`. Тексты — `messages.yml` (`gui.*`).

### Админ (`soulholo.admin`)

Без проверки региона и лимитов. Если админ вне регионов WG — `admin.fallbackRegionId` в config.

| Команда | Описание |
|---------|----------|
| `/dholo admin create <название> [игрок]` | Создать голограмму (позиция игрока; owner — указанный или вы) |
| `/dholo admin add <голограмма> [текст]` | Добавить строку |
| `/dholo admin remove <голограмма> <номер>` | Удалить строку |
| `/dholo admin edit <голограмма> <номер> <текст>` | Изменить строку |
| `/dholo admin move <голограмма> <up\|down\|left\|right>` | Сдвинуть голограмму |
| `/dholo admin line <голограмма> <up\|down> <номер>` | Переставить строку |
| `/dholo admin setting <голограмма> <настройка> [+/-]` | Display-настройка |
| `/dholo reload` | То же, что `/dholo admin reload` — config + gui + messages |

## Лимиты и права

Каждый **tier** в `config.yml` → `limits.tiers` задаёт:

| Параметр | Что ограничивает |
|----------|------------------|
| `permission` | Permission-нода (выдаёшь через LuckPerms и т.п.) |
| `maxHologramsPerRegion` | Сколько голограмм можно в одном регионе |
| `maxLines` | Строк контента (без подписи владельца) |
| `maxLineLength` | Длина одной строки |

Игроку берётся tier с **наибольшими** лимитами из тех, на которые у него есть permission.

```yaml
limits:
  countScope: owner-region
  defaultTier:
    permission: soulholo.limit.default
    maxHologramsPerRegion: 3
    maxLines: 10
    maxLineLength: 50
  tiers:
    vip:
      permission: soulholo.limit.vip
      maxHologramsPerRegion: 8
      maxLines: 15
      maxLineLength: 80
    premium:
      permission: soulholo.limit.premium
      maxHologramsPerRegion: 15
      maxLines: 25
      maxLineLength: 120
```

Добавляешь новый донат — просто новый блок в `tiers` + permission в LuckPerms. Хардкода tier'ов в Java нет.

## Производительность

| Что | Как |
|-----|-----|
| Запись YAML | Отдельный IO-пул (`performance.io-threads`), не main thread |
| Память | Обновляется **сразу** при save; на диск — async (без гонки add после create) |
| Старт сервера | Голограммы поднимаются **батчами** (`performance.restore-batch-size` за тик) |
| Лог действий | Запись в `actions.log` — async |
| Команды | Bukkit API (голограммы, WG) — только main thread, это норма |

```yaml
performance:
  restore-batch-size: 20
  io-threads: 1

selection:
  nearest-radius: 5.0   # если нет активной — ищет свою голограмму в радиусе
```

## Механики

- **Регион:** создать можно только в WorldGuard-регионе, где игрок — **owner** (не member, не чужая земля).
- **Подпись:** последняя строка всегда показывает владельца; текст в `config.yml` → `ownerLine` (`%player%`).
- **Лимиты (на tier):** голограмм на регион, строк на голограмму, длина строки.
- **Blacklist:** запрещённые слова и REGEX в `config.yml` → `blacklist`.
- **PlaceholderAPI:** плейсхолдеры в строках и в `owner-line`.
- **Логи:** создание и правки → console и `plugins/SoulHolo/actions.log`.

## Конфигурация

Конфиг — **Elytrium Serializer** (как в SoulPact): дефолты в Java (`SoulHoloSettings`, `GuiGeneralSettings`), при первом старте создаются файлы с комментариями. `reload()` мерджит новые ключи без сброса правок админа.

```
plugins/SoulHolo/
├── config.yml              ← SoulHoloSettings (лимиты, blacklist, GUI-права)
├── gui/general.yml         ← GuiGeneralSettings (слоты, материалы)
├── messages.yml            ← тексты (Bukkit YAML, отдельно)
├── actions.log
└── holograms/<uuid>.yml
```

Зависимость: `net.elytrium:serializer:1.1.1` в `plugin.yml` → `libraries` (Paper грузит, не shade).

Пример секций в сгенерированном `config.yml`:

```yaml
hologramBackend: auto
ownerLine: "&fГолограмма игрока: &a%player%"

limits:
  countScope: owner-region
  defaultTier:
    permission: soulholo.limit.default
    maxHologramsPerRegion: 3
    maxLines: 10
    maxLineLength: 50
  tiers:
    vip:
      permission: soulholo.limit.vip
      maxHologramsPerRegion: 8

gui:
  openPermission: soulholo.gui
  settings:
    seeThrough: soulholo.gui.setting.see-through
  backgroundPresets:
    - id: transparent
      permission: soulholo.gui.bg.transparent
```

### `messages.yml`

Все сообщения игрокам, GUI title/lore — ключи с плейсхолдерами `{prefix}`, `{name}`, `{region}`, `{limit}` и т.д.

## Пример (игрок)

Ты строишь магазин в своём привате:

```
/dholo create clan_shop
/dholo gui
```

В GUI добавляешь строки, подбираешь фон и масштаб — или одной пачкой:

```
/dholo add &#FFD700&lМагазин клана
/dholo add &7Открыто &aкруглосуточно
/dholo edit 1 &#FFAA00&l★ Магазин ★
```

Внизу автоматически висит подпись «чья голограмма» — из настроек сервера. Красиво, читаемо, не занимает блоки.

## Данные голограмм

Файлы в `plugins/SoulHolo/holograms/<uuid>.yml`: id, name, owner, region-id, координаты, строки, display, backend-id.

## Автор

b0b0b0
