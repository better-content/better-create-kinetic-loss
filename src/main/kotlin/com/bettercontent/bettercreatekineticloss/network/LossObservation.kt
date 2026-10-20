package com.bettercontent.bettercreatekineticloss.network

/** Classifies an already-completed scan; never reads native members or changes cache policy. */
internal object LossObservation {
    fun scanReason(previous: CachedLoss?, current: CachedLoss, forced: Boolean): String = when {
        forced -> "forced"
        previous == null -> "first_scan"
        previous.configFingerprint != current.configFingerprint -> "config_changed"
        previous.immediateRefresh -> "invalidated"
        previous.dirty -> "dirty_cooldown_elapsed"
        else -> "binding_rebuilt"
    }

    fun isLossOverloaded(baseStress: Float, loss: Float, capacity: Float, committedStress: Float): Boolean =
        baseStress.isFinite() && loss > 0f && capacity > 0f &&
            baseStress <= capacity && committedStress > capacity

    /** Called at Create's committed sync boundary, not at calculateStress admission. */
    fun overloadTransition(previous: Boolean, current: Boolean): String? = when {
        previous == current -> null
        current -> "loss_overloaded"
        else -> "loss_overload_cleared"
    }
}
