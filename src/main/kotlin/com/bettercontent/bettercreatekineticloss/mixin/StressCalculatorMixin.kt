package com.bettercontent.bettercreatekineticloss.mixin

import com.bettercontent.bettercreatekineticloss.network.NetworkRuntimeBridge
import com.simibubi.create.content.kinetics.KineticNetwork
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Pseudo
@Mixin(targets = ["com.simibubi.create.content.kinetics.KineticNetwork"], remap = false)
abstract class StressCalculatorMixin {
    @Shadow private var currentCapacity: Float = 0f
    @Shadow private var currentStress: Float = 0f
    @Unique private var discoveryBaseStress: Float = Float.NaN
    @Unique private var discoveryLoss: Float = 0f
    @Unique private var discoveryOverloaded: Boolean = false
    @Inject(method = ["calculateStress"], at = [At("RETURN")], cancellable = true, require = 0, remap = false)
    private fun appendNetworkLoss(cir: CallbackInfoReturnable<Float>) {
        val cached = NetworkRuntimeBridge.refreshLoss(this as KineticNetwork) ?: return
        discoveryBaseStress = cir.returnValue
        discoveryLoss = cached.lossSu.toFloat()
        cir.returnValue = cir.returnValue + discoveryLoss
    }
    @Inject(method = ["sync"], at = [At("TAIL")], remap = false)
    private fun signalCommittedOverload(ci: CallbackInfo) {
        val causedByLoss = discoveryBaseStress.isFinite() && discoveryLoss > 0f && currentCapacity > 0f &&
            discoveryBaseStress <= currentCapacity && currentStress > currentCapacity
        if (causedByLoss && !discoveryOverloaded) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                com.bettercontent.bettercreatekineticloss.api.TransmissionOverloadEvent(
                    this as KineticNetwork, discoveryBaseStress, discoveryLoss, currentCapacity))
        }
        discoveryOverloaded = causedByLoss
    }
}
