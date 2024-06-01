package me.devcexx.basiclockette

import me.devcexx.basiclockette.commands.GodModeCommand
import me.devcexx.basiclockette.i18n.PluginMessages
import me.devcexx.basiclockette.i18n.PluginTranslations
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class BasicLockette : JavaPlugin() {
    companion object : Logging by logFor<BasicLockette>()

    private val messages =
        PluginTranslations.buildDefault(
            System.getProperty("basiclockette.customtranslations") == "1",
        )
    val locketteService = LocketteService(this)

    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(WorldListener(this, locketteService), this)
        server.getPluginCommand("blgod")!!.setExecutor(GodModeCommand(this))

        logger.info("BasicLockette plugin enabled")
    }

    override fun onDisable() {
        super.onDisable()
        logger.info("BasicLockette plugin disabled")
    }

    fun sendActionBarMessage(
        player: Player,
        message: (PluginMessages) -> Component,
    ) {
        player.sendActionBar(message(messages.messagesForLocale(player.locale())))
    }
}
