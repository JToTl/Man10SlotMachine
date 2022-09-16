package ltotj.minecraft.man10slotmachine.slot.data

import org.bukkit.Particle
import org.bukkit.configuration.ConfigurationSection

class ParticleData(configSection: ConfigurationSection){
    var particle:Particle?=null
    val count=configSection.getInt("count",1)
    val speed=configSection.getInt("speed",0)

    init {
        if(configSection.getString("particle")!=null){
            particle=Particle.valueOf(configSection.getString("particle")!!)
        }
    }
}