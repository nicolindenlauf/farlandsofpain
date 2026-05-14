# Farlands of Pain

Farlands of Pain is a NeoForge mod for Minecraft 1.21.1 that makes non-player mobs scale with world position, depth, nearby player level, entity health, and world age.

## Mod Overview

- Scales non-player living entities with configurable formulas.
- Can increase max health, movement speed, knockback resistance, mob damage, bonus loot rolls, and dropped XP.
- Adds `/pain-o-meter`, a command that toggles a small client overlay with aligned labels and right-aligned values.
- Uses NeoForge `ModConfigSpec`, so the config can be edited in-game with Configured when Configured is installed.

## Configuration

Config file: `config/farlandsofpain-common.toml`

Formula variables:

- `x_coord`, `y_coord`, `z_coord`: evaluated coordinates after origin and dimension-offset handling.
- `dist_from_spawn`: horizontal distance from the configured origin. Kept as the short legacy name.
- `horizontal_distance_from_spawn`: same value as `dist_from_spawn`, but with a clearer name.
- `distance_from_world_origin`: horizontal distance from real world coordinate `0,0`, independent of spawn settings.
- `vertical_depth`: how far below the evaluated Y origin the entity is. Values above that origin are `0`.
- `nearest_player_level`: experience level of the nearest player within 256 blocks, or `0` if none is found.
- `nearest_player_health_percent`: nearest player's current health as `0..100`, or `0` if none is found.
- `nearby_player_count`: non-spectator players within 256 blocks.
- `nearby_mob_count`: non-player living entities within 32 blocks, excluding the entity being evaluated.
- `entity_health`: current health of the entity being evaluated.
- `entity_max_health`: maximum health of the entity being evaluated.
- `current_day`: world time divided by 24000.
- `moon_phase`: current moon phase as `0..7`.
- `world_difficulty`: vanilla difficulty as `0` Peaceful, `1` Easy, `2` Normal, or `3` Hard.
- `local_difficulty`: vanilla local difficulty at the entity position, including world age, inhabited time, moon brightness, and difficulty.

Formula operators:

- `+`, `-`, `*`, `/`, `^`, `%`

Formula functions:

- `min(a, b)`, `max(a, b)`, `clamp(value, min, max)`, `round(value)`
- `pow(a, b)`: same idea as `a ^ b`, but often easier to read in long formulas.
- `lerp(a, b, t)`: linear interpolation from `a` to `b`.
- `smoothstep(edge0, edge1, x)`: smooth `0..1` ramp between two edges.
- `logistic(x, midpoint, steepness, max)`: curved ramp that approaches `max` instead of growing forever.
- `select(condition, whenNonZero, whenZero)`: returns the second value when `condition` is not `0`, otherwise the third value.
- `gt(a, b)`, `gte(a, b)`, `lt(a, b)`, `lte(a, b)`, `eq(a, b)`: comparison helpers that return `1` for true and `0` for false.
- exp4j built-ins such as `abs`, `ceil`, `floor`, `sqrt`, `sin`, `cos`, `tan`, `log`, and related math functions

Main options:

- `general.dimensionOffsetsJson`: JSON map of dimension IDs to Y-coordinate offsets. This lets formulas treat dimensions as deeper or higher than the Overworld.
- `general.zeroZeroOrigin`: Use world coordinate `0,0` instead of world spawn as the horizontal origin.
- `formulas.healthFormula`: Multiplier for mob maximum health.
- `formulas.damageFormula`: Multiplier for damage dealt by scaled non-player mobs.
- `formulas.lootFormula`: Loot multiplier. The mod converts this to extra loot rolls by subtracting `1` and rounding down.
- `formulas.xpFormula`: Multiplier for XP dropped by scaled non-player mobs.
- `formulas.speedFormula`: Multiplier for mob movement speed.
- `formulas.knockbackFormula`: Multiplier for mob knockback resistance.
- `client.overlayX`: Pain-O-Meter overlay X position in screen pixels, counted from the left edge.
- `client.overlayY`: Pain-O-Meter overlay Y position in screen pixels, counted from the top edge.
- `client.overlayUpdateIntervalTicks`: How often the overlay recalculates formula values. `20` ticks is roughly one second.
- `debug.debugLogging`: Enables detailed debug logging for formula evaluation and scaling decisions.

Safety limits:

- `limits.maxHealthMultiplier`: Maximum health multiplier after formula evaluation.
- `limits.maxDamageMultiplier`: Maximum damage multiplier after formula evaluation.
- `limits.maxSpeedMultiplier`: Maximum movement-speed multiplier after formula evaluation.
- `limits.maxKnockbackMultiplier`: Maximum knockback-resistance multiplier after formula evaluation.
- `limits.maxXpMultiplier`: Maximum XP multiplier after formula evaluation.
- `limits.maxExtraLootRolls`: Maximum extra loot rolls from a single mob death.

## Compatibility

- Minecraft: `1.21.1`
- Loader: NeoForge `21.1.228+`
- Java: `21`
- Side: Both client and server. Gameplay scaling is applied server-side, and relevant config values are synced to the client-side Pain-O-Meter overlay.
- Optional companion mod: Configured for in-game config editing

## Development Checklist

- Local `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build` passes.
- GitHub Actions build passes on push and pull request.
- CI uploads `build/libs/*.jar` artifacts from workflow runs.
- Local runtime includes Configured as an optional dev-only dependency for config screen smoke tests.

Runtime smoke test checklist:

- Start the client run and confirm Configured opens the Farlands of Pain config screen.
- Run `/pain-o-meter` and confirm chat says the overlay was enabled or disabled, then confirm the overlay appears near the configured screen position.
- Move near spawn and far from spawn, then compare the live overlay values. The overlay updates on `client.overlayUpdateIntervalTicks`, not every frame.
- Temporarily edit a formula in `config/farlandsofpain-common.toml`, reload or restart, and confirm it is accepted or rejected cleanly.
- Test an intentionally invalid formula and confirm the config rejects it instead of crashing gameplay.
- Kill a scaled mob and confirm bonus loot respects `limits.maxExtraLootRolls`.
- Confirm scaled mobs also drop XP according to `formulas.xpFormula` and `limits.maxXpMultiplier`.

## License

All Rights Reserved for original Farlands of Pain code and assets.

NeoForge MDK template files remain under their original MIT license; see `TEMPLATE_LICENSE.txt`.
