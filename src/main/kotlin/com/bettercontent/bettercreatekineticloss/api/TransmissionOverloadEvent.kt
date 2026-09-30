package com.bettercontent.bettercreatekineticloss.api
import com.simibubi.create.content.kinetics.KineticNetwork
import net.minecraftforge.eventbus.api.Event
/** Posted after the network synchronizes a committed overload caused specifically by added transmission loss. */
class TransmissionOverloadEvent(val network: KineticNetwork, val baseStress: Float, val loss: Float, val capacity: Float) : Event()
