package com.bettercontent.bettercreatekineticloss

import com.mojang.brigadier.CommandDispatcher
import com.bettercontent.bettercreatekineticloss.command.TransmissionLossCommands
import com.bettercontent.bettercreatekineticloss.config.TransmissionLossConfig
import com.bettercontent.bettercreatekineticloss.network.LossCache
import com.bettercontent.bettercreatekineticloss.network.NetworkRuntimeBridge
import net.minecraft.commands.CommandSourceStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.server.ServerStoppedEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import com.bettercontent.bettercreatekineticloss.diagnostics.PlaytestLog
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod(TransmissionLossMod.MOD_ID)
class TransmissionLossMod {
    init {
        TransmissionLossConfig.register()
        LossCache.bootstrap()
        NetworkRuntimeBridge.bootstrap()
        MinecraftForge.EVENT_BUS.register(this)
        FMLJavaModLoadingContext.get().modEventBus.addListener<FMLCommonSetupEvent> {
            val ownFile = net.minecraftforge.fml.ModList.get().getModFileById(MOD_ID)
            val source = if (ownFile == null) "UNKNOWN" else PLAYTEST.sourceIdentity(
                ownFile.file.findResource("META-INF", "better-content-source.properties"))
            PLAYTEST.status("common", net.minecraftforge.fml.ModList.get().getModContainerById(MOD_ID)
                .map { container -> container.modInfo.version.toString() }.orElse("UNKNOWN"), source) {
                PlaytestLog.fields("enabled", TransmissionLossConfig.valueOrDefault(TransmissionLossConfig.enabled, true),
                    "integration", "create_present", "hook_execution", "UNKNOWN")
            }
        }
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        register(event.dispatcher)
    }

    @SubscribeEvent
    fun onServerStopped(event: ServerStoppedEvent) {
        // Release native level bindings between integrated-server lifetimes.
        NetworkRuntimeBridge.bootstrap()
        LossCache.bootstrap()
        PLAYTEST.reset("server")
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END && PLAYTEST.enabled("summary")) PLAYTEST.summary("server")
    }

    private fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        TransmissionLossCommands.register(dispatcher)
    }

    companion object {
        const val MOD_ID = "better_create_kinetic_loss"
        @JvmField val PLAYTEST = PlaytestLog(MOD_ID)
    }
}
