package me.devcexx.basiclockette.i18n.translations

import me.devcexx.basiclockette.chatColorError
import me.devcexx.basiclockette.chatColorInfo
import me.devcexx.basiclockette.chatColorWarning
import me.devcexx.basiclockette.i18n.PluginMessages
import net.kyori.adventure.text.Component

object EnUs : PluginMessages {
    override val chestClaimed: Component = "This chest is now yours!".chatColorInfo
    override val chestUnclaimed: Component = "You just unclaimed this chest. Beware!".chatColorWarning
    override val unauthorizedToOpenChest: Component = "You can't open this chest!".chatColorError
    override val unauthorizedToExtendChest: Component = "I appreciate your generosity, but that chest is not yours!".chatColorError
    override val unauthorizedToChangeClaimingSign: Component = "You don't own this chest, you can't do that!".chatColorError
    override val chestAlreadyClaimed: Component = "This chest is already claimed by someone else!".chatColorError
    override val cannotBreakProtectedBlock: Component = "You cannot break this!".chatColorError
}
