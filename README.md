# SoulHolo

Донатные голограммы для ваших игроков.

Игрок ставит текст только **в своём регионе** — на чужой земле не получится. **ЛКМ** по своей голограмме — GUI, **ПКМ** — показать ID (имя для команд). Команды `add`, `remove`, `edit` работают с последней выбранной голограммой.

**Сколько голограмм** — решает админ через `soulholo.limit.<N>`: число в праве = лимит в регионе. Это же право открывает `/dholo` и GUI — отдельные `soulholo.use` / `soulholo.gui` не нужны. Без `limit` — плагин недоступен. Лимит строк и длина текста — в `config.yml`.

На каждой **новой** голограмме внизу висит подпись владельца — она сама пропадает, как только игрок напишет свой текст (удалять вручную не нужно).

## Особенности

- Полная кастомизация
- Использование только оптимизированных решений
- Удаление голограмм при удалении региона
- Бесконечное количество голограмм

## Команды

`/dholo create <название>` — создать голограмму  
`/dholo gui [название]` — открыть GUI  
`/dholo add [текст]` — добавить строку (сначала ПКМ/ЛКМ по голограмме)  
`/dholo remove <номер>` — удалить строку  
`/dholo edit <номер> <текст>` — изменить строку  
`/dholo line <up|down> <номер>` — переставить строку  
`/dholo move <up|down|left|right>` — сдвинуть голограмму  
`/dholo setting <настройка> [+/-]` — display-настройка  
`/dholo reload` — перезагрузка config, gui, messages  

Админ (`soulholo.admin`): `/dholo admin …` — без лимитов; **создать** можно только в своём регионе (owner), как и все.

## Права на плагин

### Базовый доступ (игрок)

| Право | Назначение |
|-------|------------|
| `soulholo.limit.<N>` | Единственное обязательное право: **N** — сколько голограмм можно в своём регионе. Даёт `/dholo`, GUI, ЛКМ по своей голограмме. Без него — ничего из плагина. |
| `soulholo.admin` | `/dholo admin …`, `/dholo reload`, обход лимитов (default: OP) |

Если у игрока несколько `soulholo.limit.*`, берётся **наибольшее N**.

Примеры (LuckPerms):

```
/lp group vip permission set soulholo.limit.1 true
/lp group premium permission set soulholo.limit.3 true
```

### Тонкая настройка GUI (опционально)

Работают только при наличии `soulholo.limit.<N>`. В `plugin.yml` у большинства стоит `default: true` — можно **запретить** отдельным группам.

| Право | Что даёт | default |
|-------|----------|---------|
| `soulholo.gui.lines` | Редактирование строк в GUI | true |
| `soulholo.gui.position` | Перемещение в GUI | true |
| `soulholo.gui.setting.enabled` | Вкл/выкл голограмму | true |
| `soulholo.gui.setting.see-through` | Сквозь блоки (дефолт вкл.) | true |
| `soulholo.gui.setting.text-shadow` | Тень текста | true |
| `soulholo.gui.setting.billboard` | Billboard / поворот | true |
| `soulholo.gui.setting.background` | Все пресеты фона (14 цветов в дефолте) | true |
| `soulholo.gui.setting.scale` | Масштаб | false |
| `soulholo.gui.setting.text-alignment` | Выравнивание текста | false |
| `soulholo.gui.setting.shadow` | Тень (radius/strength) | false |

**Не используются:** `soulholo.use`, `soulholo.gui`, `soulholo.gui.bg.*` — достаточно `soulholo.limit.<N>` и `soulholo.gui.setting.background`.
