package me.nickotato.simplePolls.guis

import me.nickotato.simplePolls.SimplePolls
import me.nickotato.simplePolls.managers.PollsManager
import me.nickotato.simplePolls.model.Poll
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.iterator

class AnswerPollGui(player: Player, val poll: Poll): Gui(Component.text("§8Cast Your Vote"),27) {

    init {
        val name = ItemStack(Material.NAME_TAG, 1)
        val nameMeta = name.itemMeta
        nameMeta.displayName(Component.text("§6${poll.question}"))
        name.itemMeta = nameMeta
        setItem(4, name)

        updateOptionItems(poll, player)
    }

    private fun updateOptionItems(poll: Poll, player: Player) {
        var slot = 13 - (poll.options.size / 2)

        for (option in poll.options) {
            val item = ItemStack(Material.ANVIL, 1)
            val meta = item.itemMeta
            meta.displayName(Component.text("§3${option.key}"))
            val lore = mutableListOf<Component>(Component.text("${option.value} §7votes"))
            val vote = poll.votes[player.uniqueId.toString()]
            if (vote == option.key) {
                lore.add(Component.text("§aYou voted for this"))
            }
            meta.lore(lore)

            val pdc = meta.persistentDataContainer
            pdc.set(SimplePolls.OPTION_KEY, PersistentDataType.STRING, option.key)
            item.itemMeta = meta

            if (poll.options.size == 2) {
                setItem(slot, item)
                 slot += 2
            }else {
                setItem(slot++, item)
            }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked
        if (player !is Player) return
        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer
        val optionName = pdc.get(SimplePolls.OPTION_KEY, PersistentDataType.STRING) ?: return
//        val option = poll.options[optionName]
        PollsManager.setPlayersAnswer(poll, player, optionName)

        updateOptionItems(poll, player)
    }
}