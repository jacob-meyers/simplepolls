package me.nickotato.simplePolls.guis

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

abstract class Gui(title: Component, size: Int) {
    val inventory = Bukkit.createInventory(null, size, title)

    open fun open(player: Player) {
        player.openInventory(inventory)
    }

    open fun onClose(player: Player) {
    }

    abstract fun onClick(event: InventoryClickEvent)

    fun setItem(slot: Int, item: ItemStack) {
        inventory.setItem(slot, item)
    }
}
