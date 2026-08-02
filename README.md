![Schematicannon](title.png)

# Schematicannon for Minecraft 26.2

An unofficial, standalone NeoForge port of the schematic tools and Schematicannon gameplay originally found in Create.

This mod does not require Create at runtime. It uses its own mod id, registries, networking, saved data and menus while preserving the familiar workflow: capture a build, prepare the schematic, position its preview and let the cannon construct it from supplied materials.

> **Status:** active development. The port is playable, but Minecraft 26.2 and its NeoForge toolchain are still evolving. Back up important worlds before testing.

## Features

- Schematic and Quill area selection
- Schematic Table compilation
- movable, rotatable and flippable in-world previews
- Schematicannon construction with nearby container support
- material checklist clipboard
- vanilla structure `.nbt` schematics
- direct `.litematic` import
- English and Russian localization

## Requirements

- Minecraft `26.2`
- NeoForge `26.2.0.35-beta` or newer compatible build
- Java version required by Minecraft 26.2

Create is not a runtime dependency.

## Using schematics

1. Select an area with the Schematic and Quill and save it.
2. Put a blank schematic into the Schematic Table and select the saved file.
3. Hold the completed schematic to position, rotate or mirror its preview, then confirm placement.
4. Put the schematic and required materials into the Schematicannon, or place a compatible container with materials nearby.
5. Configure the cannon and start printing.

Litematica files can be placed in the schematic directory and selected through the Schematic Table. The importer supports vanilla blocks and attempts to retain compatible block-state properties and block-entity data.

## Building

On Windows:

```powershell
.\gradlew.bat clean build
```

The built mod is written to `build/libs/`.

## Credits and relationship to Create

This is an independent, unofficial project and is not affiliated with, sponsored by or endorsed by the Create team.

The original schematic mechanics and portions of the implementation were derived from [Create](https://github.com/Creators-of-Create/Create), created by simibubi and the Creators of Create contributors. Some visual and audio resources in this development repository originate from or are adapted from Create. Ownership and license terms for those materials remain with their respective authors.

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) before redistributing this project or its compiled artifacts.

The standalone Schematicannon project is based on [michiel1106/Create-schematicannon](https://github.com/michiel1106/Create-schematicannon). This Minecraft 26.2 NeoForge port is maintained by PeemKay1 and preserves the original Git history.

## License

Project code is provided under the [MIT License](LICENSE), except for third-party material identified in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). The project license does not grant additional rights to third-party assets.
