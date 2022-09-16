package ltotj.minecraft.man10slotmachine.event

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.slot.data.MadeSlotData
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import java.util.*
import kotlin.collections.HashMap

object CreateSlotEvent:Listener{


    val frameMaker=HashMap<UUID,MadeSlotData>()



    @EventHandler
    fun placeItemFrame(e:HangingPlaceEvent){
        val player=e.player?:return
        val data=frameMaker[player.uniqueId]?:return
        if(e.entity.type!=EntityType.ITEM_FRAME)return
        if(data.finishedItemFrame)return

        player.sendMessage("${Main.pluginTitle}§a登録しました")
        if(data.addLoc(e.entity.location.toCenterLocation())){
            data.finishedItemFrame=true
            player.sendMessage("${Main.pluginTitle}§a次にレバーを設置してください")
        }
    }

    @EventHandler
    fun placeLever(e:BlockPlaceEvent){
        val player=e.player
        val data= frameMaker[player.uniqueId]?:return
        val block=e.block
        if(block.type!= Material.LEVER)return
        if(!data.finishedItemFrame)return
        if(Main.levers.containsKey(block.location.toCenterLocation())){
            player.sendMessage("${Main.pluginTitle}§4その位置には既にレバーが設置されています")
            return
        }
        frameMaker.remove(player.uniqueId)
        if(Main.placedSlots.containsKey(data.name)){
            player.sendMessage("${Main.pluginTitle}§4識別名にエラーが発生しました")
            return
        }
        if(!Main.slots.containsKey(data.innerSlotName)){
            player.sendMessage("${Main.pluginTitle}§4スロットデータにエラーが発生しました")
            return
        }
        data.create(block.location)
        player.sendMessage("${Main.pluginTitle}§a設置完了")
    }


}