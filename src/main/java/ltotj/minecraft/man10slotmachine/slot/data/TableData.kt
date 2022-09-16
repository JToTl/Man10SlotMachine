package ltotj.minecraft.man10slotmachine.slot.data

import ltotj.minecraft.man10slotmachine.Main.Companion.pluginTitle
import org.bukkit.configuration.ConfigurationSection
import kotlin.random.Random

class TableData(val innerTableName:String,config:ConfigurationSection,val slot:SlotData) {

    val stock=config.getDouble("stock", Double.MIN_VALUE)
    private val winnings=LinkedHashMap<WinningData,Double>()
    var startTableSetting:SettingData?=null
    var endTableSetting:SettingData?=null
    var spinSetting:SettingData?=null
    var endWinning:WinningData?=null
    var spinningSound:SoundData?=null






    init {

        //当たり読み込み
        for(str in config.getStringList("winnings")){
            val list=str.split("-")
            val win=slot.winnings[list[0]]
            val chance=list[1].toDoubleOrNull()
            if(win==null){
                println("${pluginTitle}テーブル設定に不明な当たり役が指定されています")
                println("${pluginTitle}エラー箇所->${innerTableName},${str}")
                continue
            }
            if(chance==null){
                println("${pluginTitle}テーブル設定に不明な確率が指定されています")
                println("${pluginTitle}エラー箇所->${innerTableName},${str}")
                continue
            }
            winnings[win]=chance
        }

        if(config.get("start_table_setting")!=null){
            startTableSetting=SettingData(config.getStringList("start_table_setting.command")
                , config.getConfigurationSection("start_table_setting.sound")?.let { SoundData(it) }
                , config.getConfigurationSection("start_table_setting.particle")?.let { ParticleData(it) })
        }

        if(config.get("end_table_setting")!=null){
            endTableSetting=SettingData(config.getStringList("end_table_setting.command")
                , config.getConfigurationSection("end_table_setting.sound")?.let { SoundData(it) }
                , config.getConfigurationSection("end_table_setting.particle")?.let { ParticleData(it) })
        }

        if(config.get("spin_setting")!=null){
            spinSetting=SettingData(config.getStringList("spin_setting.command")
                , config.getConfigurationSection("spin_setting.sound")?.let { SoundData(it) }
                , config.getConfigurationSection("spin_setting.particle")?.let { ParticleData(it) })
        }

        if(config.get("spinning_sound")!=null){
            spinningSound= SoundData(config.getConfigurationSection("spinning_sound")!!)
        }


    }

//    fun setWinning(win:WinningData,chance:Double){
//        winnings[win]=chance
//    }

    fun getWinning():WinningData? {
        for (win in winnings.keys) {
            if (Random.nextDouble() < winnings[win]!!) {
                return win
            }
        }
        return null
    }


}