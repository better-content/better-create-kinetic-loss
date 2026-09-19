package com.bettercontent.createtransmissionloss.mixin

import com.bettercontent.createtransmissionloss.network.NetworkRuntimeBridge
import com.simibubi.create.content.kinetics.KineticNetwork
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Pseudo
@Mixin(targets = ["com.simibubi.create.content.kinetics.KineticNetwork"], remap = false)
abstract class NetworkDirtyMixin {
    @Inject(method = ["add"], at = [At("TAIL")], require = 0, remap = false)
    private fun onNodeAdded(ci: CallbackInfo) {
        markDirty()
    }

    @Inject(method = ["remove"], at = [At("TAIL")], require = 0, remap = false)
    private fun onNodeRemoved(ci: CallbackInfo) {
        markDirty()
    }

    private fun markDirty() {
        NetworkRuntimeBridge.invalidateTopology(this as KineticNetwork)
    }
}
