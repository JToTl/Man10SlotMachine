package ltotj.minecraft.man10slotmachine.slot.data

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.utilities.ItemManager.ItemStackPlus
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import kotlin.random.Random

class WinningData(val innerWinName:String,private val config:ConfigurationSection,val slot:SlotData){

    val winName=config.getString("win_name","当たり")
    val prize=config.getDouble("prize",0.0)
    val payStock=config.getDouble("pay_stock",0.0)
    val addStock=config.getDouble("add_stock",0.0)
    val resetStock=config.getBoolean("reset_stock",true)
    val level=config.getInt("level",2)
    val addGameCount=config.getInt("add_game_count",0)
    val winningPattern=ArrayList<String>()
    val changeTable=ArrayList<ChangeTable>()
    val command: MutableList<String> =config.getStringList("command")
    val coinDrop=config.getBoolean("coin_drop",false)
    var coinDropItem:ItemStackPlus?=null
    var winSound:SoundData?=null
    var particleData: ParticleData?=null
    var freezeData:FreezeData?=null

    inner class ChangeTable(str:String){
        var table:TableData?=null
        var chance=0.0
        var count=0
        init {
            val list=str.split("-")
            table=slot.tables[list[0]]
            chance=list[1].toDoubleOrNull()?:0.0
            count=list[2].toIntOrNull()?:1
            if(table==null){
                println("${Main.pluginTitle}不明なテーブルが指定されています")
            }
        }
    }

    init {

        if(config.get("freeze")!=null){
            freezeData=slot.freezes[config.getString("freeze")]
        }
        if(config.get("coin_drop_item")!=null){
            coinDropItem= ItemStackPlus(Material.valueOf(config.getString("coin_drop_item.material","BARRIER")!!),1)
                .setCustomModelData(config.getInt("coin_drop_item.cmd",1))
        }
        if(config.get("win_sound")!=null){
            winSound= SoundData(config.getConfigurationSection("win_sound")!!)
        }
        if(config.get("win_particle")!=null){
            particleData= ParticleData(config.getConfigurationSection("win_particle")!!)
        }

        if(slot.alone){
            winningPattern.add(config.getString("item")!!)
        }
        else{
            val list=ArrayList<Int>()
            for(num in config.getString("item")!!.split(",")){
                list.add(num.toInt())
            }
            winningPattern.add("${list[0]},${list[1]},${list[2]}")
            winningPattern.add("${(list[0]%slot.reels[1]!!.size)+1},${(list[1]%slot.reels[2]!!.size)+1},${(list[2]%slot.reels[3]!!.size)+1}")
            winningPattern.add("${((list[0]+1)%slot.reels[1]!!.size)+1},${((list[1]+1)%slot.reels[2]!!.size)+1},${((list[2]+1)%slot.reels[3]!!.size)+1}")
            winningPattern.add("${(list[0]%slot.reels[1]!!.size)},${(list[1]%slot.reels[2]!!.size)+1},${((list[2]+1)%slot.reels[3]!!.size)+1}")
            winningPattern.add("${((list[0]+1)%slot.reels[1]!!.size)+1},${(list[1]%slot.reels[2]!!.size)+1},${(list[2]%slot.reels[3]!!.size)}")
        }
    }

    fun loadChangeTable(){
        for(str in config.getStringList("change_table")){
            changeTable.add(ChangeTable(str))
        }
    }

    fun isFreezing():Boolean{
        return Random.nextDouble() < (freezeData?.chance ?: 0.0)
    }

    fun getChangeTable():ChangeTable?{
        for(change in changeTable){
            if(Random.nextDouble()<change.chance){
                if(change.table==null)return null
                return change
            }
        }
        return null
    }

}