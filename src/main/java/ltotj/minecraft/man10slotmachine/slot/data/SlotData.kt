package ltotj.minecraft.man10slotmachine.slot.data

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.utilities.ItemManager.ItemStackPlus
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class SlotData(val innerSlotName:String,config:ConfigurationSection) {

    val slotName=config.getString("general_setting.slot_name")
    val price=config.getDouble("general_setting.price")
    val wait=config.getInt("general_setting.wait",2000)
    val nextReelWait=config.getInt("general_setting.next_reel_wait",wait/10)
    val reels=HashMap<Int,ArrayList<ItemStack>>()
    var priceItem:PriceItem?=null
    val sleep=config.getLong("general_setting.sleep",100L)
    val preStock=config.getDouble("general_setting.pre_stock",0.0)
    val stock=config.getDouble("general_setting.stock",0.0)
    val alone=config.getBoolean("general_setting.alone",true)
    val permission=config.getString("general_setting.permission",null)
    var spinningSound:SoundData?=null
    var spinSetting:SettingData?=null
    var stopSetting:SettingData?=null
    lateinit var generalTable: TableData

    val tables=HashMap<String,TableData>()
    val winnings=HashMap<String,WinningData>()
    val freezes=HashMap<String,FreezeData>()

    val allowedWinningPattern=HashMap<WinningData,ArrayList<String>>()
    val winningPattern=ArrayList<String>()

    var losingPattern:String?=null


    inner class PriceItem(val material: Material,val amount:Int,val cmd:Int,val name:String?)

    init {

        //リールのアイテム読み込み
        for(i in 1..3){
            val items=config.getString("general_setting.items_reel${i}")!!.split(",")
            reels[i]= ArrayList()
            for(str in items){
                val list=str.split("-")
                if(list.size==1){
                    reels[i]!!.add(ItemStackPlus(Material.valueOf(list[0].toUpperCase()), 1))
                }
                else {
                    reels[i]!!.add(
                        ItemStackPlus(Material.valueOf(list[0].toUpperCase()), 1).setCustomModelData(
                            list[1].toIntOrNull() ?: 0
                        )
                    )
                }
            }
        }

        //回しはじめの設定読み込み
        if(config.get("general_setting.spin_setting")!=null){
            spinSetting= SettingData(config.getStringList("general_setting.spin_setting.command")
                , config.getConfigurationSection("general_setting.spin_setting.sound")?.let { SoundData(it) }
                , config.getConfigurationSection("general_setting.spin_setting.particle")?.let { ParticleData(it) })
        }

        //止まるときの設定読み込み
        if(config.get("general_setting.stop_setting")!=null){
            spinSetting= SettingData(config.getStringList("general_setting.stop_setting.command")
                , config.getConfigurationSection("general_setting.stop_setting.sound")?.let { SoundData(it) }
                , config.getConfigurationSection("general_setting.stop_setting.particle")?.let { ParticleData(it) })
        }

        //回すときに消費するアイテム読み込み
        if(config.get("general_setting.price_item")!=null){
            priceItem=PriceItem(Material.valueOf(config.getString("general_setting.price_item.material","BARRIER")!!),config.getInt("general_setting.price_item.amount",1)
                ,config.getInt("general_setting.price_item.cmd",Integer.MIN_VALUE),config.getString("general_setting.price_item.name",null))
        }

        //回転するときの音読み込み
        if(config.get("general_setting.spinning_sound")!=null){
            spinningSound=SoundData(config.getConfigurationSection("general_setting.spinning_sound")!!)
        }


        if(config.getConfigurationSection("freeze_setting")!=null){
            //フリーズ設定読み込み(当たり役の前に読み込まないとバグる)
            for(key in config.getConfigurationSection("freeze_setting")!!.getKeys(false)){
                freezes[key]= FreezeData(key,config.getConfigurationSection("freeze_setting.${key}")!!)
            }
        }

        if(config.getConfigurationSection("winning_setting")!=null) {
            //当たり役読み込み
            for (key in config.getConfigurationSection("winning_setting")!!.getKeys(false)) {
                winnings[key]= WinningData(key,config.getConfigurationSection("winning_setting.${key}")!!,this)
            }
        }
        if(config.getConfigurationSection("table_setting")!=null){
            //テーブル読み込み(当たり役の後に読み込む)
            for (key in config.getConfigurationSection("table_setting")!!.getKeys(false)) {
                tables[key]= TableData(key,config.getConfigurationSection("table_setting.${key}")!!,this)
            }
        }

        //当たりの並び読み込み

        //複数の当たりになってしまう出方を除外
        val duplicatedPattern=ArrayList<String>()
        val baseWinningPattern=ArrayList<String>()
        for(win in winnings.values){
            allowedWinningPattern[win]= win.winningPattern.clone() as ArrayList<String>
            //並びが完全に同じ役は重複扱いにならないようにする
            if(baseWinningPattern.contains(win.winningPattern[0])){
                continue
            }
            baseWinningPattern.add(win.winningPattern[0])
            for(str in allowedWinningPattern[win]!!){
                if(winningPattern.contains(str)){
                    duplicatedPattern.add(str)
                }
                else{
                    winningPattern.add(str)
                }
            }
        }
        for(win in allowedWinningPattern.keys){
            allowedWinningPattern[win]!!.removeAll(duplicatedPattern.toSet())
            if(allowedWinningPattern[win]!!.isEmpty()){
                println("${Main.pluginTitle}§4${innerSlotName}の${win.innerWinName}に有効な当たりが存在しません")
            }
        }

        //外れの出方を一つだけ保存しておく
        var r1=java.util.Random().nextInt(reels[1]!!.size)+1
        var r2=java.util.Random().nextInt(reels[2]!!.size)+1
        var r3=java.util.Random().nextInt(reels[3]!!.size)+1

        for(i in 0 until reels[1]!!.size){
            for(j in 0 until reels[2]!!.size){
                for(k in 0 until reels[3]!!.size){
                    val str="${r1},${r2},${r3}"
                    if(!winningPattern.contains(str)){
                        losingPattern=str
                        break
                    }
                    r1=(r1%reels[1]!!.size)+1
                }
                if(losingPattern!=null){
                    break
                }
                r2=(r2%reels[1]!!.size)+1
            }
            if(losingPattern!=null){
                break
            }
            r3=(r3%reels[1]!!.size)+1
        }


        //基礎テーブル読み込み
        generalTable=tables[config.getString("general_setting.general_table")]?:tables.values.first()

        //当たり役の移行テーブル設定読み込み
        for(winning in winnings.values){
            winning.loadChangeTable()
        }
    }



}