package ltotj.minecraft.man10slotmachine.event

import ltotj.minecraft.man10slotmachine.Main
import net.md_5.bungee.api.chat.HoverEvent.Action
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

object SpinSlotEvent:Listener {

    @EventHandler
    fun spinSlot(e:PlayerInteractEvent){


        if(e.action==org.bukkit.event.block.Action.RIGHT_CLICK_AIR||e.action==org.bukkit.event.block.Action.LEFT_CLICK_AIR)return
        val slot=Main.levers[e.clickedBlock?.location?.toCenterLocation()]?:return
        slot.start(e.player)

    }


}