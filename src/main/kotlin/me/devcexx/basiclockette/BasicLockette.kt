package me.devcexx.basiclockette

import org.bukkit.plugin.java.JavaPlugin

class BasicLockette : JavaPlugin() {
    companion object : Logging by logFor<BasicLockette>()

    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(WorldListener(LocketteService(this)), this)
        logger.info("BasicLockette plugin enabled")
    }

    override fun onDisable() {
        super.onDisable()
        logger.info("BasicLockette plugin disabled")
    }
}
