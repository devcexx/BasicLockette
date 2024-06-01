package me.devcexx.basiclockette.i18n.translations

import me.devcexx.basiclockette.chatColorWarning
import me.devcexx.basiclockette.i18n.PluginMessages
import net.kyori.adventure.text.Component

object EsEsCustom : PluginMessages by EsEs {
    override val chestUnclaimed: Component = "Este cofre vuelve a ser público. Cuidado con Peta!".chatColorWarning
}
