package com.bettercontent.bettercreatekineticloss.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NetworkRuntimeBridgeTest {
    @Test
    fun selectsLowestPositionAndItsNetworkDimension() {
        val id = NetworkRuntimeBridge.selectNetworkId(listOf(
            NetworkRuntimeBridge.NetworkMemberIdentity("minecraft:the_nether", 20L),
            NetworkRuntimeBridge.NetworkMemberIdentity("minecraft:the_nether", 7L)
        ))

        assertEquals(NetworkId("minecraft:the_nether", 7L), id)
    }

    @Test
    fun returnsNullWhenThereAreNoMembers() {
        assertNull(NetworkRuntimeBridge.selectNetworkId(emptyList()))
    }

    @Test
    fun returnsDefaultDimensionWhenNoMemberHasALevel() {
        val id = NetworkRuntimeBridge.selectNetworkId(listOf(
            NetworkRuntimeBridge.NetworkMemberIdentity(null, 9L)
        ))

        assertEquals(NetworkId("minecraft:overworld", 9L), id)
    }
}
