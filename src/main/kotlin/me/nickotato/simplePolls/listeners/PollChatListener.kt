package me.nickotato.simplePolls.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object PollChatListener: Listener {
    private val awaitingInput = mutableMapOf<Player, (String) -> Unit>()

    enum class InputType {
        NAME,
        OPTION,
        DURATION
    }

    private fun promptFor(type: InputType): String {
        val cancelHint = " Type \"§ecancel§a\" to exit input mode."
        return when (type) {
            InputType.NAME -> "§aEnter the poll question:$cancelHint"
            InputType.OPTION -> "§aEnter a response option (one at a time):$cancelHint"
            InputType.DURATION -> "§aEnter poll duration (e.g., 1d 2h 5m):$cancelHint"
        }
    }

    fun requestInput(player: Player, type: InputType, callback: (String) -> Unit) {
        awaitingInput[player] = callback
        player.sendMessage(promptFor(type))
    }

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val callback = awaitingInput[player] ?: return
        event.isCancelled = true

        val inputText: String = PlainTextComponentSerializer.plainText().serialize(event.message())
        if (inputText.trim().equals("cancel", ignoreCase = true)) {
            awaitingInput.remove(player)
            player.sendMessage("§cInput cancelled.")
            return
        }

        callback(inputText)
        awaitingInput.remove(player)
    }
}
