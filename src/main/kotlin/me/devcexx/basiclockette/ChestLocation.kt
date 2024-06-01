package me.devcexx.basiclockette

import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest

sealed class ChestLocation {
    abstract val chestEntity: Chest
    abstract val chestCoords: List<BlockCoord>
    abstract val world: World
    abstract val facing: BlockFace
    abstract val tileBlockCoord: BlockCoord

    val claimingSignCoords: List<BlockCoord> by lazy {
        chestCoords.map { it.advance(facing) }
    }

    data class Single(
        override val world: World,
        override val chestEntity: Chest,
        override val facing: BlockFace,
        override val tileBlockCoord: BlockCoord,
    ) : ChestLocation() {
        override val chestCoords: List<BlockCoord> = listOf(tileBlockCoord)
    }

    data class Double(
        override val world: World,
        override val chestEntity: Chest,
        override val facing: BlockFace,
        override val tileBlockCoord: BlockCoord,
        val secondaryBlockCoord: BlockCoord,
    ) : ChestLocation() {
        override val chestCoords: List<BlockCoord> = listOf(tileBlockCoord, secondaryBlockCoord)
    }
}
