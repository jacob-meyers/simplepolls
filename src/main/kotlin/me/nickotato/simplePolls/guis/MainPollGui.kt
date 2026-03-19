package me.nickotato.simplePolls.guis

import me.nickotato.simplePolls.managers.GuiManager
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class MainPollGui(player: Player): Gui(Component.text("§8Polls Menu"), 27) {
    init {
        val view = ItemStack(Material.PAPER, 1)
        val viewMeta = view.itemMeta
        viewMeta.displayName(Component.text("§3View Polls"))
        view.itemMeta = viewMeta
        setItem(11,view)

        val manage = ItemStack(Material.WRITTEN_BOOK, 1)
        val manageMeta = manage.itemMeta
        manageMeta.displayName(Component.text("§3Manage Polls"))
        if (!player.hasPermission("polls.manage")) {
            manageMeta.lore(listOf<Component>(Component.text("§cYou don't have permission to manage polls")))
        }
        manage.itemMeta = manageMeta
        setItem(15, manage)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked
        if (player !is Player) return
        val slot = event.slot

        when (slot) {
            15 -> {
                if (!player.hasPermission("polls.manage")) {
                    player.sendMessage("§cYou don't have permission to manage polls")
                    return
                }

                GuiManager.open(ManagePollsGui(player), player)
            }
            11 -> {
                GuiManager.open(ViewPollsGui(player), player)
            }
        }
    }
}
