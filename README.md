# ZenithProxyPlayerStatePlugin

[![Plugin Build](https://github.com/zychen027/PlayerStatusPlugin/actions/workflows/build.yml/badge.svg)](https://github.com/zychen027/PlayerStatusPlugin/actions/workflows/build.yml)

Player state query commands and a queue position monitor for [ZenithProxy](https://github.com/rfresh2/ZenithProxy) on **Minecraft 26.2.0**.

Ever wanted to check where your bot is standing, where it's looking, what effects it has, or what title is currently on its screen — directly from Discord / in-game chat / the proxy console? That's what this plugin does.

## Commands

| Command | Description |
|---|---|
| `pos` | The bot's current coordinates (exact + block position) |
| `perspective` | View direction: yaw / pitch / head yaw + compass facing (e.g. `north (down)`) |
| `playerStatus` | Everything in one view: health, active potion effects (with amplifier & remaining time), coordinates, view direction |
| `title` | What's currently on the bot's screen: title, subtitle and action bar — each shown independently, with remaining display time. `title status` prints packet counters for diagnostics |
| `queuewarn3c` | Queue position watcher — see below |

All commands work from Discord, in-game chat, and the terminal, just like built-in ZenithProxy commands.

### `queuewarn3c` — queue position monitor

Watches the title / subtitle / action bar packets the bot receives and extracts the number after a configurable keyword (default: `"Position in queue: "` — matches 2b2t-style queue subtitles like `555 Playing | Position in queue: 121`).

Console output:

- When the position **changes** → logged at `info` level, **yellow**
- When the position is **≤ threshold** (default `100`) → logged at `warn` level, **green**

```
[21:05:05] [Module] [INFO] [QueueWarn3c] position: 121
[21:06:12] [Module] [WARN] [QueueWarn3c] position: 98 (changed, now <= threshold 100)
```

Subcommands:

| Usage | Description |
|---|---|
| `queuewarn3c threshold <n>` | Set the warn threshold |
| `queuewarn3c toggle <on/off>` | Enable / disable the watcher |
| `queuewarn3c test <text...>` | Dry-run the extractor on any text (shows extracted number & log level, no console output) |

## Installation

1. Grab `ZenithProxyPlayerStatePlugin-<version>.jar` from [releases](../../releases) (or build it yourself, see below)
2. Drop it into your ZenithProxy instance's `plugins/` directory
3. Restart the proxy

Requirements:

- ZenithProxy **26.2.0**
- Minecraft **26.2.0**
- Java 25+

## Configuration

Persisted at `plugins/config/player-state.json`, managed automatically:

```json
{
  "queueWarn": {
    "enabled": true,
    "keyword": "Position in queue: ",
    "threshold": 100
  }
}
```

- `keyword` — the text to look for; the first integer appearing after it is treated as the position
- `threshold` — positions at or below this number are logged at warn level

You can also change these at runtime with the `queuewarn3c` subcommands above.

## Building from source

```bash
./gradlew build
```

The plugin jar will be in `build/libs/`.

Requires a JDK 25 toolchain (auto-provisioned by Gradle via foojay if missing).

## How it works

- `pos` / `perspective` / `playerStatus` read ZenithProxy's player cache (`EntityPlayer`) — the proxy's real-time mirror of the bot's state, so they work even while no human is connected to the proxy.
- ZenithProxy does not cache title packets, so this plugin registers a **client packet codec** that intercepts the full clientbound title packet family as the bot receives them from the destination server:
  - `ClientboundSetTitleTextPacket`
  - `ClientboundSetSubtitleTextPacket`
  - `ClientboundSetActionBarTextPacket`
  - `ClientboundSetTitlesAnimationPacket` (used to compute remaining display time)
  - `ClientboundClearTitlesPacket`
- `queuewarn3c` runs a small regex (`keyword + first following integer`) over the plain-serialized text of those packets and logs on changes.

## Compatibility

The `mcVersions` constraint is set to `26.2.0`. For other MC versions, change `mc` in `gradle.properties` and rebuild — the title packet API used here has been stable across recent versions.

## License

[CC0](LICENSE), following the upstream [ZenithProxyExamplePlugin](https://github.com/rfresh2/ZenithProxyExamplePlugin) template.

---

## 中文说明

**ZenithProxy 玩家状态查询 + 队列位置监控插件**（Minecraft 26.2.0 / ZenithProxy 26.2.0 / Java 25+）。

| 指令 | 功能 |
|---|---|
| `pos` | 输出 bot 当前坐标（精确 + 方块坐标） |
| `perspective` | 视角：yaw / pitch / head yaw + 朝向（八方位 + 俯仰） |
| `playerStatus` | 一屏输出：血量 / 状态效果（等级 + 剩余时长）/ 坐标 / 视角 |
| `title` | bot 屏幕上当前显示的标题 / 副标题 / 动作栏（独立展示 + 剩余显示时间）；`title status` 查看包计数诊断 |
| `queuewarn3c` | 队列位置监控 |

**queuewarn3c**：监听 bot 收到的标题类数据包，提取关键词（默认 `"Position in queue: "`，兼容 2b2t 式队列提示 `555 Playing | Position in queue: 121`）后的数字。位置变化时以 `info`（黄色）输出；位置小于等于阈值（默认 `100`）时以 `warn`（绿色）输出。

可用子命令：`queuewarn3c threshold <n>`（改阈值）、`queuewarn3c toggle <on/off>`（开关）、`queuewarn3c test <文本>`（试跑提取器）。

**安装**：将 jar 放入 ZenithProxy 的 `plugins/` 目录并重启。配置自动持久化在 `plugins/config/player-state.json`。

**构建**：`./gradlew build`，产物在 `build/libs/`。
