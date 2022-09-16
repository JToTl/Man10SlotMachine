package ltotj.minecraft.man10slotmachine.slot.data


import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.slot.Man10Slot
import org.bukkit.Location
import org.bukkit.entity.Player

class MadeSlotData(val name:String,val innerSlotName:String,player:Player) {

    var finishedItemFrame=false
    var alone=Main.slots[innerSlotName]!!.alone
    val itemFrameLocation=ArrayList<Location>()
    lateinit var leverLocation:Location

    init {
        player.sendMessage("${Main.pluginTitle}§a額縁を")
        player.sendMessage("${Main.pluginTitle}§a1,2,3")
        if(!alone){
            player.sendMessage("${Main.pluginTitle}§a4,5,6")
            player.sendMessage("${Main.pluginTitle}§a7,8,9")
        }
        player.sendMessage("${Main.pluginTitle}§aの順に設置してください")
    }

    fun addLoc(location: Location):Boolean{
        itemFrameLocation.add(location)
        return (alone&&(itemFrameLocation.size==3))||itemFrameLocation.size==9
    }

    fun create(leverLocation:Location){
        this.leverLocation=leverLocation.toCenterLocation()
        Man10Slot(this)
    }

}