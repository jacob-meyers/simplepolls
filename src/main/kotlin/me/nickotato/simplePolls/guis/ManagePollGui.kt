package me.nickotato.simplePolls.guis

import me.nickotato.simplePolls.SimplePolls
import me.nickotato.simplePolls.listeners.PollChatListener
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
import java.time.Duration
import java.time.LocalDateTime

class ManagePollGui(private val poll: Poll) :
    Gui(Component.text("§8Manage Poll"), 27) {

    private val size = 27

    init {
        draw()
    }

    private fun draw() {
        clear()
        fillBackground()
        setHeader()
        setEditButtons()
        setOptions()
        setActions()
        setInfo()
    }

    private fun clear() {
        for (i in 0 until size) setItem(i, ItemStack(Material.AIR))
    }

    private fun fillBackground() {
        val filler = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val meta = filler.itemMeta
        meta.displayName(Component.text(""))
        filler.itemMeta = meta

        for (i in 0 until size) setItem(i, filler)
    }

    private fun setHeader() {
        val nameItem = ItemStack(Material.NAME_TAG)
        val meta = nameItem.itemMeta
        meta.displayName(Component.text("§b${poll.question}"))
        nameItem.itemMeta = meta
        setItem(4, nameItem)
    }

    private fun setEditButtons() {
        val editName = ItemStack(Material.FEATHER)
        val editMeta = editName.itemMeta
        editMeta.displayName(Component.text("§eEdit Question"))
        editMeta.persistentDataContainer.set(
            SimplePolls.ACTION_KEY,
            PersistentDataType.STRING,
            "edit_question"
        )
        editName.itemMeta = editMeta
        setItem(2, editName)

        // Add option
        val add = ItemStack(Material.LIME_DYE)
        val addMeta = add.itemMeta
        addMeta.displayName(Component.text("§aAdd Option"))
        addMeta.persistentDataContainer.set(
            SimplePolls.ACTION_KEY,
            PersistentDataType.STRING,
            "add_option"
        )
        add.itemMeta = addMeta
        setItem(6, add)
    }

    private fun setOptions() {
        val startSlot = 9
        var slot = startSlot

        for ((option, votes) in poll.options) {
            val item = ItemStack(Material.PAPER)
            val meta = item.itemMeta
            meta.displayName(Component.text("§3$option"))
            meta.lore(
                listOf(
                    Component.text("§7Votes: §b$votes"),
                    Component.text("§cRight-click to remove")
                )
            )

            meta.persistentDataContainer.set(
                SimplePolls.OPTION_KEY,
                PersistentDataType.STRING,
                option
            )
            item.itemMeta = meta

            setItem(slot, item)
            slot++
        }
    }

    private fun setActions() {
        val force = ItemStack(Material.CLOCK)
        val forceMeta = force.itemMeta
        forceMeta.displayName(Component.text("§cForce End Poll"))
        forceMeta.persistentDataContainer.set(SimplePolls.ACTION_KEY, PersistentDataType.STRING, "force_end")
        force.itemMeta = forceMeta
        setItem(20, force)

        val broadcast = ItemStack(Material.NOTE_BLOCK)
        val broadcastMeta = broadcast.itemMeta
        broadcastMeta.displayName(Component.text("§6Broadcast Results"))
        broadcastMeta.persistentDataContainer.set(SimplePolls.ACTION_KEY, PersistentDataType.STRING, "broadcast")
        broadcast.itemMeta = broadcastMeta
        setItem(22, broadcast)

        val delete = ItemStack(Material.BARRIER)
        val deleteMeta = delete.itemMeta
        deleteMeta.displayName(Component.text("§4Delete Poll"))
        deleteMeta.persistentDataContainer.set(SimplePolls.ACTION_KEY, PersistentDataType.STRING, "delete")
        delete.itemMeta = deleteMeta
        setItem(24, delete)
    }

    private fun setInfo() {
        val item = ItemStack(Material.OAK_SIGN)
        val meta = item.itemMeta

        val now = LocalDateTime.now()
        val duration = Duration.between(now, poll.endsAt)
        val status = if (!duration.isNegative) {
            "§aActive (ends in ${duration.toHours()}h)"
        } else {
            "§cExpired"
        }

        val winner = PollsManager.getOptionWithMostVotes(poll)

        meta.displayName(Component.text("§ePoll Info"))
        meta.lore(
            listOf(
                Component.text("§7Status: $status"),
                Component.text("§7Winner: §b$winner"),
                Component.text("§7Options: §3${poll.options.size}/9"),
                Component.text("§7Total Votes: §3${poll.votes.size}")
            )
        )

        item.itemMeta = meta
        setItem(0, item)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return

        val pdc = meta.persistentDataContainer

        val option = pdc.get(SimplePolls.OPTION_KEY, PersistentDataType.STRING)
        if (option != null) {
            if (event.isRightClick) {
                poll.options.remove(option)
                draw()
                return
            }
            return
        }

        val action = pdc.get(SimplePolls.ACTION_KEY, PersistentDataType.STRING) ?: return

        when (action) {

            "edit_question" -> {
                player.closeInventory()
                PollChatListener.requestInput(player, PollChatListener.InputType.NAME) { input ->
                    Bukkit.getScheduler().runTask(SimplePolls.instance, Runnable {
                        poll.question = input
                        player.sendMessage("§aQuestion updated.")
                        GuiManager.open(this, player)
                    })
                }
            }

            "add_option" -> {
                if (poll.options.size >= 9) {
                    player.sendMessage("§cMax 9 options allowed.")
                    return
                }

                player.closeInventory()
                PollChatListener.requestInput(player, PollChatListener.InputType.OPTION) { input ->
                    Bukkit.getScheduler().runTask(SimplePolls.instance, Runnable {
                        poll.options[input] = 0
                        player.sendMessage("§aOption added.")
                        GuiManager.open(this, player)
                    })
                }
            }

            "force_end" -> {
                poll.endsAt = LocalDateTime.now().minusSeconds(1)
                player.sendMessage("§cPoll forced to expire.")
                draw()
            }

            "delete" -> {
                PollsManager.polls.removeIf { it.id == poll.id }
                PollsManager.expiredPolls.removeIf { it.id == poll.id }

                val active = SimplePolls.instance.dataFolder.resolve("polldata/${poll.id}.yml")
                val expired = SimplePolls.instance.dataFolder.resolve("expiredpolldata/${poll.id}.yml")
                if (active.exists()) active.delete()
                if (expired.exists()) expired.delete()

                player.closeInventory()
                player.sendMessage("§4Poll deleted.")
            }

            "broadcast" -> {
                val winner = PollsManager.getOptionWithMostVotes(poll)
                Bukkit.broadcast(Component.text("§6[Poll Results] §e${poll.question} §7→ Winner: §b$winner"))
                player.sendMessage("§aResults broadcast.")
            }
        }
    }
}
