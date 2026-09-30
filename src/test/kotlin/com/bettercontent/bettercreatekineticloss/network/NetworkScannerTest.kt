package com.bettercontent.bettercreatekineticloss.network

import com.bettercontent.bettercreatekineticloss.config.TransmissionLossConfig
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkScannerTest {
    @Test
    fun computesExpectedLossWithLinearScaling() {
        val breakdown = TransmissionBreakdown(
            shaftBlocks = 10,
            encasedShaftBlocks = 2,
            cogwheels = 3,
            largeCogwheels = 1,
            gearboxes = 1,
            beltSegments = 8,
            beltPulleys = 2,
            chainDrives = 1,
            rpm = 64f
        )

        val expectedBase =
            10 * TransmissionLossConfig.shaftValue() +
                2 * TransmissionLossConfig.encasedShaftValue() +
                3 * TransmissionLossConfig.cogwheelValue() +
                1 * TransmissionLossConfig.largeCogwheelValue() +
                1 * TransmissionLossConfig.gearboxValue() +
                8 * TransmissionLossConfig.beltSegmentValue() +
                2 * TransmissionLossConfig.beltPulleyValue() +
                1 * TransmissionLossConfig.chainDriveValue()

        val normalized = abs(breakdown.rpm.toDouble()) / 32.0
        val multiplier = (1.0 + 0.5 * normalized).coerceAtMost(3.0)

        assertEquals(expectedBase * multiplier, NetworkScanner.computeLoss(breakdown), 1e-9)
    }

    @Test
    fun cacheRecalcRespectsCooldown() {
        val id = NetworkId("minecraft:overworld", 1L)
        LossCache.bootstrap()
        LossCache.set(id, 100L, TransmissionBreakdown(shaftBlocks = 1, rpm = 16f))
        LossCache.markDirty(id)

        assertEquals(false, LossCache.shouldRecalc(id, 110L, force = false))
        assertEquals(true, LossCache.shouldRecalc(id, 120L, force = false))
        assertEquals(true, LossCache.shouldRecalc(id, 110L, force = true))
    }

    @Test
    fun computesSameTypeLossFromCountAndSpeed() {
        val baseCost = TransmissionLossConfig.shaftValue()
        val normalized = 64.0 / 32.0
        val multiplier = (1.0 + 0.5 * normalized).coerceAtMost(3.0)

        assertEquals(4 * baseCost * multiplier, NetworkScanner.computeTypeLoss(4, baseCost, 64f), 1e-9)
    }

    @Test
    fun computeTypeLossReturnsZeroWhenCountOrCostAreInvalid() {
        assertEquals(0.0, NetworkScanner.computeTypeLoss(0, TransmissionLossConfig.shaftValue(), 32f))
        assertEquals(0.0, NetworkScanner.computeTypeLoss(-1, TransmissionLossConfig.shaftValue(), 32f))
        assertEquals(0.0, NetworkScanner.computeTypeLoss(1, 0.0, 32f))
        assertEquals(0.0, NetworkScanner.computeTypeLoss(1, -1.0, 32f))
    }

    @Test
    fun computeTypeLossUsesAbsoluteSpeedAndCapsMultiplier() {
        val baseCost = TransmissionLossConfig.gearboxValue()

        assertEquals(2 * baseCost * 2.0, NetworkScanner.computeTypeLoss(2, baseCost, -64f), 1e-9)
        assertEquals(2 * baseCost * 3.0, NetworkScanner.computeTypeLoss(2, baseCost, 256f), 1e-9)
    }

    @Test
    fun computesLossFromBreakdownAcrossAllBlockTypes() {
        val breakdown = TransmissionBreakdown(
            shaftBlocks = 1,
            encasedShaftBlocks = 2,
            cogwheels = 1,
            largeCogwheels = 1,
            gearboxes = 1,
            beltSegments = 1,
            beltPulleys = 1,
            chainDrives = 1,
            rpm = 48f
        )

        val expected =
            TransmissionLossConfig.shaftValue() +
                2 * TransmissionLossConfig.encasedShaftValue() +
                TransmissionLossConfig.cogwheelValue() +
                TransmissionLossConfig.largeCogwheelValue() +
                TransmissionLossConfig.gearboxValue() +
                TransmissionLossConfig.beltSegmentValue() +
                TransmissionLossConfig.beltPulleyValue() +
                TransmissionLossConfig.chainDriveValue()

        val normalized = abs(48f.toDouble()) / 32.0
        val multiplier = (1.0 + 0.5 * normalized).coerceAtMost(3.0)

        assertEquals(expected * multiplier, NetworkScanner.computeLoss(breakdown), 1e-9)
    }

    @Test
    fun computesEachComponentAtItsOwnSpeed() {
        val slowShaft = TransmissionComponent(TransmissionComponentKind.SHAFT, 16f)
        val fastGearbox = TransmissionComponent(TransmissionComponentKind.GEARBOX, 128f)
        val breakdown = TransmissionBreakdown(
            shaftBlocks = 1,
            gearboxes = 1,
            rpm = 128f,
            components = listOf(slowShaft, fastGearbox)
        )

        val expected = NetworkScanner.computeTypeLoss(1, TransmissionLossConfig.shaftValue(), 16f) +
            NetworkScanner.computeTypeLoss(1, TransmissionLossConfig.gearboxValue(), 128f)
        val repricedAtFastBranch = NetworkScanner.computeTypeLoss(
            1,
            TransmissionLossConfig.shaftValue(),
            128f
        ) + NetworkScanner.computeTypeLoss(1, TransmissionLossConfig.gearboxValue(), 128f)

        assertEquals(expected, NetworkScanner.computeLoss(breakdown), 1e-9)
        assertEquals(expected, NetworkScanner.computeComponentLoss(breakdown, TransmissionComponentKind.SHAFT) +
            NetworkScanner.computeComponentLoss(breakdown, TransmissionComponentKind.GEARBOX), 1e-9)
        assert(expected < repricedAtFastBranch)
    }
}
