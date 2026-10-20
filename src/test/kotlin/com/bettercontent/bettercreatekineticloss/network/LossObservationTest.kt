package com.bettercontent.bettercreatekineticloss.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LossObservationTest {
    @Test
    fun scanReasonsDescribeObservedRecalculationWithoutChangingInputs() {
        val current = CachedLoss(dirty = false, immediateRefresh = false)
        assertEquals("forced", LossObservation.scanReason(null, current, true))
        assertEquals("first_scan", LossObservation.scanReason(null, current, false))
        val changed = current.copy(configFingerprint = current.configFingerprint.copy(enabled = !current.configFingerprint.enabled))
        assertEquals("config_changed", LossObservation.scanReason(changed, current, false))
        assertEquals("invalidated", LossObservation.scanReason(current.copy(immediateRefresh = true), current, false))
        assertEquals("dirty_cooldown_elapsed", LossObservation.scanReason(current.copy(dirty = true), current, false))
        assertEquals("binding_rebuilt", LossObservation.scanReason(current, current, false))
        assertEquals(false, current.dirty)
        assertEquals(false, current.immediateRefresh)
    }

    @Test
    fun overloadRequiresObservedCommittedExcessCausedByLoss() {
        assertEquals(true, LossObservation.isLossOverloaded(90f, 20f, 100f, 110f))
        assertEquals(true, LossObservation.isLossOverloaded(100f, 1f, 100f, 101f))
        assertEquals(false, LossObservation.isLossOverloaded(Float.NaN, 20f, 100f, 110f))
        assertEquals(false, LossObservation.isLossOverloaded(Float.POSITIVE_INFINITY, 20f, 100f, 110f))
        assertEquals(false, LossObservation.isLossOverloaded(90f, 0f, 100f, 110f))
        assertEquals(false, LossObservation.isLossOverloaded(0f, 20f, 0f, 20f))
        assertEquals(false, LossObservation.isLossOverloaded(101f, 20f, 100f, 121f))
        assertEquals(false, LossObservation.isLossOverloaded(90f, 10f, 100f, 100f))
    }

    @Test
    fun onlyCommittedEdgesProduceOverloadObservations() {
        assertNull(LossObservation.overloadTransition(false, false))
        assertNull(LossObservation.overloadTransition(true, true))
        assertEquals("loss_overloaded", LossObservation.overloadTransition(false, true))
        assertEquals("loss_overload_cleared", LossObservation.overloadTransition(true, false))
    }
}
