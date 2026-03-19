package me.nickotato.simplePolls.managers

import me.nickotato.simplePolls.guis.Gui
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

object GuiManager: Listener {
    private val openGuis = mutableMapOf<String, Gui>()

    fun open(gui: Gui, player: Player) {
        player.closeInventory()
        openGuis[player.name] = gui
        gui.open(player)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val gui = openGuis[event.whoClicked.name] ?: return

        if (event.clickedInventory == gui.inventory) {
            gui.onClick(event)
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val gui = openGuis.remove(event.player.name) ?: return
        gui.onClose(event.player as Player)
    }
}
