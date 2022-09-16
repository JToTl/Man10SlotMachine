package ltotj.minecraft.man10slotmachine.event

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.slot.Man10Slot
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.util.UUID

object CreateChairEvent:Listener {

    val chairMaker=HashMap<UUID,Man10Slot>()

    @EventHandler
    fun setChair(e:BlockBreakEvent){
        val uuid=e.player.uniqueId
        if(!chairMaker.containsKey(uuid))return
        e.isCancelled=true
        if(Main.placedSlots.containsValue(chairMaker[uuid])){
            chairMaker[uuid]!!.createChair(e.block.location)
            e.player.sendMessage("${Main.pluginTitle}§a椅子を設置しました")
        }
        else{
            e.player.sendMessage("${Main.pluginTitle}§4スロットが見つかりませんでした")
        }
        chairMaker.remove(e.player.uniqueId)
    }


}