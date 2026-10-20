package com.bettercontent.bettercreatekineticloss.gametest

import com.bettercontent.bettercreatekineticloss.TransmissionLossMod
import com.bettercontent.bettercreatekineticloss.config.TransmissionLossConfig
import com.bettercontent.bettercreatekineticloss.network.NetworkScanner
import com.bettercontent.bettercreatekineticloss.network.TransmissionBreakdown
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraftforge.gametest.GameTestHolder

@GameTestHolder(TransmissionLossMod.MOD_ID)
object TransmissionLossGameTests {
    /** Native calculateStress call: detects a silently unapplied require=0 injection.
     * This isolates the hook, not normal Create topology propagation or an assembled rig. */
    @JvmStatic
    @GameTest(template = "empty")
    fun nativeStressHookPublishesAndReusesLoss(helper: GameTestHelper) {
        val pos = net.minecraft.core.BlockPos(1, 1, 1)
        helper.setBlock(pos, com.simibubi.create.AllBlocks.SHAFT.defaultState)
        val shaft = helper.getBlockEntity(pos) as com.simibubi.create.content.kinetics.base.KineticBlockEntity
        val network = com.simibubi.create.content.kinetics.KineticNetwork()
        network.members[shaft] = 0f
        val before = com.bettercontent.bettercreatekineticloss.network.LossCache.scanCount()
        val expected = com.bettercontent.bettercreatekineticloss.network.NetworkRuntimeBridge.computeBlockLoss(shaft.blockState, shaft.speed)
        helper.assertTrue(expected > 0.0, "Fixture requires enabled positive shaft loss")
        val actual = network.calculateStress()
        helper.assertTrue(kotlin.math.abs(actual - expected) < 1e-5, "Native calculateStress must append published loss")
        helper.assertTrue(com.bettercontent.bettercreatekineticloss.network.LossCache.scanCount() == before + 1, "Hook must publish one completed scan")
        helper.assertTrue(network.calculateStress() == actual, "Stable native calculation must preserve loss")
        helper.assertTrue(com.bettercontent.bettercreatekineticloss.network.LossCache.scanCount() == before + 1, "Stable calculation must reuse cache")
        com.bettercontent.bettercreatekineticloss.network.NetworkRuntimeBridge.invalidateTopology(network)
        helper.assertTrue(network.calculateStress() == actual, "Invalidated hook must recalculate the same loss")
        helper.assertTrue(com.bettercontent.bettercreatekineticloss.network.LossCache.scanCount() == before + 2, "Invalidated calculation must complete another scan")
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun linearSpeedScalingApplies(helper: GameTestHelper) {
        val breakdown = TransmissionBreakdown(shaftBlocks = 1, rpm = 32f)
        val expected = TransmissionLossConfig.shaftValue() * TransmissionLossConfig.speedMultiplier(32f)
        val actual = NetworkScanner.computeLoss(breakdown)
        if (kotlin.math.abs(expected - actual) > 1e-9) {
            helper.fail("Expected $expected but got $actual")
            return
        }
        helper.succeed()
    }
}
