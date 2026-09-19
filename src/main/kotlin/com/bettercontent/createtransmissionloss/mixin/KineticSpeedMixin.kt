package com.bettercontent.createtransmissionloss.mixin

import com.bettercontent.createtransmissionloss.network.NetworkRuntimeBridge
import com.simibubi.create.content.kinetics.base.KineticBlockEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Pseudo
@Mixin(value = [KineticBlockEntity::class], remap = false)
abstract class KineticSpeedMixin {
    @Inject(method = ["onSpeedChanged"], at = [At("TAIL")], require = 0, remap = false)
    private fun invalidateTransmissionLossSpeed(previousSpeed: Float, ci: CallbackInfo) {
        NetworkRuntimeBridge.invalidateSpeed(this as KineticBlockEntity)
    }
}
