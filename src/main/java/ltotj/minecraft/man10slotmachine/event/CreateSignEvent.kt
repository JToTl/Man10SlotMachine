package ltotj.minecraft.man10slotmachine.event

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.slot.Man10Slot
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
import java.util.UUID

object CreateSignEvent:Listener {

    val signMaker=HashMap<UUID,Man10Slot>()


    @EventHandler
    fun setSign(e: SignChangeEvent){
        val uuid=e.player.uniqueId
        if(!signMaker.containsKey(uuid))return
        val block=e.block.state
        if(block !is Sign)return
        if(!Main.placedSlots.containsKey(signMaker[uuid]!!.dataName)){
            e.player.sendMessage("${Main.pluginTitle}§4スロットが見つかりませんでした")
            return
        }
        signMaker[uuid]!!.createSign(block)
        e.player.sendMessage("${Main.pluginTitle}§a看板を設置しました")
        signMaker.remove(uuid)
    }

}