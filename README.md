# Ziyno Addons for Minecraft 26.1.2

Client-side Fabric mod providing:

- ability countdown HUD driven by server time packets
- blood-camp timing commands (`/camps` and `/allcamps`)
- protection against accidentally placing player-head items with right-click lore
- removal of bat wings while preserving the rest of the bat model
- depth-tested white outlines around levers in a 24-block scan radius
- classic sword-blocking animations while holding right-click with a sword
- removal of mouse-driven first-person hand sway
- faster camera eye-height smoothing

Lever outlines are enabled by default. Use `/leverhitbox toggle` to toggle them.
Use `/nohandsway toggle` and `/lbtimer toggle` for the other optional features.
All three toggle states persist in `config/ziyno-addons.properties`.

## Requirements

- Minecraft Java Edition 26.1.2
- Java 25
- Fabric Loader 0.19.3 or newer
- Fabric API 0.154.2+26.1.2

## Build

On Windows:

```powershell
.\gradlew.bat build
```

The compiled mod is written to `build/libs/ziyno-addons-1.1.jar`.

## Install

Place `ziyno-addons-1.1.jar` and the matching Fabric API JAR in the
Minecraft instance's `mods` folder.
