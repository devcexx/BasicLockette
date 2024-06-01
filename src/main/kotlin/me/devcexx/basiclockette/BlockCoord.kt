package me.devcexx.basiclockette

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

data class BlockCoord(val blockX: Int, val blockY: Int, val blockZ: Int) {
    fun advance(
        facing: BlockFace,
        times: Int = 1,
    ): BlockCoord =
        BlockCoord(
            blockX + facing.modX * times,
            blockY + facing.modY * times,
            blockZ + facing.modZ * times,
        )
}

val Location.blockCoord get() = BlockCoord(blockX, blockY, blockZ)
val Block.coord get() = BlockCoord(x, y, z)

fun World.getBlockAt(coord: BlockCoord): Block = getBlockAt(coord.blockX, coord.blockY, coord.blockZ)
