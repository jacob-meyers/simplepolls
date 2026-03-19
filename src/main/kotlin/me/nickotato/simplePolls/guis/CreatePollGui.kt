package me.nickotato.simplePolls.guis

import me.nickotato.simplePolls.SimplePolls
import me.nickotato.simplePolls.listeners.PollChatListener
import me.nickotato.simplePolls.managers.GuiManager
import me.nickotato.simplePolls.managers.PollsManager
import me.nickotato.simplePolls.utils.DurationParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class CreatePollGui:Gui(Component.text("§8Creating Poll"),4 * 9) {
    private val gui = this
    private var name = "Undefined"
    private val options = mutableListOf<String>()
    private var duration: Long = 0

    init {
        updateNameItem()
        updateDurationItem()
        updateOptionsItem()

        val create = ItemStack(Material.LIME_DYE)
        val createMeta = create.itemMeta
        createMeta.displayName(Component.text("§2Create Poll"))
        create.itemMeta = createMeta
        setItem(32, create)

        val cancel = ItemStack(Material.RED_DYE)
        val cancelMeta = cancel.itemMeta
        cancelMeta.displayName(Component.text("§cCancel"))
        cancel.itemMeta = cancelMeta
        setItem(30, cancel)
    }

    private fun updateNameItem() {
        val nameItem = ItemStack(Material.NAME_TAG, 1)
        val meta = nameItem.itemMeta
        meta.displayName(Component.text("§6Poll Question"))
        meta.lore(listOf(Component.text("§7Current Question: "), Component.text("§5$name")))
        nameItem.itemMeta = meta
        setItem(11, nameItem)
    }

    private fun updateDurationItem() {
        val durationItem = ItemStack(Material.CLOCK, 1)
        val meta = durationItem.itemMeta
        meta.displayName(Component.text("§6Set Duration (e.g., 1d 2h 5m)"))
        val durationText = if (duration <= 0) "Not set" else DurationParser.formatDuration(duration)
        meta.lore(listOf(Component.text("§7Current Duration: "), Component.text("§5$durationText")))
        durationItem.itemMeta = meta
        setItem(15, durationItem)
    }

    private fun updateOptionsItem() {
        val optionsItem = ItemStack(Material.PAPER, 1)
        val meta = optionsItem.itemMeta
        meta.displayName(Component.text("§6Add Option"))
        val lore = mutableListOf<Component>()
        for (option in options) {
            lore.add(Component.text("§5● $option"))
        }
        meta.lore(lore)
        optionsItem.itemMeta = meta
        setItem(13, optionsItem)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        val slot = event.slot

        when (slot) {
            11 -> {
                player.closeInventory()
                PollChatListener.requestInput(player, PollChatListener.InputType.NAME) { input ->
                    Bukkit.getScheduler().runTask(SimplePolls.instance, Runnable {
                        name = input
                        updateNameItem()
                        GuiManager.open(gui, player)
                    })
                }
            }
            13 -> {
                player.closeInventory()
                PollChatListener.requestInput(player, PollChatListener.InputType.OPTION) { input ->
                    Bukkit.getScheduler().runTask(SimplePolls.instance, Runnable {
                        options.add(input)
                        updateOptionsItem()
                        GuiManager.open(gui, player)
                    })
                }
            }
            15 -> {
                player.closeInventory()
                PollChatListener.requestInput(player, PollChatListener.InputType.DURATION) { input ->
                    Bukkit.getScheduler().runTask(SimplePolls.instance, Runnable {
                        val parsed = try {
                            DurationParser.parseDuration(input)
                        } catch (e: IllegalArgumentException) {
                            player.sendMessage("§a${e.message}")
                            return@Runnable
                        }

                        duration = parsed
                        updateDurationItem()
                        GuiManager.open(gui, player)
                    })
                }

            }
            30 -> {
                player.closeInventory()
                player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            }
            32 -> {
                player.closeInventory()
                PollsManager.createPoll(name, options, duration)
                Bukkit.broadcast(Component.text("§3------------------------------"))
                val pollLine = Component.text("§3${name} ")
                    .append(
                        Component.text("§b/poll")
                            .clickEvent(ClickEvent.runCommand("/poll"))
                            .hoverEvent(HoverEvent.showText(Component.text("§7Click to vote")))
                    )
                    .append(Component.text("§3 to vote!"))
                Bukkit.broadcast(pollLine)
                Bukkit.broadcast(Component.text("§3------------------------------"))
                player.playSound(player.location, Sound.ENTITY_VILLAGER_YES, 1f, 1f)
            }
        }
    }

}




