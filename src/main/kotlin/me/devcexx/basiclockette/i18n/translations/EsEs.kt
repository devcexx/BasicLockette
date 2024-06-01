package me.devcexx.basiclockette.i18n.translations

import me.devcexx.basiclockette.chatColorError
import me.devcexx.basiclockette.chatColorInfo
import me.devcexx.basiclockette.chatColorWarning
import me.devcexx.basiclockette.i18n.PluginMessages
import net.kyori.adventure.text.Component

object EsEs : PluginMessages {
    override val chestClaimed: Component = "Ahora este cofre es tuyo!".chatColorInfo
    override val chestUnclaimed: Component = "Este cofre vuelve a ser público. Ten cuidado!".chatColorWarning
    override val unauthorizedToOpenChest: Component = "No puedes abrir este cofre!".chatColorError
    override val unauthorizedToExtendChest: Component = "No puedes extender un cofre que no es tuyo!".chatColorError
    override val unauthorizedToChangeClaimingSign: Component = "No puedes cambiar este cartel!".chatColorError
    override val chestAlreadyClaimed: Component = "Este cofre ya es de otro jugador!".chatColorError
    override val cannotBreakProtectedBlock: Component = "No puedes romper eso!".chatColorError
}
