package me.devcexx.basiclockette

import org.bukkit.block.Sign
import org.bukkit.entity.HumanEntity

data class ClaimedChest(val owner: PlayerProfile, val location: ChestLocation, val claimingSign: Sign) {
    val protectedCoords: List<BlockCoord> by lazy {
        location.chestCoords + claimingSign.block.coord
    }
}

fun ClaimedChest.isAccessAllowed(player: HumanEntity): Boolean = player.uniqueId == owner.id
