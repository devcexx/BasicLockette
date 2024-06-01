package me.devcexx.basiclockette

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.WallSign
import org.bukkit.block.sign.Side
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class LocketteService(private val plugin: BasicLockette) {
    companion object : Logging by logFor<WorldListener>() {
        private val KEY_CLAIMER_ID = NamespacedKey.fromString("basiclockette:claimer_id")!!
        private val KEY_CLAIMER_LAST_KNOWN_NAME = NamespacedKey.fromString("basiclockette:claimer_last_known_name")!!
    }

    private fun perpendicularChestFace(face: BlockFace): BlockFace =
        when (face) {
            BlockFace.NORTH -> BlockFace.EAST
            BlockFace.EAST -> BlockFace.SOUTH
            BlockFace.SOUTH -> BlockFace.WEST
            BlockFace.WEST -> BlockFace.NORTH
            else -> throw IllegalArgumentException("Unsupported block face for chest: $face")
        }

    fun getChestLocationForChestBlockAt(chest: Chest): ChestLocation {
        val facing = (chest.blockData as Directional).facing
        val coord = chest.location.blockCoord
        val world = chest.world

        return when (chest.inventory) {
            is DoubleChestInventory -> {
                val tileEntityLocation =
                    chest.inventory.location?.blockCoord ?: throw IllegalStateException(
                        "Expected inventory of chest at $world; $coord to have a proper location set!",
                    )

                if (tileEntityLocation != coord) {
                    // First case: given coord does not match with tile entity location. In such case,
                    // we determine that the chest is conformed by the given coord and the tile entity location.
                    ChestLocation.Double(
                        world,
                        world.getBlockAt(tileEntityLocation).state as Chest,
                        facing,
                        tileEntityLocation,
                        coord,
                    )
                } else {
                    // Second case: given coord matches tile entity location. In such case, we need to figure out the
                    // coord of the other block that composes the double chest.
                    val secondCoord =
                        listOf(
                            perpendicularChestFace(facing),
                            perpendicularChestFace(facing.oppositeFace),
                        ).map(coord::advance)
                            .find { candidateCoord ->
                                coord == (world.getBlockAt(candidateCoord).state as? Chest)?.inventory?.location?.blockCoord
                            }
                            ?: throw IllegalStateException("Couldn't find second block for the double chest at $world; $coord")

                    ChestLocation.Double(world, chest, facing, tileEntityLocation, secondCoord)
                }
            }

            else -> ChestLocation.Single(world, chest, facing, coord)
        }
    }

    fun getChestLocationForChestBlockAt(
        world: World,
        coord: BlockCoord,
    ): ChestLocation? = (world.getBlockAt(coord).state as? Chest)?.let(::getChestLocationForChestBlockAt)

    fun getChestLocationForClaimingSignAt(
        world: World,
        coord: BlockCoord,
    ): ChestLocation? {
        val wallSign = world.getBlockAt(coord).blockData as? WallSign ?: return null
        val attachedChest = world.getBlockAt(coord.advance(wallSign.facing.oppositeFace)).state as? Chest ?: return null

        return getChestLocationForChestBlockAt(attachedChest)
    }

    fun getClaimedChestForSignAt(
        world: World,
        signCoord: BlockCoord,
    ): ClaimedChest? {
        val attachedChest =
            (world.getBlockAt(signCoord).blockData as? WallSign)?.let { wallSign ->
                getChestLocationForChestBlockAt(world, signCoord.advance(wallSign.facing.oppositeFace))
            } ?: return null

        if (!attachedChest.claimingSignCoords.contains(signCoord)) {
            return null
        }

        return getClaimedChestForChest(attachedChest)
    }

    fun getClaimedChestForChest(chestLocation: ChestLocation): ClaimedChest? =
        chestLocation.claimingSignCoords
            .mapNotNull { chestLocation.world.getBlockAt(it).state as? Sign }
            .firstNotNullOfOrNull { sign ->
                // For consistency with what the player sees, make sure we not only check the owner metadata
                // but also that the sign is correct. If the sign text is not correct, we ignore ownership metadata.
                if (isSignTextClaimingAChest(sign.getSide(Side.FRONT).lines())) {
                    getChestOwnerFromClaimingSign(sign)?.let { Pair(sign, it) }
                } else {
                    null
                }
            }?.let { (sign, profile) ->
                ClaimedChest(profile, chestLocation, sign)
            }

    fun getClaimedChestForChestAt(
        world: World,
        chestCoord: BlockCoord,
    ): ClaimedChest? = getChestLocationForChestBlockAt(world, chestCoord)?.let(::getClaimedChestForChest)

    fun getClaimedChestForBlockAt(
        world: World,
        chestOrSignCoord: BlockCoord,
    ): ClaimedChest? {
        val block = world.getBlockAt(chestOrSignCoord)
        val blockState = block.state
        val blockData = block.blockData

        return when {
            blockData is WallSign -> {
                getClaimedChestForSignAt(block.world, block.coord)
            }

            blockState is Chest -> {
                getClaimedChestForChest(getChestLocationForChestBlockAt(blockState))
            }

            else -> null
        }
    }

    fun getChestOwnerFromClaimingSign(sign: Sign): PlayerProfile? {
        val id = sign.persistentDataContainer[KEY_CLAIMER_ID, PersistentDataType.STRING]
        val lastKnownName = sign.persistentDataContainer[KEY_CLAIMER_LAST_KNOWN_NAME, PersistentDataType.STRING]

        return if (id == null && lastKnownName == null) {
            return null
        } else if (id == null) {
            logger.severe(
                "Claimer last known name is available in chest metadata at $sign, " +
                    "but player id is not. This should not happen!",
            )
            null
        } else if (lastKnownName == null) {
            logger.severe(
                "Claimer ID is available in chest metadata at $sign, but last name is not. " +
                    "This should not happen!",
            )
            null
        } else {
            val parsedId =
                try {
                    UUID.fromString(id)
                } catch (e: IllegalArgumentException) {
                    logger.severe("Invalid player ID stored at chest metadata $sign")
                    return null
                }

            PlayerProfile(parsedId, lastKnownName)
        }
    }

    fun setChestOwnerInClaimingSign(
        sign: Sign,
        profile: PlayerProfile?,
    ) {
        if (profile == null) {
            sign.persistentDataContainer.remove(KEY_CLAIMER_ID)
            sign.persistentDataContainer.remove(KEY_CLAIMER_LAST_KNOWN_NAME)
        } else {
            sign.persistentDataContainer.set(
                KEY_CLAIMER_ID,
                PersistentDataType.STRING,
                profile.id.toString(),
            )
            sign.persistentDataContainer.set(
                KEY_CLAIMER_LAST_KNOWN_NAME,
                PersistentDataType.STRING,
                profile.lastKnownName,
            )
        }
        sign.update()
    }

    fun isSignTextClaimingAChest(signComponent: List<Component>): Boolean =
        signComponent.mapNotNull {
            (it as? TextComponent)?.content()
        }.firstOrNull {
            it.isNotBlank()
        } == "[private]"

    fun buildLinesForClaimingSign(claimedChest: ClaimedChest): List<Component> {
        return listOf(
            Component.text("[private]"),
            Component.text("Chest owned by"),
            Component.text(claimedChest.owner.lastKnownName),
            Component.text(""),
        )
    }

    fun refreshClaimSignMetadataAndText(claimedChest: ClaimedChest): ClaimedChest {
        val onlineOwner = plugin.server.getPlayer(claimedChest.owner.id)
        val ownerProfile =
            if (onlineOwner == null) {
                claimedChest.owner
            } else {
                claimedChest.owner.copy(lastKnownName = onlineOwner.name)
            }

        val signSide = claimedChest.claimingSign.getSide(Side.FRONT)

        buildLinesForClaimingSign(claimedChest).forEachIndexed { index, component ->
            signSide.line(index, component)
        }

        claimedChest.claimingSign.update()

        return if (onlineOwner == null) {
            claimedChest
        } else {
            setChestOwnerInClaimingSign(claimedChest.claimingSign, ownerProfile)
            claimedChest.copy(owner = ownerProfile)
        }
    }

    fun reclaimChest(
        location: ChestLocation,
        claimer: HumanEntity,
        signDataProvider: (BlockCoord) -> Pair<Sign, List<Component>>?,
    ): ClaimedChest? {
        val existingSigns =
            location.claimingSignCoords
                .mapNotNull(signDataProvider)

        var newClaimingSign: Sign? = null

        // Make sure that everytime we reclaim a chest, we just keep claim data in a single sign and,
        // if the text of a sign doesn't make it a claiming sign anymore, we just remove claiming data.
        existingSigns.forEach { (sign, text) ->
            if (isSignTextClaimingAChest(text) && newClaimingSign == null) {
                newClaimingSign = sign
                setChestOwnerInClaimingSign(sign, claimer.playerProfile)
            } else {
                setChestOwnerInClaimingSign(sign, null)
            }
        }

        return newClaimingSign?.let { ClaimedChest(claimer.playerProfile, location, it) }
    }
}
