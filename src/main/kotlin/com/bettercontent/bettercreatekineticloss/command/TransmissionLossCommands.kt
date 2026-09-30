package com.bettercontent.bettercreatekineticloss.command

import com.mojang.brigadier.CommandDispatcher
import com.bettercontent.bettercreatekineticloss.network.CachedLoss
import com.bettercontent.bettercreatekineticloss.network.LossCache
import com.bettercontent.bettercreatekineticloss.network.NetworkRuntimeBridge
import com.simibubi.create.content.kinetics.base.KineticBlockEntity
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.BlockHitResult
import java.util.Locale

object TransmissionLossCommands {
    private const val DEMO_NAME = "transloss"

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("transloss")
                .then(Commands.literal("debug").then(Commands.literal("here").executes { context ->
                    showNetworkDebug(context.source, force = false)
                }))
                .then(Commands.literal("recalc").then(Commands.literal("here").executes { context ->
                    showNetworkDebug(context.source, force = true)
                }))
                .then(Commands.literal("profile").executes {
                    it.source.sendSuccess({ Component.literal("Profiling hook not yet implemented; cache size=${LossCache.size()}") }, false)
                    1
                })
        )
        dispatcher.register(
            Commands.literal("demo")
                .requires { it.hasPermission(2) }
                .then(Commands.literal(DEMO_NAME).executes { context ->
                    spawnDemoRig(context.source)
                })
        )
    }

    private fun showNetworkDebug(source: CommandSourceStack, force: Boolean): Int {
        val player = source.playerOrException
        val cached = readLookedAtLoss(player, force)
        if (cached == null) {
            source.sendFailure(Component.literal("Look at a Create kinetic block in the target network and try again."))
            return 0
        }

        source.sendSuccess({ Component.literal(formatSnapshot(cached)) }, false)
        return 1
    }

    private fun spawnDemoRig(source: CommandSourceStack): Int {
        val player = source.playerOrException
        val result = DemoRigSpawner.spawn(player)
        if (!result.success) {
            source.sendFailure(Component.literal(result.message))
            return 0
        }

        source.sendSuccess({ Component.literal(result.message) }, true)
        return 1
    }

    private fun readLookedAtLoss(player: ServerPlayer, force: Boolean): CachedLoss? {
        val hit = player.pick(20.0, 0f, false)
        if (hit !is BlockHitResult) {
            return null
        }

        val blockEntity = player.serverLevel().getBlockEntity(hit.blockPos) as? KineticBlockEntity ?: return null
        return NetworkRuntimeBridge.refreshLossFromBlockEntity(blockEntity, force)
    }

    private fun formatSnapshot(cached: CachedLoss): String {
        val breakdown = cached.breakdown
        return String.format(
            Locale.ROOT,
            "Transmission loss %.2f SU @ %.1f rpm | shafts=%d encased=%d cogs=%d large=%d gearboxes=%d belts=%d pulleys=%d chains=%d",
            cached.lossSu,
            breakdown.rpm,
            breakdown.shaftBlocks,
            breakdown.encasedShaftBlocks,
            breakdown.cogwheels,
            breakdown.largeCogwheels,
            breakdown.gearboxes,
            breakdown.beltSegments,
            breakdown.beltPulleys,
            breakdown.chainDrives
        )
    }
}
