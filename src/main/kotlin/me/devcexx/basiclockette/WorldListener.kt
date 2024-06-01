package me.devcexx.basiclockette

import me.devcexx.basiclockette.i18n.PluginMessages
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.block.data.type.Chest
import org.bukkit.block.sign.Side
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerInteractEvent

class WorldListener(
    private val plugin: BasicLockette,
    private val locketteService: LocketteService,
) : Listener {
    companion object : Logging by logFor<WorldListener>()

    private fun handleAccessAttemptEvent(
        location: ChestLocation,
        actor: HumanEntity,
    ): Boolean =
        locketteService.getClaimedChestForChest(location)?.let { claimedChest ->
            locketteService.refreshClaimSignMetadataAndText(claimedChest)
            logger.fine { "Interaction of actor $actor with claimed chest $claimedChest" }

            if (locketteService.isAccessAllowedToClaimedChest(claimedChest, actor)) {
                logger.fine("Access was allowed for actor $actor to chest $claimedChest")
                true
            } else {
                if (actor is Player) {
                    plugin.sendActionBarMessage(actor, PluginMessages::unauthorizedToOpenChest)
                }
                logger.fine("Access was denied for actor $actor to chest $claimedChest")
                false
            }
        } ?: run {
            logger.fine { "Chest $location is not claimed" }
            true
        }

    private fun removeProtectedBlocksFromExplosion(affectedBlocks: MutableList<Block>) {
        affectedBlocks.removeAll { block ->
            val claimedChest = locketteService.getClaimedChestForBlockAt(block.world, block.coord)
            claimedChest != null && claimedChest.protectedCoords.contains(block.coord)
        }
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && !(e.hasItem() && e.player.isSneaking)) {
            e.clickedBlock?.let { block ->
                locketteService.getChestLocationForChestBlockAt(block.world, block.coord)
            }?.let {
                if (!handleAccessAttemptEvent(it, e.player)) {
                    e.setUseInteractedBlock(Event.Result.DENY)
                }
            }
        }
    }

    @EventHandler
    fun onInventoryOpen(e: InventoryOpenEvent) {
        e.inventory.location?.let { inventoryLocation ->
            locketteService.getChestLocationForChestBlockAt(inventoryLocation.world, inventoryLocation.blockCoord)
        }?.let {
            if (!handleAccessAttemptEvent(it, e.player)) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        locketteService.getClaimedChestForBlockAt(e.block.world, e.block.coord)?.let { claimedChest ->
            val claimingSignCoord = claimedChest.claimingSign.block.coord
            val brokenBlockCoord = e.block.coord

            if (claimedChest.protectedCoords.contains(brokenBlockCoord)) {
                if (locketteService.isAccessAllowedToClaimedChest(claimedChest, e.player)) {
                    // Notify player that their chest will become unprotected if the broken block is the
                    // claiming sign or the chest block in which it lies on, when it is a double chest.
                    if (brokenBlockCoord == claimingSignCoord ||
                        (brokenBlockCoord.advance(claimedChest.location.facing) == claimingSignCoord) &&
                        claimedChest.location is ChestLocation.Double
                    ) {
                        plugin.sendActionBarMessage(e.player, PluginMessages::chestUnclaimed)
                    }

                    logger.fine { "Player ${e.player} is allowed to break a block from the claimed chest $claimedChest" }
                } else {
                    logger.fine { "Player ${e.player} is allowed to break a block from the claimed chest $claimedChest" }
                    e.isCancelled = true
                    plugin.sendActionBarMessage(e.player, PluginMessages::cannotBreakProtectedBlock)
                }
            }
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val world = e.blockPlaced.world
        val blockAgainstData = e.blockAgainst.blockData
        val blockPlacedData = e.blockPlaced.blockData

        if (blockAgainstData is Chest) {
            if (blockPlacedData is Chest) {
                val claimedChest = locketteService.getClaimedChestForChestAt(world, e.blockAgainst.coord) ?: return
                if (!locketteService.isAccessAllowedToClaimedChest(claimedChest, e.player)) {
                    // Prevent players to expand claimed chest when they are unauthorized
                    logger.fine {
                        "Player ${e.player} attempted to expand the claimed chest $claimedChest, being unauthorized"
                    }
                    plugin.sendActionBarMessage(e.player, PluginMessages::unauthorizedToExtendChest)
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onEntityKaboom(e: EntityExplodeEvent) {
        removeProtectedBlocksFromExplosion(e.blockList())
    }

    @EventHandler
    fun onBlockKaboom(e: BlockExplodeEvent) {
        removeProtectedBlocksFromExplosion(e.blockList())
    }

    @EventHandler
    fun onSignChange(e: SignChangeEvent) {
        if (e.side != Side.FRONT) {
            return
        }
        val chestLocation = locketteService.getChestLocationForClaimingSignAt(e.block.world, e.block.coord) ?: return
        val claimedChest = locketteService.getClaimedChestForChest(chestLocation)

        if (claimedChest != null && !locketteService.isAccessAllowedToClaimedChest(claimedChest, e.player)) {
            if (e.block.coord == claimedChest.claimingSign.block.coord) {
                logger.fine {
                    "Player attempted to change a sign that claims the chest at ${claimedChest.location}, without permission"
                }
                plugin.sendActionBarMessage(e.player, PluginMessages::unauthorizedToChangeClaimingSign)
                e.isCancelled = true
            } else if (locketteService.isSignTextClaimingAChest(e.lines())) {
                logger.fine {
                    "Player attempted to claim an already claimed chest at ${claimedChest.location}, without permission"
                }
                plugin.sendActionBarMessage(e.player, PluginMessages::chestAlreadyClaimed)
                e.isCancelled = true
            }
            return
        }

        val result =
            locketteService.reclaimChest(chestLocation, e.player) {
                if (it == e.block.coord) {
                    Pair(e.block.state as Sign, e.lines())
                } else {
                    (e.block.world.getBlockAt(it) as? Sign)?.let {
                        Pair(it, it.getSide(Side.FRONT).lines())
                    }
                }
            }

        if (result != null) {
            plugin.sendActionBarMessage(e.player, PluginMessages::chestClaimed)

            // Manually setting here the text of the sign instead of calling refreshClaimSignMetadataAndText because
            // any modifications to the text of the sign inside this event must go through the event itself.
            locketteService.buildLinesForClaimingSign(result).forEachIndexed { index, component ->
                e.line(index, component)
            }
        } else if (claimedChest != null && e.block.coord == claimedChest.claimingSign.block.coord) {
            plugin.sendActionBarMessage(e.player, PluginMessages::chestUnclaimed)
        }
    }
}
