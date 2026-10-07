# WR4TH  (Minecraft 1.21.11 · Fabric)

A combined utility client inspired by Wurst, Doomsday and Meteor, with a grey-and-black click GUI.

## Build
Requires **JDK 21+**.

    ./gradlew build          (Windows: gradlew.bat build)

The jar lands in `build/libs/wr4th-1.0.0.jar`. Drop it in `.minecraft/mods` together with
**Fabric Loader 0.19.x** and **Fabric API 0.141.6+1.21.11** for Minecraft 1.21.11.
To test from source: `./gradlew runClient`.

## Controls
| Action                  | Input                         |
|-------------------------|-------------------------------|
| Open / close click GUI  | Right Shift                   |
| Toggle module           | Left-click                    |
| Show module settings    | Right-click module            |
| Bind a key              | Middle-click module, press key (Esc/Backspace = clear) |
| Move panel              | Drag its header               |
| Collapse panel          | Right-click header            |

Keybinds and settings are saved to `config/wr4th.json`.

## Modules (24)
| Category | Modules | Inspired by |
|----------|---------|-------------|
| Combat   | KillAura, TriggerBot, Velocity, AutoTotem | Wurst / Meteor / Doomsday |
| Movement | Flight, Speed, BunnyHop, Sprint, NoFall, Step, Spider, Jesus, AirJump, AutoWalk | Wurst / Meteor |
| Player   | FastPlace, AutoRespawn, AutoEat, AntiAFK, Panic | Wurst / Meteor |
| Render   | HUD, Fullbright, ESP | Meteor |
| World    | Nuker, Scaffold | Wurst / Meteor |

## Adding a module
1. Add a class extending `Module` in one of `net.wr4th.modules.*`
2. Declare settings with `add(new BoolSetting/NumSetting/ModeSetting(...))`
3. Register it in `ModuleManager.init()`. The GUI, HUD list and config pick it up automatically.

## Notes
Use only where cheats are allowed (singleplayer, your own server, or servers that permit it).
Most public servers ban hacked clients.
