# Better Create Kinetic Loss (Forge 1.20.1, KotlinForForge)

Adds a virtual stress consumer to each Create kinetic network so transmission infrastructure (shafts/cogs/gearboxes/belts) contributes configurable SU loss.

## Version targeting

- Minecraft: 1.20.1
- Forge: 47.4.13
- KotlinForForge: 4.11.0
- Create compile target: **6.0.8-291**
- Create declared compatibility: **6.0+**
- Ponder: **1.0.92**

## Testing strategy

Yes — Forge provides a built-in headless framework via **GameTest**. This project now includes:

- Unit tests for deterministic math/cache behavior: `src/test/kotlin/...`
- Forge GameTests for mod runtime assertions: `src/main/kotlin/.../gametest/...`
- A `gameTestServer` run config with `forge.enabledGameTestNamespaces=better_create_kinetic_loss`

Useful commands:

- `./gradlew verifyFast`
- `./gradlew verifyFull`
- In game: `/demo transloss`
- In game: `/transloss debug here`

## Current implementation notes

- Includes common config for coefficients and speed scaling modes (`NONE`, `LINEAR`, `QUADRATIC`).
- Adds cache primitives and dirty-marking hooks.
- Samples real Create network members at runtime via reflection and registry IDs.
- Adds in-world commands: `/transloss debug here`, `/transloss recalc here`, `/transloss profile`, `/demo transloss`.
- Belt counting strategy is defined as **pulley-to-pulley Manhattan span** for stability across Create internals.

## Release status

- Release validations pass via `./gradlew verifyFull`.
- Unit tests and GameTests are wired and passing.
- JaCoCo coverage gates are enforced for core packages in `build.gradle.kts`.

## Gradle wrapper

This repository pins Gradle to **8.8** via `gradle/wrapper/gradle-wrapper.properties`.

The Gradle wrapper, including `gradle/wrapper/gradle-wrapper.jar`, is tracked so the documented wrapper commands work from a fresh checkout.
> If needed locally/CI, regenerate it with:
>
> ```bash
> gradle wrapper --gradle-version 8.8
> ```

## Community and support

For modpack and mod discussion, playtest feedback, and bug reports, join the [Better Content Discord](https://discord.gg/EkRnZbzqS9).

## Canonical identity

- Repository and Gradle project: `better-create-kinetic-loss`
- Mod ID and resource namespace: `better_create_kinetic_loss`
- Maven group: `com.bettercontent`
- Runtime artifact: `build/libs/better-create-kinetic-loss-<version>.jar`

The canonical identity is a clean break. Legacy mod IDs, resource namespaces, configuration paths, commands, network channels, and saved-data keys are not migrated or aliased.
