package ltotj.minecraft.man10slotmachine

import ltotj.minecraft.man10slotmachine.command.MSlotCommand
import ltotj.minecraft.man10slotmachine.event.CreateSlotEvent
import ltotj.minecraft.man10slotmachine.event.SpinSlotEvent
import ltotj.minecraft.man10slotmachine.event.CreateChairEvent
import ltotj.minecraft.man10slotmachine.event.CreateSignEvent
import ltotj.minecraft.man10slotmachine.slot.DataSaver
import ltotj.minecraft.man10slotmachine.slot.Man10Slot
import ltotj.minecraft.man10slotmachine.slot.data.SlotData
import ltotj.minecraft.man10slotmachine.utilities.ConfigManager.ConfigManager
import ltotj.minecraft.man10slotmachine.utilities.ItemManager.ItemStackPlus
import ltotj.minecraft.man10slotmachine.utilities.MySQLManager.MySQLManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean

class Main : JavaPlugin() {

    companion object{

        lateinit var plugin: JavaPlugin
        const val pluginTitle="§f§l[§d§lMa§f§ln§a§l10§e§lSlot§f§l]§r"
        val slots=HashMap<String,SlotData>()
        val placedSlots=HashMap<String,Man10Slot>()
        val levers=HashMap<Location,Man10Slot>()
        val enable=AtomicBoolean(false)
        var allowSub=true
        var allowMultiSpin=false
        val spinners=LinkedList<Player>()
        val spinnersAddress=LinkedList<String>()
        var useDB=true
        lateinit var con:FileConfiguration
        lateinit var coin:ItemStackPlus
        lateinit var mSlotCommand: MSlotCommand


        fun loadConfig(){
            plugin.saveDefaultConfig()
            con=plugin.config
            enable.set(con.getBoolean("enable",false))
            allowSub=con.getBoolean("allowSub",true)
            allowMultiSpin=con.getBoolean("allowMultiSpin",false)
            useDB=con.getBoolean("use_db",true)
            coin= ItemStackPlus(Material.valueOf(con.getString("coin.material","GOLD_NUGGET")!!),1)
                .setCustomModelData(con.getInt("coin.cmd",0))
        }

        fun loadSlotConfig() {
            slots.clear()
            val slotFile = File("${plugin.dataFolder.absolutePath}${File.separator}slots")
            slotFile.mkdir()
            slotFile.listFiles()?.forEach { file ->
                val list=file.name.split(".")
                if(list.size<2||list[1]!="yml"){
                    return@forEach
                }
                val configFile = YamlConfiguration.loadConfiguration(file)
                for (key in configFile.getKeys(false)) {
                    slots[key] = SlotData(key, configFile.getConfigurationSection(key)!!)
                }
            }
        }

        fun loadDataConfig(){
            placedSlots.clear()
            val dataFile=File("${plugin.dataFolder.absolutePath}${File.separator}data")
            dataFile.mkdir()
            dataFile.listFiles()?.forEach { file->
                val list=file.name.split(".")
                if(list.size<2||list[1]!="yml"){
                    return@forEach
                }
                Man10Slot(ConfigManager(plugin,list[0],"data"))
            }
        }
    }


    override fun onEnable() {
        // Plugin startup logic
        plugin=this
        loadConfig()
        loadSlotConfig()
        loadDataConfig()
        if(useDB){
            DataSaver.start()
        }
        File("${Main.plugin.dataFolder.absolutePath}${File.separator}old_slots").mkdir()
        mSlotCommand=MSlotCommand(this, pluginTitle)
        server.pluginManager.registerEvents(CreateSlotEvent,this)
        server.pluginManager.registerEvents(SpinSlotEvent,this)
        server.pluginManager.registerEvents(CreateChairEvent,this)
        server.pluginManager.registerEvents(CreateSignEvent,this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}