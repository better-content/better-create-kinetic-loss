package com.bettercontent.createtransmissionloss.network

import com.bettercontent.createtransmissionloss.config.TransmissionLossConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LossCacheTest {
    @Test
    fun bootstrapClearsCachedNetworks() {
        LossCache.bootstrap()
        val id = NetworkId("minecraft:overworld", 7L)
        LossCache.set(id, 5L, TransmissionBreakdown(shaftBlocks = 1, rpm = 16f))

        assertEquals(1, LossCache.size())

        LossCache.bootstrap()

        assertEquals(0, LossCache.size())
        assertEquals(0.0, LossCache.getLoss(id))
        assertNull(LossCache.snapshot(id))
    }

    @Test
    fun setStoresLossAndMetadata() {
        LossCache.bootstrap()
        val id = NetworkId("minecraft:the_nether", 11L)
        val breakdown = TransmissionBreakdown(shaftBlocks = 2, cogwheels = 1, rpm = 16f)

        LossCache.set(id, 123L, breakdown)

        val cached = LossCache.snapshot(id)
        requireNotNull(cached)
        assertEquals(NetworkScanner.computeLoss(breakdown), cached.lossSu, 1e-9)
        assertEquals(123L, cached.lastRecalcGameTime)
        assertEquals(breakdown, cached.breakdown)
        assertEquals(false, cached.dirty)
    }

    @Test
    fun markDirtyKeepsExistingCachedValuesAndDefaultsWhenMissing() {
        LossCache.bootstrap()
        val id = NetworkId("minecraft:overworld", 3L)
        val breakdown = TransmissionBreakdown(shaftBlocks = 1, rpm = 32f)
        LossCache.set(id, 10L, breakdown)

        LossCache.markDirty(id)
        val dirty = requireNotNull(LossCache.snapshot(id))
        assertEquals(true, dirty.dirty)
        assertEquals(10L, dirty.lastRecalcGameTime)
        assertEquals(breakdown, dirty.breakdown)

        LossCache.bootstrap()
        val emptyId = NetworkId("minecraft:overworld", 4L)
        LossCache.markDirty(emptyId)
        val initialized = requireNotNull(LossCache.snapshot(emptyId))
        assertEquals(0.0, initialized.lossSu)
        assertEquals(0L, initialized.lastRecalcGameTime)
        assertEquals(true, initialized.dirty)
    }

    @Test
    fun shouldRecalcHandlesForceDirtyAndCooldownPaths() {
        LossCache.bootstrap()
        val id = NetworkId("minecraft:overworld", 42L)
        val breakdown = TransmissionBreakdown(shaftBlocks = 1, rpm = 32f)
        LossCache.set(id, 100L, breakdown)

        assertEquals(false, LossCache.shouldRecalc(id, 100L, force = false))

        LossCache.markDirty(id)
        assertEquals(false, LossCache.shouldRecalc(id, 105L, force = false))
        assertEquals(false, LossCache.shouldRecalc(id, 110L, force = false))
        assertEquals(true, LossCache.shouldRecalc(id, 120L, force = false))
        assertEquals(true, LossCache.shouldRecalc(id, 120L, force = true))
    }

    @Test
    fun shouldRecalcReturnsTrueWhenNetworkIsNotCached() {
        LossCache.bootstrap()
        assertEquals(true, LossCache.shouldRecalc(NetworkId("minecraft:the_end", 1L), 0L, force = false))
    }

    @Test
    fun validSnapshotIsAHitWithoutRecordingAnotherScan() {
        LossCache.bootstrap()
        val id = NetworkId("minecraft:overworld", 84L)
        LossCache.set(id, 100L, TransmissionBreakdown(shaftBlocks = 1, rpm = 16f))
        LossCache.recordScan()

        val cached = LossCache.usableSnapshot(id, 100L)

        assertEquals(1L, LossCache.scanCount())
        assertEquals(1L, LossCache.cacheHitCount())
        assertEquals(100L, requireNotNull(cached).lastRecalcGameTime)
    }

    @Test
    fun topologyOrSpeedInvalidationBypassesCooldown() {
        LossCache.bootstrap()
        val id = NetworkId("minecraft:overworld", 85L)
        LossCache.set(id, 100L, TransmissionBreakdown(shaftBlocks = 1, rpm = 16f))

        LossCache.invalidate(id)

        assertEquals(true, LossCache.shouldRecalc(id, 101L, force = false))
        assertNull(LossCache.usableSnapshot(id, 101L))
    }
}
