package com.bettercontent.createtransmissionloss.network

import com.bettercontent.createtransmissionloss.config.TransmissionLossConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object LossCache {
    private val cache = ConcurrentHashMap<NetworkId, CachedLoss>()
    private val scans = AtomicLong()
    private val cacheHits = AtomicLong()

    fun bootstrap() {
        cache.clear()
        scans.set(0)
        cacheHits.set(0)
    }

    fun markDirty(networkId: NetworkId) {
        cache.compute(networkId) { _, current ->
            (current ?: CachedLoss()).copy(dirty = true)
        }
    }

    /** Topology and speed changes must take effect on the next network calculation. */
    fun invalidate(networkId: NetworkId) {
        cache.compute(networkId) { _, current ->
            (current ?: CachedLoss()).copy(dirty = true, immediateRefresh = true)
        }
    }

    fun set(networkId: NetworkId, gameTime: Long, breakdown: TransmissionBreakdown) {
        cache[networkId] = CachedLoss(
            lossSu = NetworkScanner.computeLoss(breakdown),
            breakdown = breakdown,
            lastRecalcGameTime = gameTime,
            dirty = false,
            immediateRefresh = false,
            configFingerprint = configFingerprint()
        )
    }

    fun getLoss(networkId: NetworkId): Double = cache[networkId]?.lossSu ?: 0.0

    fun snapshot(networkId: NetworkId): CachedLoss? = cache[networkId]

    fun shouldRecalc(networkId: NetworkId, gameTime: Long, force: Boolean): Boolean {
        if (force) return true
        val entry = cache[networkId] ?: return true
        if (entry.configFingerprint != configFingerprint()) return true
        if (!entry.dirty) return false
        if (entry.immediateRefresh) return true
        return gameTime - entry.lastRecalcGameTime >= TransmissionLossConfig.recalcCooldownTicksValue()
    }

    fun usableSnapshot(networkId: NetworkId, gameTime: Long, force: Boolean = false): CachedLoss? {
        val cached = cache[networkId] ?: return null
        if (shouldRecalc(networkId, gameTime, force)) return null
        cacheHits.incrementAndGet()
        return cached
    }

    fun recordScan() { scans.incrementAndGet() }
    fun scanCount(): Long = scans.get()
    fun cacheHitCount(): Long = cacheHits.get()

    fun size(): Int = cache.size
}

data class CachedLoss(
    val lossSu: Double = 0.0,
    val breakdown: TransmissionBreakdown = TransmissionBreakdown(),
    val lastRecalcGameTime: Long = 0L,
    val dirty: Boolean = true,
    val immediateRefresh: Boolean = false,
    val configFingerprint: ConfigFingerprint = configFingerprint()
)

data class ConfigFingerprint(
    val enabled: Boolean,
    val includeEncased: Boolean,
    val speedMode: TransmissionLossConfig.SpeedMode,
    val baseRpm: Double,
    val k: Double,
    val maxMult: Double,
    val shaft: Double,
    val encasedShaft: Double,
    val cogwheel: Double,
    val largeCogwheel: Double,
    val gearbox: Double,
    val beltSegment: Double,
    val beltPulley: Double,
    val chainDrive: Double
)

private fun configFingerprint() = ConfigFingerprint(
    TransmissionLossConfig.valueOrDefault(TransmissionLossConfig.enabled, true),
    TransmissionLossConfig.includeEncasedShaftsValue(),
    TransmissionLossConfig.valueOrDefault(TransmissionLossConfig.speedMode, TransmissionLossConfig.SpeedMode.LINEAR),
    TransmissionLossConfig.valueOrDefault(TransmissionLossConfig.baseRpm, 32.0),
    TransmissionLossConfig.valueOrDefault(TransmissionLossConfig.k, 0.5),
    TransmissionLossConfig.valueOrDefault(TransmissionLossConfig.maxMult, 3.0),
    TransmissionLossConfig.shaftValue(), TransmissionLossConfig.encasedShaftValue(), TransmissionLossConfig.cogwheelValue(),
    TransmissionLossConfig.largeCogwheelValue(), TransmissionLossConfig.gearboxValue(), TransmissionLossConfig.beltSegmentValue(),
    TransmissionLossConfig.beltPulleyValue(), TransmissionLossConfig.chainDriveValue()
)

data class NetworkId(
    val dimension: String,
    val canonicalPosLong: Long
)
