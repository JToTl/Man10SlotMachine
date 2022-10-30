package ltotj.minecraft.man10slotmachine.utilities.ValutManager

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class VaultManager(private val plugin: JavaPlugin){

    companion object{
        var economy:Economy?=null
    }

    init{
        setupEconomy()
    }


    private fun setupEconomy():Boolean{
        plugin.logger.info("setupEconomy")
        if(plugin.server.pluginManager.getPlugin("Vault")==null){
            plugin.logger.warning("Vault plugin is not installed")
            return false
        }
        val rsp=plugin.server.servicesManager.getRegistration(Economy::class.java)
        if(rsp==null){
            plugin.logger.warning(("Can't get vault service"))
            return false
        }
        economy =rsp.provider
        plugin.logger.info("Economy setup")
        return economy !=null
    }

    fun getYenString(money: Double):String{
        return String.format("%,.0f",money)
    }

    fun getBalance(uuid: UUID):Double{
        return economy!!.getBalance(Bukkit.getOfflinePlayer(uuid))
    }

    fun showBalance(uuid:UUID){
        val player=Bukkit.getOfflinePlayer(uuid).player?:return
        player.sendMessage("§eあなたの所持金は${getYenString(getBalance(uuid))}です")
    }

    fun hasMoney(player:Player,money:Double):Boolean{
        return hasMoney(player.uniqueId,money)
    }

    fun hasMoney(uuid:UUID,money:Double):Boolean{
        return getBalance(uuid)>=money
    }

    fun withdraw(player: Player, money:Double):Boolean{
        return withdraw(player.uniqueId,money)
    }

    fun withdraw(uuid:UUID,money:Double):Boolean{
        val p=Bukkit.getOfflinePlayer(uuid)
        if(!hasMoney(uuid,money)){
            p.player?.sendMessage("§4電子マネーが足りません")
            return false
        }
        val resp= economy!!.withdrawPlayer(p,money)
        if(resp.transactionSuccess()){
            if(p.isOnline){
                p.player?.sendMessage("§e${getYenString(money)}円支払いました")
            }
            return true
        }
        return false
    }

    fun deposit(player:Player,money:Double):Boolean{
        return deposit(player.uniqueId,money)
    }

    fun deposit(uuid:UUID,money:Double):Boolean{
        val p=Bukkit.getOfflinePlayer(uuid)
        val resp= economy!!.depositPlayer(p,money)
        if(resp.transactionSuccess()){
            if(p.isOnline){
                p.player?.sendMessage("§e${getYenString(money)}円受け取りました")
            }
            return true
        }
        return false
    }


}