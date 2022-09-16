package ltotj.minecraft.man10slotmachine.slot.data

import org.bukkit.configuration.ConfigurationSection

class FreezeData(val freezeName:String,config:ConfigurationSection) {

    val kind=config.getString("kind","slow")
    val chance=config.getDouble("chance",1.0)
    val step=config.getInt("step",100)
    val sleep=config.getLong("sleep",300L)
    val command: MutableList<String> =config.getStringList("command")
    var predictSound:SoundData?=null
    val stopSounds=HashMap<Int,SoundData>()

    init {
        if(config.get("predict_sound")!=null){
            predictSound= SoundData(config.getConfigurationSection("predict_sound")!!)
        }

        for(i in 0 until 3){
            if(config.get("stop${i+1}_sound")!=null){
                stopSounds[i+1]= SoundData(config.getConfigurationSection("stop${i+1}_sound")!!)
            }
        }
    }
}