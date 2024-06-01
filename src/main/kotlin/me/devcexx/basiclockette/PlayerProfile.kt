package me.devcexx.basiclockette

import org.bukkit.entity.HumanEntity
import java.util.UUID

data class PlayerProfile(val id: UUID, val lastKnownName: String)

val HumanEntity.playerProfile get() = PlayerProfile(this.uniqueId, this.name)
