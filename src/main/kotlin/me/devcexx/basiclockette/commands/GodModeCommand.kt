package me.devcexx.basiclockette.commands

import me.devcexx.basiclockette.BasicLockette
import me.devcexx.basiclockette.chatColorError
import me.devcexx.basiclockette.chatColorFine
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GodModeCommand(private val plugin: BasicLockette) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!sender.isOp) {
            sender.sendMessage("Unauthorized".chatColorError)
            return true
        }

        val targetPlayer: Player
        if (args.size > 2) {
            sender.sendMessage("Unknown parameters".chatColorError)
            return true
        } else if (args.isNotEmpty()) {
            targetPlayer = plugin.server.getPlayer(args.first()) ?: run {
                sender.sendMessage("Player ${args.first()} is not online".chatColorError)
                return true
            }
        } else if (sender !is Player) {
            sender.sendMessage("Player name is required".chatColorError)
            return true
        } else {
            targetPlayer = sender
        }

        val godMode = plugin.locketteService.toggleGodMode(targetPlayer)
        if (godMode) {
            sender.sendMessage("Enabled god mode for player ${targetPlayer.name}".chatColorFine)
        } else {
            sender.sendMessage("Disabled god mode for player ${targetPlayer.name}".chatColorFine)
        }

        return true
    }
}
