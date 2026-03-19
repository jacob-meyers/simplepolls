package me.nickotato.simplePolls.guis

import me.nickotato.simplePolls.managers.PollsManager
import me.nickotato.simplePolls.model.Poll
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import java.time.LocalDateTime

class ViewExpiredPollsGui: Gui(Component.text("§8Viewing Expired Polls"),54) {
    private var page = 1
    private val air = ItemStack(Material.AIR)

    init {
        buildPage()
    }

    private fun buildPage() {
        for (i in 0 until 53) {
            setItem(i, air)
        }
        for ((index, poll) in getViewablePolls().withIndex()) {
            val item = ItemStack(Material.WRITTEN_BOOK)
            val meta = item.itemMeta as BookMeta
            meta.displayName(Component.text("§6${poll.question}"))
            meta.lore(listOf<Component>(
                Component.text("§7Winner was: §d${PollsManager.getOptionWithMostVotes(poll)}"),
                Component.text("§7Ended on ${poll.endsAt}")
            )
            )
            item.itemMeta = meta
            setItem(index,item)
        }

        buildNavigationItems()
    }

    private fun getViewablePolls(): List<Poll> {
        val polls = PollsManager.expiredPolls
        val viewablePolls = polls.drop(45 * (page - 1 )).take(45)
        return viewablePolls
    }

    private fun buildNavigationItems() {
        val default = ItemStack(Material.ARROW)
        val next = default.clone()
        val nextMeta = next.itemMeta
        nextMeta.displayName(Component.text("Next Page"))
        next.itemMeta = nextMeta

        val previous = default.clone()
        val previousMeta = previous.itemMeta
        previousMeta.displayName(Component.text("Previous Page"))
        previous.itemMeta = previousMeta


        if (page > 1) setItem(45, previous)
        val viewableLast = getViewablePolls().lastOrNull() ?: return
        if (viewableLast != PollsManager.expiredPolls.last()) setItem(53, next)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked
        if (player !is Player) return
        val slot = event.slot

        when (slot) {
            45 -> {
                page--
                buildPage()
            }
            53 -> {
                page++
                buildPage()
            }
        }
    }
}