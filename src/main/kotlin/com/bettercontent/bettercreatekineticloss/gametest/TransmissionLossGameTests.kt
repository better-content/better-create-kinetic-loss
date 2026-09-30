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
