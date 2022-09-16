package ltotj.minecraft.man10slotmachine.slot.data

import org.bukkit.configuration.ConfigurationSection

class SoundData(configSection: ConfigurationSection){
    val sound=configSection.getString("sound")
    val volume=configSection.getDouble("volume",1.0).toFloat()
    val pitch=configSection.getDouble("pitch",1.0).toFloat()
}