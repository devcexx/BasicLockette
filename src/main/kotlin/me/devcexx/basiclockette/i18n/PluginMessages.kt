package me.devcexx.basiclockette.i18n

import net.kyori.adventure.text.Component

interface PluginMessages {
    val chestClaimed: Component
    val chestUnclaimed: Component
    val unauthorizedToOpenChest: Component
    val unauthorizedToExtendChest: Component
    val unauthorizedToChangeClaimingSign: Component
    val chestAlreadyClaimed: Component
    val cannotBreakProtectedBlock: Component
}
