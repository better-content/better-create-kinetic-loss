package com.bettercontent.bettercreatekineticloss.command

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraftforge.registries.ForgeRegistries

object DemoRigSpawner {
    fun spawn(player: ServerPlayer): SpawnResult {
        val level = player.serverLevel()
        val forward = player.direction
        val origin = player.blockPosition().relative(forward, 3).above()
        val placements = plan(origin, forward)

        val missingBlock = placements.firstOrNull { resolveBlock(it.blockId) == null }
        if (missingBlock != null) {
            return SpawnResult(false, "Missing Create block ${missingBlock.blockId}.")
        }

        val blocked = placements.firstOrNull { !canReplace(level.getBlockState(it.pos)) }
        if (blocked != null) {
            val pos = blocked.pos
            return SpawnResult(false, "Demo rig area is blocked at ${pos.x} ${pos.y} ${pos.z}.")
        }

        placements.forEach { placement ->
            val supportPos = placement.pos.below()
            val supportState = level.getBlockState(supportPos)
            if (canReplace(supportState)) {
                level.setBlock(supportPos, Blocks.ANDESITE.defaultBlockState(), Block.UPDATE_ALL)
            }
        }

        placements.forEach { placement ->
            val block = resolveBlock(placement.blockId) ?: return@forEach
            val state = placement.configure(block.defaultBlockState())
            level.setBlock(placement.pos, state, Block.UPDATE_ALL)
        }

        return SpawnResult(
            success = true,
            message = "Spawned demo rig at ${origin.x} ${origin.y} ${origin.z}. Look at the motor or a shaft and run /transloss debug here."
        )
    }

    private fun plan(origin: BlockPos, forward: Direction): List<BlockPlacement> {
        val axis = forward.axis
        return listOf(
            BlockPlacement(origin, id("creative_motor")) { state -> setDirection(state, forward) },
            BlockPlacement(origin.relative(forward, 1), id("shaft")) { state -> setAxis(state, axis) },
            BlockPlacement(origin.relative(forward, 2), id("cogwheel")) { state -> setAxis(state, axis) },
            BlockPlacement(origin.relative(forward, 3), id("large_cogwheel")) { state -> setAxis(state, axis) },
            BlockPlacement(origin.relative(forward, 4), id("gearbox")) { state -> setAxis(state, axis) },
            BlockPlacement(origin.relative(forward, 5), id("andesite_encased_shaft")) { state -> setAxis(state, axis) },
            BlockPlacement(origin.relative(forward, 6), id("encased_chain_drive")) { state ->
                setBoolean(setAxis(state, axis), "connected_along_first_coordinate", axis != Direction.Axis.X)
            },
            BlockPlacement(origin.relative(forward, 7), id("shaft")) { state -> setAxis(state, axis) }
        )
    }

    private fun resolveBlock(id: ResourceLocation) = ForgeRegistries.BLOCKS.getValue(id)

    private fun canReplace(state: BlockState): Boolean = state.isAir || state.canBeReplaced()

    private fun id(path: String) = ResourceLocation("create", path)

    private fun setDirection(state: BlockState, direction: Direction): BlockState {
        val property = state.properties.firstOrNull { it.name == "facing" } as? DirectionProperty ?: return state
        return state.setValue(property, direction)
    }

    private fun setAxis(state: BlockState, axis: Direction.Axis): BlockState {
        val property = state.properties.firstOrNull { it.name == "axis" } as? EnumProperty<*> ?: return state
        @Suppress("UNCHECKED_CAST")
        return state.setValue(property as EnumProperty<Direction.Axis>, axis)
    }

    private fun setBoolean(state: BlockState, name: String, value: Boolean): BlockState {
        val property = state.properties.firstOrNull { it.name == name } as? BooleanProperty ?: return state
        return state.setValue(property, value)
    }

    private data class BlockPlacement(
        val pos: BlockPos,
        val blockId: ResourceLocation,
        val configure: (BlockState) -> BlockState
    )

    data class SpawnResult(
        val success: Boolean,
        val message: String
    )
}
