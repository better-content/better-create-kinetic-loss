package com.bettercontent.createtransmissionloss

import com.mojang.brigadier.CommandDispatcher
import com.bettercontent.createtransmissionloss.command.TransmissionLossCommands
import com.bettercontent.createtransmissionloss.config.TransmissionLossConfig
import com.bettercontent.createtransmissionloss.network.LossCache
import com.bettercontent.createtransmissionloss.network.NetworkRuntimeBridge
import net.minecraft.commands.CommandSourceStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod(TransmissionLossMod.MOD_ID)
class TransmissionLossMod {
    init {
        TransmissionLossConfig.register()
        LossCache.bootstrap()
        NetworkRuntimeBridge.bootstrap()
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        register(event.dispatcher)
    }

    private fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        TransmissionLossCommands.register(dispatcher)
    }

    companion object {
        const val MOD_ID = "create_transmission_loss"
    }
}
