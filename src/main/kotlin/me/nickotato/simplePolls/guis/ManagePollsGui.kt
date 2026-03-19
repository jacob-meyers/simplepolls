package me.nickotato.simplePolls.guis

import me.nickotato.simplePolls.SimplePolls
import me.nickotato.simplePolls.managers.GuiManager
import me.nickotato.simplePolls.managers.PollsManager
import me.nickotato.simplePolls.model.Poll
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.time.LocalDateTime

class ManagePollsGui(private val viewer: Player): Gui(Component.text("§8Manage Polls"),54) {
    private val page = 1
    private var updateTask: BukkitTask? = null

    init {
        val create = ItemStack(Material.KNOWLEDGE_BOOK)
        val createMeta = create.itemMeta
        createMeta.displayName(Component.text("§2Create Poll"))
        create.itemMeta = createMeta
        setItem(49, create)

        for ((index, poll) in getViewablePolls().withIndex()) {
            val pollItem = ItemStack(Material.WRITABLE_BOOK, 1)
            val pollMeta = pollItem.itemMeta
            pollMeta.displayName(Component.text("§6${poll.question}"))
            pollMeta.lore(buildLore(poll, LocalDateTime.now()))

            val pdc = pollMeta.persistentDataContainer
            pdc.set(SimplePolls.POLL_KEY, PersistentDataType.INTEGER, poll.id)

            pollItem.itemMeta = pollMeta
            setItem(index, pollItem)
        }
    }

    override fun open(player: Player) {
        super.open(player)
        startCountdownUpdates()
    }

    override fun onClose(player: Player) {
        stopCountdownUpdates()
    }

    private fun getViewablePolls(): List<Poll> {
        val polls = PollsManager.polls
        val viewablePolls = polls.drop(45 * (page - 1 )).take(45)
        return viewablePolls
    }

    private fun buildLore(poll: Poll, now: LocalDateTime): List<Component> {
        val lore = mutableListOf<Component>()
        val ends = poll.endsAt
        val duration = Duration.between(now, ends)
        if (!duration.isNegative) {
            val totalSecondsLeft = duration.seconds
            val hoursLeft = totalSecondsLeft / 3600
            val minutesLeft = (totalSecondsLeft % 3600) / 60
            val secondsLeft = totalSecondsLeft % 60
            lore.add(Component.text("§7Ends in §3${hoursLeft}h ${minutesLeft}m ${secondsLeft}s"))
        } else {
            lore.add(Component.text("§cExpired"))
        }
        return lore
    }

    private fun startCountdownUpdates() {
        if (updateTask != null) return
        updateTask = Bukkit.getScheduler().runTaskTimer(
            SimplePolls.instance,
            Runnable { updateCountdownLore() },
            20L,
            20L
        )
    }

    private fun stopCountdownUpdates() {
        updateTask?.cancel()
        updateTask = null
    }

    private fun updateCountdownLore() {
        if (viewer.openInventory.topInventory != inventory) {
            stopCountdownUpdates()
            return
        }

        val now = LocalDateTime.now()
        for ((index, poll) in getViewablePolls().withIndex()) {
            val item = inventory.getItem(index) ?: continue
            val meta = item.itemMeta ?: continue
            meta.lore(buildLore(poll, now))
            item.itemMeta = meta
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked
        if (player !is Player) return
        val slot = event.slot

        when (slot) {
            49 -> {
                GuiManager.open(CreatePollGui(), player)
            }
        }

        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer
        val pollId = pdc.get(SimplePolls.POLL_KEY, PersistentDataType.INTEGER) ?: return
        val poll = PollsManager.polls.find { it.id == pollId } ?: return

        GuiManager.open(ManagePollGui(poll), player)
    }
}
