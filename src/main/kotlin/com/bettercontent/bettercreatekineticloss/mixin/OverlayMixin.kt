package com.bettercontent.bettercreatekineticloss.mixin

import com.simibubi.create.content.kinetics.base.KineticBlockEntity
import com.simibubi.create.foundation.utility.CreateLang
import com.bettercontent.bettercreatekineticloss.network.NetworkRuntimeBridge
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Pseudo
@Mixin(value = [KineticBlockEntity::class], remap = false)
abstract class OverlayMixin {
    @Shadow(remap = false)
    abstract fun getTheoreticalSpeed(): Float

    @Suppress("UNUSED_PARAMETER", "USELESS_CAST")
    @Inject(method = ["addToGoggleTooltip"], at = [At("RETURN")], cancellable = true, require = 0, remap = false)
    private fun addTransmissionLossToGoggles(
        tooltip: MutableList<Component>,
        _isPlayerSneaking: Boolean,
        cir: CallbackInfoReturnable<Boolean>
    ) {
        val blockEntity = this as Any as KineticBlockEntity
        val level = blockEntity.level ?: return
        val state = level.getBlockState(blockEntity.blockPos)
        val summary = NetworkRuntimeBridge.summarizeBlockLoss(blockEntity, state, getTheoreticalSpeed()) ?: return

        if (!cir.returnValue) {
            CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip)
        }

        CreateLang.translate("tooltip.stressImpact")
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip)

        CreateLang.number(summary.individualLoss)
            .translate("generic.unit.stress")
            .style(ChatFormatting.AQUA)
            .space()
            .text(ChatFormatting.DARK_GRAY, "from transmission loss")
            .forGoggles(tooltip, 1)

        CreateLang.number(summary.networkTypeLoss)
            .translate("generic.unit.stress")
            .style(ChatFormatting.AQUA)
            .space()
            .text(
                ChatFormatting.DARK_GRAY,
                "across ${summary.networkTypeCount} ${summary.networkTypeLabel} on network"
            )
            .forGoggles(tooltip, 1)

        cir.returnValue = true
    }
}
