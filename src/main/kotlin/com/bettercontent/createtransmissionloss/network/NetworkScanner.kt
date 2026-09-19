package com.bettercontent.createtransmissionloss.network

import com.bettercontent.createtransmissionloss.config.TransmissionLossConfig

/**
 * Belts are counted as pulley-pair Manhattan span on purpose for stability across Create internals.
 */
object NetworkScanner {
    fun computeLoss(breakdown: TransmissionBreakdown): Double {
        if (breakdown.components.isNotEmpty()) {
            return breakdown.components.sumOf { component ->
                computeTypeLoss(1, component.kind.baseCost(), component.rpm)
            }
        }
        val base = breakdown.shaftBlocks * TransmissionLossConfig.shaftValue() +
            breakdown.encasedShaftBlocks * TransmissionLossConfig.encasedShaftValue() +
            breakdown.cogwheels * TransmissionLossConfig.cogwheelValue() +
            breakdown.largeCogwheels * TransmissionLossConfig.largeCogwheelValue() +
            breakdown.gearboxes * TransmissionLossConfig.gearboxValue() +
            breakdown.beltSegments * TransmissionLossConfig.beltSegmentValue() +
            breakdown.beltPulleys * TransmissionLossConfig.beltPulleyValue() +
            breakdown.chainDrives * TransmissionLossConfig.chainDriveValue()
        return base * TransmissionLossConfig.speedMultiplier(breakdown.rpm)
    }

    fun computeTypeLoss(count: Int, baseCost: Double, rpm: Float): Double {
        if (count <= 0 || baseCost <= 0.0) return 0.0
        return count * baseCost * TransmissionLossConfig.speedMultiplier(rpm)
    }

    fun computeComponentLoss(breakdown: TransmissionBreakdown, kind: TransmissionComponentKind): Double =
        breakdown.components.filter { it.kind == kind }
            .sumOf { component -> computeTypeLoss(1, kind.baseCost(), component.rpm) }
}

enum class TransmissionComponentKind {
    SHAFT,
    ENCASED_SHAFT,
    COGWHEEL,
    LARGE_COGWHEEL,
    GEARBOX,
    BELT,
    CHAIN_DRIVE;

    fun baseCost(): Double = when (this) {
        SHAFT -> TransmissionLossConfig.shaftValue()
        ENCASED_SHAFT -> TransmissionLossConfig.encasedShaftValue()
        COGWHEEL -> TransmissionLossConfig.cogwheelValue()
        LARGE_COGWHEEL -> TransmissionLossConfig.largeCogwheelValue()
        GEARBOX -> TransmissionLossConfig.gearboxValue()
        BELT -> TransmissionLossConfig.beltSegmentValue()
        CHAIN_DRIVE -> TransmissionLossConfig.chainDriveValue()
    }
}

data class TransmissionComponent(val kind: TransmissionComponentKind, val rpm: Float)

data class TransmissionBreakdown(
    val shaftBlocks: Int = 0,
    val encasedShaftBlocks: Int = 0,
    val cogwheels: Int = 0,
    val largeCogwheels: Int = 0,
    val gearboxes: Int = 0,
    val beltSegments: Int = 0,
    val beltPulleys: Int = 0,
    val chainDrives: Int = 0,
    val rpm: Float = 0f,
    val components: List<TransmissionComponent> = emptyList()
)
