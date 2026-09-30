package com.bettercontent.bettercreatekineticloss.config

import com.bettercontent.bettercreatekineticloss.TransmissionLossMod
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig

object TransmissionLossConfig {
    private const val DEFAULT_RECALC_COOLDOWN_TICKS = 20
    private const val DEFAULT_INCLUDE_ENCASED_SHAFTS = true
    private const val DEFAULT_BASE_RPM = 32.0
    private const val DEFAULT_K = 0.5
    private const val DEFAULT_MAX_MULT = 3.0
    private const val DEFAULT_SHAFT = 0.05
    private const val DEFAULT_ENCASED_SHAFT = 0.07
    private const val DEFAULT_COGWHEEL = 0.10
    private const val DEFAULT_LARGE_COGWHEEL = 0.15
    private const val DEFAULT_GEARBOX = 0.20
    private const val DEFAULT_BELT_SEGMENT = 0.04
    private const val DEFAULT_BELT_PULLEY = 0.10
    private const val DEFAULT_CHAIN_DRIVE = 0.12

    private val builder = ForgeConfigSpec.Builder()

    val enabled: ForgeConfigSpec.BooleanValue
    val recalcCooldownTicks: ForgeConfigSpec.IntValue
    val includeEncasedShafts: ForgeConfigSpec.BooleanValue
    val countDecorative: ForgeConfigSpec.BooleanValue

    val speedMode: ForgeConfigSpec.EnumValue<SpeedMode>
    val baseRpm: ForgeConfigSpec.DoubleValue
    val k: ForgeConfigSpec.DoubleValue
    val maxMult: ForgeConfigSpec.DoubleValue

    val shaft: ForgeConfigSpec.DoubleValue
    val encasedShaft: ForgeConfigSpec.DoubleValue
    val cogwheel: ForgeConfigSpec.DoubleValue
    val largeCogwheel: ForgeConfigSpec.DoubleValue
    val gearbox: ForgeConfigSpec.DoubleValue
    val beltSegment: ForgeConfigSpec.DoubleValue
    val beltPulley: ForgeConfigSpec.DoubleValue
    val chainDrive: ForgeConfigSpec.DoubleValue

    val spec: ForgeConfigSpec

    init {
        builder.push("general")
        enabled = builder.comment("Master switch for applying transmission loss.").define("enabled", true)
        recalcCooldownTicks = builder
            .comment("Minimum ticks before recomputing a dirty network.")
            .defineInRange("recalcCooldownTicks", DEFAULT_RECALC_COOLDOWN_TICKS, 1, 1200)
        includeEncasedShafts = builder
            .comment("Whether encased shafts contribute loss separately from plain shafts.")
            .define("includeEncasedShafts", DEFAULT_INCLUDE_ENCASED_SHAFTS)
        countDecorative = builder
            .comment("Reserved compatibility toggle for decorative transmission parts.")
            .define("countDecorative", false)
        builder.pop()

        builder.push("speedScaling")
        speedMode = builder.comment("Scaling mode: NONE, LINEAR, QUADRATIC.").defineEnum("speedMode", SpeedMode.LINEAR)
        baseRpm = builder
            .comment("Reference RPM used for scaling normalization.")
            .defineInRange("baseRpm", DEFAULT_BASE_RPM, 0.001, 4096.0)
        k = builder
            .comment("Scaling factor applied by LINEAR/QUADRATIC modes.")
            .defineInRange("k", DEFAULT_K, 0.0, 1000.0)
        maxMult = builder
            .comment("Hard cap for speed multiplier.")
            .defineInRange("maxMult", DEFAULT_MAX_MULT, 1.0, 1000.0)
        builder.pop()

        builder.push("suLoss")
        shaft = builder.defineInRange("shaft", DEFAULT_SHAFT, 0.0, 1000.0)
        encasedShaft = builder.defineInRange("encasedShaft", DEFAULT_ENCASED_SHAFT, 0.0, 1000.0)
        cogwheel = builder.defineInRange("cogwheel", DEFAULT_COGWHEEL, 0.0, 1000.0)
        largeCogwheel = builder.defineInRange("largeCogwheel", DEFAULT_LARGE_COGWHEEL, 0.0, 1000.0)
        gearbox = builder.defineInRange("gearbox", DEFAULT_GEARBOX, 0.0, 1000.0)
        beltSegment = builder.defineInRange("beltSegment", DEFAULT_BELT_SEGMENT, 0.0, 1000.0)
        beltPulley = builder.defineInRange("beltPulley", DEFAULT_BELT_PULLEY, 0.0, 1000.0)
        chainDrive = builder.defineInRange("chainDrive", DEFAULT_CHAIN_DRIVE, 0.0, 1000.0)
        builder.pop()

        spec = builder.build()
    }

    fun register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec, "${TransmissionLossMod.MOD_ID}-common.toml")
    }

    fun recalcCooldownTicksValue(): Int = valueOrDefault(recalcCooldownTicks, DEFAULT_RECALC_COOLDOWN_TICKS)
    fun includeEncasedShaftsValue(): Boolean = valueOrDefault(includeEncasedShafts, DEFAULT_INCLUDE_ENCASED_SHAFTS)
    fun shaftValue(): Double = valueOrDefault(shaft, DEFAULT_SHAFT)
    fun encasedShaftValue(): Double = valueOrDefault(encasedShaft, DEFAULT_ENCASED_SHAFT)
    fun cogwheelValue(): Double = valueOrDefault(cogwheel, DEFAULT_COGWHEEL)
    fun largeCogwheelValue(): Double = valueOrDefault(largeCogwheel, DEFAULT_LARGE_COGWHEEL)
    fun gearboxValue(): Double = valueOrDefault(gearbox, DEFAULT_GEARBOX)
    fun beltSegmentValue(): Double = valueOrDefault(beltSegment, DEFAULT_BELT_SEGMENT)
    fun beltPulleyValue(): Double = valueOrDefault(beltPulley, DEFAULT_BELT_PULLEY)
    fun chainDriveValue(): Double = valueOrDefault(chainDrive, DEFAULT_CHAIN_DRIVE)

    fun speedMultiplier(rpm: Float): Double {
        val normalized = kotlin.math.abs(rpm.toDouble()) / valueOrDefault(baseRpm, DEFAULT_BASE_RPM).coerceAtLeast(0.001)
        val raw = when (valueOrDefault(speedMode, SpeedMode.LINEAR)) {
            SpeedMode.NONE -> 1.0
            SpeedMode.LINEAR -> 1.0 + valueOrDefault(k, DEFAULT_K) * normalized
            SpeedMode.QUADRATIC -> 1.0 + valueOrDefault(k, DEFAULT_K) * normalized * normalized
        }
        return raw.coerceAtMost(valueOrDefault(maxMult, DEFAULT_MAX_MULT))
    }

    enum class SpeedMode { NONE, LINEAR, QUADRATIC }

    internal fun <T> valueOrDefault(readValue: () -> T, defaultValue: T): T {
        return runCatching(readValue).getOrDefault(defaultValue)
    }

    internal fun <T> valueOrDefault(value: ForgeConfigSpec.ConfigValue<T>?, defaultValue: T): T {
        if (value == null) return defaultValue
        return valueOrDefault({ value.get() }, defaultValue)
    }
}
