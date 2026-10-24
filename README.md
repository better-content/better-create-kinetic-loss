# Better Create Kinetic Loss (Forge 1.20.1, KotlinForForge)

## Scope

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
- Samples real Create network members and sources through the typed native API and registry IDs.
- Adds in-world commands: `/transloss debug here`, `/transloss recalc here`, `/transloss profile`, `/demo transloss`.
- Belt counting strategy is defined as **pulley-to-pulley Manhattan span** for stability across Create internals.

## Bounded playtest logging

`bc.playtest.better_create_kinetic_loss.network` emits completed scan/cache-publication
reasons and committed loss-overload/clear transitions at DEBUG. Stable cache hits do not
emit records. Scan details use existing sampled values; logging never rescans network
members. `network_pos_long` identifies the native canonical network block position in
its dimension, not a player identity. This ID can change with topology.

INFO startup reports effective enabled state, version, limits and Create availability;
source identity uses the exact owner's bounded Forge ModFile metadata read and is `UNKNOWN`
when unavailable; hook execution remains `UNKNOWN` until observed. An INFO startup is
not proof that the optional `require=0` hook applied. The native hook GameTest calls real
Create `calculateStress` on an isolated placed shaft, checks publication/cache reuse and
invalidation, and does not claim normal topology propagation or assembled-rig coverage.

The repository-local Java17 helper shares 120 DEBUG/TRACE details per minute with burst20
across categories, bounds aggregation to256 keys and records to2048 characters, and emits
one coalesced summary per30 seconds on native server ticks (plus final teardown). Server
stop clears native level bindings, cache and logging aggregation, without rearming TRACE
or replenishing the shared allowance. Standalone defaults remain native INFO; the pack's
scoped XML uses DEBUG and its `bc.playtest.level` override.

Focused TRACE requires logger TRACE plus JVM properties
`bc.playtest.trace.better_create_kinetic_loss.dimension=minecraft:overworld` and
`bc.playtest.trace.better_create_kinetic_loss.pos=x,y,z` (canonical network position).
The common helper alternatively accepts exactly one UUID subject, but this network
instrumentation has no subject identity, so use position. Invalid/absent focus emits only
one status; first matching detail arms at most60 seconds/1000 records once per process,
subject to the ordinary bucket. No seeds, inventories, NBT or player names are logged.

## Release status

- Run `./gradlew verifyFull` for source/runtime validation; historical passes are not acceptance of changed source.
- Unit tests and native GameTests are wired.
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

## Native source fixture safety

`verifyFull` runs both authored native GameTests in a fresh `build/gametest/<UUID>/`
fixture, separate from client `run/`. Preparation reads the authoritative ForgeGradle
RunConfig, creates only an absent comment-only `server.properties` (native defaults),
and copies authored structures there. Existing properties, worlds and logs are not reset.
The gate requires actual startup/shutdown, exactly two completed/passed required tests
and zero native ERROR/FATAL records. `verifyFast` exercises settings preservation and
negative diagnostic/population controls without Minecraft. Retain complete prior builds,
libraries, fixtures and evidence before any independently authorized clean/reset.

## Canonical identity

- Repository and Gradle project: `better-create-kinetic-loss`
- Mod ID and resource namespace: `better_create_kinetic_loss`
- Maven group: `com.bettercontent`
- Runtime artifact: `build/libs/better-create-kinetic-loss-<version>.jar`

The canonical identity is a clean break. Legacy mod IDs, resource namespaces, configuration paths, commands, network channels, and saved-data keys are not migrated or aliased.
