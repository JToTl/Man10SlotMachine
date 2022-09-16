package ltotj.minecraft.man10slotmachine.slot

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.Main.Companion.plugin
import ltotj.minecraft.man10slotmachine.Main.Companion.pluginTitle
import ltotj.minecraft.man10slotmachine.slot.data.*
import ltotj.minecraft.man10slotmachine.utilities.ConfigManager.ConfigManager
import ltotj.minecraft.man10slotmachine.utilities.ItemManager.ItemStackPlus
import ltotj.minecraft.man10slotmachine.utilities.ValutManager.VaultManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.lang.Integer.max
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

class Man10Slot {

    private lateinit var configManager:ConfigManager
    var slotData:SlotData?=null
    var spinner:Player?=null
    private var stock=0.0
    var remainingTableCount=0
    private lateinit var table:TableData
    private val reels=HashMap<Int,Reel>()
    var chair:Chair?=null
    private val executor= Executors.newCachedThreadPool()
    var isSpinning=AtomicBoolean(false)
    var sign:SlotSign?=null
    lateinit var dataName:String
    lateinit var leverLocation:Location

    var flag:String?=null

    companion object{
        val vault=VaultManager(plugin)

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //innerクラス
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inner class SlotSign{

        var sign:Sign?=null
        var location: Location?=null


        //既存のもの読み込み
        constructor(){
            val list=configManager.getString("sign_location")?.split(",")?:return
            val world=Bukkit.getWorld(list[0])?:return
            location=Location(world,list[1].toDouble(),list[2].toDouble(),list[3].toDouble())
            val block=location!!.block.state
            if(block !is Sign)return
            sign=block
            updateSign()
        }


        //新規設置
        constructor(sign: Sign){
            this.sign=sign
            this.location=sign.location.toCenterLocation()
            configManager.setValue("sign_location","${location!!.world.name},${location!!.x},${location!!.y},${location!!.z}")
            updateSign()
        }

    }


    inner class Reel(private val column:Int){

        val itemFrames=ArrayList<ItemFrame?>()
        private val reelItems= slotData!!.reels[column]?: arrayListOf(ItemStack(Material.BARRIER,1))
        var currentItemNum=1 //一番上の段

        fun loadItemFrames(){
            val list=configManager.getStringList("item_frame_location.reel${column}")
            itemFrames.clear()
            for(str in list){
                val frameList=str.split(",")
                val world=Bukkit.getWorld(frameList[0])
                if(world==null){
                    itemFrames.add(null)
                }
                else{
                    val entities=Location(world,frameList[1].toDouble(),frameList[2].toDouble(),frameList[3].toDouble())
                        .toCenterLocation()
                        .getNearbyEntitiesByType(ItemFrame::class.java,0.5)
                    if(entities.isEmpty()){
                        itemFrames.add(null)
                    }
                    else{
                        itemFrames.add(entities.random())
                    }
                }
            }
        }

        fun removeItems(){
            for(frame in itemFrames){
                frame?.setItem(ItemStack(Material.AIR,1))
            }
        }

        fun next(){
            currentItemNum=(currentItemNum%reelItems.size)+1
            for(i in 0 until itemFrames.size){
                itemFrames[i]?.setItem(reelItems[(currentItemNum-1+i*reelItems.size-i)%reelItems.size])
            }
        }
    }

    inner class Chair{
        var location: Location?=null

        //既存の椅子を読み込む
        constructor(){
            val list=configManager.getString("chair_location")?.split(",")?:return
            val world=Bukkit.getWorld(list[0])?:return
            location= Location(world,list[1].toDouble(),list[2].toDouble(),list[3].toDouble())
        }

        //新規に設置する
        constructor(location: Location){
            this.location=location.toCenterLocation()

            configManager.setValue("chair_location","${this.location!!.world.name},${this.location!!.x},${this.location!!.y},${this.location!!.z}")
        }

        fun getSeatedPlayer():Player?{
            location?.getNearbyPlayers(0.1)?.forEach {
                if(it.isInsideVehicle){
                    if(it.vehicle!!.type==EntityType.ARMOR_STAND){
                        return it
                    }
                }
            }
            return null
        }
    }

    //既存のスロットを読み込む
    constructor(configManager: ConfigManager) {
        this.configManager=configManager

        loadSlot()
    }

    //新規に設置する
    constructor(madeSlotData: MadeSlotData){

        //ファイルの作成.既に存在するかどうかの判定はこれより前のコマンドの方で行う
        configManager= ConfigManager(Main.plugin,madeSlotData.name,"data")

        //額縁の位置保存
        val locMap=HashMap<Int,ArrayList<String>>()
        for(i in 1 .. 3){
            locMap[i]= ArrayList()
        }
        for(i in 0 until madeSlotData.itemFrameLocation.size){
            val loc=madeSlotData.itemFrameLocation[i].toCenterLocation()
            locMap[(i%3)+1]!!.add("${loc.world.name},${loc.x},${loc.y},${loc.z}")
        }
        for (i in 1 .. 3){
            configManager.setValue("item_frame_location.reel${i}",locMap[i])
        }

        //レバーの位置保存
        configManager.setValue("lever_location","${madeSlotData.leverLocation.world.name},${madeSlotData.leverLocation.x}" +
                ",${madeSlotData.leverLocation.y},${madeSlotData.leverLocation.z}")

        //スロットの内部指定保存
        configManager.setValue("slot_name",madeSlotData.innerSlotName)
        configManager.saveConfig()

        loadSlot()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //外部から呼び出す関数
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun removeChair(player:Player){
        if(chair==null){
            player.sendMessage("${pluginTitle}§4椅子は設置されていません")
            return
        }
        chair=null
        configManager.setValue("chair_location",null)
        player.sendMessage("${pluginTitle}§a椅子を撤去しました")
    }

    fun removeSign(player: Player){
        if(sign==null){
            player.sendMessage("${pluginTitle}§4看板は設置されていません")
            return
        }
        sign=null
        configManager.setValue("sign_location",null)
        player.sendMessage("${pluginTitle}§a看板を撤去しました")
    }

    fun loadSlot(){
        //スロットのデータ読み込み
        dataName=configManager.filename.split(".")[0]

        Main.placedSlots[dataName]=this

        val loc=configManager.getString("lever_location")?.split(",")?:return

        val world=Bukkit.getWorld(loc[0])?:return

        leverLocation=Location(world,loc[1].toDouble(),loc[2].toDouble(),loc[3].toDouble())

        Main.levers[leverLocation]=this

        slotData=Main.slots[configManager.getString("slot_name")]
        if (slotData != null) {
            //リール読み込み
            for (i in slotData!!.reels.keys) {
                reels[i] = Reel(i)
                reels[i]!!.loadItemFrames()
            }
            //現在のテーブル読み込み
            table = slotData!!.tables[configManager.getString("table")] ?: slotData!!.generalTable

        }
        //ストック読み込み
        stock=configManager.getDouble("stock",0.0)

        //椅子の読み込み
        if(configManager.getString("chair_location","none")!="none"){
            chair=Chair()
        }

        //残りゲーム数読み込み
        remainingTableCount=configManager.getInt("remaining_table_count",0)
        sign=SlotSign()

    }

    fun createChair(location: Location){
        this.chair=Chair(location)
    }

    fun createSign(sign:Sign){
        this.sign=SlotSign(sign)
    }

    fun saveConfig(){
        configManager.setValue("stock",stock)
        configManager.setValue("table",table.innerTableName)
        configManager.setValue("remaining_table_count",remainingTableCount)
    }

    fun setSlotFlag(flag:String){
        this.flag=flag
    }

    fun setSlotTable(sender:CommandSender,table:String,gameCount:Int){
        if(slotData==null){
            sender.sendMessage("${pluginTitle}§4スロットのデータが空です")
            return
        }
        if(!slotData!!.tables.containsKey(table)){
            sender.sendMessage("${pluginTitle}§4${table}は存在しません")
            return
        }
        remainingTableCount=gameCount
        this.table=slotData!!.tables[table]!!
        saveConfig()
        sender.sendMessage("${pluginTitle}§aテーブルを§${table}(${gameCount}ゲーム)に設定しました")
    }

    fun setSlotStock(stock:Double){
        this.stock=stock
        saveConfig()
    }

    fun delete(){
        Main.placedSlots.remove(dataName)
        Main.levers.remove(leverLocation)
        configManager.delete()
    }

    fun updateSign(){
        if(sign?.sign==null)return
        sign!!.sign!!.line(0, Component.text("§9============"))
        sign!!.sign!!.line(1, Component.text("§0§l[§r${slotData?.slotName}§0§l]"))
        sign!!.sign!!.line(2, Component.text("§0§lstock:§a§l${stock}"))
        sign!!.sign!!.line(3, Component.text("§9============"))
        sign!!.sign!!.update()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //スロットの実行処理
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    fun start(player:Player){
        if(slotData==null){
            return
        }
        if(slotData!!.losingPattern==null){
            return
        }
        if(!Main.enable.get()){
            player.sendMessage("${pluginTitle}§4現在使用不可です")
            return
        }
        if(!player.hasPermission("mslot.use")){
            player.sendMessage("${pluginTitle}§4あなたはスロットを回す権限がありません")
            return
        }
        if(!player.hasPermission(slotData!!.permission?:"mslot.use")){
            player.sendMessage("${pluginTitle}§4このスロットを回す権限がありません")
            return
        }
        if(chair?.getSeatedPlayer()!=null&&chair!!.getSeatedPlayer()!=player){
            player.sendMessage("${pluginTitle}§4横回しはできません")
            return
        }
        if(isSpinning.get()){
            player.sendMessage("${pluginTitle}§c既に回っています")
            return
        }
        if(!Main.allowMultiSpin.get()&&Main.spinners.contains(player)){
            player.sendMessage("${pluginTitle}§4一度に複数のスロットを回すことはできません")
            return
        }
        if(!Main.allowSub.get()&&Main.spinnersAddress.contains(player.address.address.hostAddress)){
            player.sendMessage("${pluginTitle}§4サブアカウントを用いて複数のスロットを回すことはできません")
            return
        }

        //支払い処理
        if(!payToSpinSlot(player)){
            return
        }

        Main.spinners.add(player)
        Main.spinnersAddress.add(player.address.address.hostAddress)
        isSpinning.set(true)

        //ストックの追加
        stock += if(table.stock!=Double.MIN_VALUE){
            table.stock
        } else{
            slotData!!.stock
        }
        updateSign()

        executor.execute {

            //spin_settingの取得
            var spinSetting=slotData?.spinSetting
            //tableにspinSettingがあればそれを優先
            if(table.spinSetting?.soundData!=null){
                spinSetting=table.spinSetting
            }

            executeCommand(player,spinSetting?.command)
            playSound(player,spinSetting?.soundData)
            playEffects(player,spinSetting?.particleData)

            //spinning_soundの取得
            var spinningSound=slotData?.spinningSound
            //tableにspinningSoundがあればそれを優先
            if(table.spinningSound!=null){
                spinningSound=table.spinningSound
            }

            //間違っても0未満にならないようにmaxでとる
            remainingTableCount=max(0,remainingTableCount-1)

            //当たりの取得
            //フラグが設定されていれば役をそれにする
            var win:WinningData?
            if(flag!=null){
                win=slotData!!.winnings[flag]
                flag=null
            }
            else if(remainingTableCount==0&&table.endWinning!=null){
                win=table.endWinning!!
            }
            else{
                win=table.getWinning()
            }

            //リールの並び取得
            var line: String? =null
            if(win!=null){
                //当たりの場合、並びを許可されているものから取る.許可されたものがない場合外れとする
                if(slotData!!.allowedWinningPattern[win]!!.isEmpty()){
                    win=null
                }
                else {
                    line = slotData!!.allowedWinningPattern[win]!!.random()
                }
            }
            else{
                //外れの並び
                //1,2の列の出目をランダムで決め、それに対し外れになるように3つ目を決める
                val r1= Random.nextInt(slotData!!.reels[1]!!.size)+1
                val r2= Random.nextInt(slotData!!.reels[2]!!.size)+1
                var r3= Random.nextInt(slotData!!.reels[3]!!.size)+1
                for(i in 0 until slotData!!.reels[3]!!.size){
                    val str="${r1},${r2},${r3}"
                    if(!slotData!!.winningPattern.contains(str)){
                        line=str
                        break
                    }
                    r3=(r3%slotData!!.reels[3]!!.size)+1
                }
            }

            if(line==null){
                //許可された当たりの並びなし、または1,2列目に対して外れになるような3列目がない場合、予め用意された外れの並びにする
                line=slotData!!.losingPattern
            }

            //スロットの止まり方を、フリーズしたかどうかで分岐させる
            if(win?.isFreezing()?:false){
                for(i in 0 until slotData!!.wait*3/2){
                    Thread.sleep(slotData!!.sleep)
                    reels[1]!!.next()
                    reels[2]!!.next()
                    reels[3]!!.next()
                    playSound(player,spinningSound)
                }
                for(reel in reels.values){
                    reel.removeItems()
                }
                val freeze=win!!.freezeData!!
                playSound(player,freeze.predictSound)
                executeCommand(player,freeze.command)
                Thread.sleep(5000)
                //回転数の計算
                val itemsNumStr=line!!.split(",")
                val toReel1=freeze.step+(slotData!!.reels[1]!!.size-reels[1]!!.currentItemNum-freeze.step+itemsNumStr[0].toInt())%slotData!!.reels[1]!!.size+slotData!!.reels[1]!!.size
                val toReel2=slotData!!.reels[2]!!.size+(slotData!!.reels[2]!!.size-(1+(reels[2]!!.currentItemNum+toReel1-1)%slotData!!.reels[2]!!.size)-slotData!!.reels[2]!!.size+itemsNumStr[1].toInt())%slotData!!.reels[2]!!.size+slotData!!.reels[2]!!.size
                val toReel3=slotData!!.reels[3]!!.size+(slotData!!.reels[3]!!.size-(1+(reels[3]!!.currentItemNum+toReel1+toReel2-1)%slotData!!.reels[3]!!.size)-slotData!!.reels[3]!!.size+itemsNumStr[2].toInt())%slotData!!.reels[3]!!.size+slotData!!.reels[3]!!.size

                //回転
                for(i in 0 until toReel1){
                    Thread.sleep(freeze.sleep)
                    reels[1]!!.next()
                    reels[2]!!.next()
                    reels[3]!!.next()
                    playSound(player, spinningSound)
                }
                playSound(player,freeze.stopSounds[1])
                for(i in 0 until toReel2){
                    Thread.sleep(freeze.sleep)
                    reels[2]!!.next()
                    reels[3]!!.next()
                    playSound(player, spinningSound)
                }
                playSound(player,freeze.stopSounds[2])
                for(i in 0 until toReel3){
                    Thread.sleep(freeze.sleep)
                    reels[3]!!.next()
                    playSound(player, spinningSound)
                }
                playSound(player,freeze.stopSounds[3])

            }
            else {
                //回転数の計算
                //%sizeの後に+sizeしてさらに%sizeすれば全てのwait対応になる
                val itemsNumStr = line!!.split(",")
                val toReel1 =
                    slotData!!.wait + (slotData!!.reels[1]!!.size - reels[1]!!.currentItemNum - slotData!!.wait + itemsNumStr[0].toInt()) % slotData!!.reels[1]!!.size
                val toReel2 =
                    slotData!!.nextReelWait + (slotData!!.reels[2]!!.size - (1 + (reels[2]!!.currentItemNum + toReel1 - 1) % slotData!!.reels[2]!!.size) - slotData!!.nextReelWait + itemsNumStr[1].toInt()) % slotData!!.reels[2]!!.size
                val toReel3 =
                    slotData!!.nextReelWait + (slotData!!.reels[3]!!.size - (1 + (reels[3]!!.currentItemNum + toReel1 + toReel2 - 1) % slotData!!.reels[3]!!.size) - slotData!!.nextReelWait + itemsNumStr[2].toInt()) % slotData!!.reels[3]!!.size

                //回転
                for (i in 0 until toReel1) {
                    Thread.sleep(slotData!!.sleep)
                    reels[1]!!.next()
                    reels[2]!!.next()
                    reels[3]!!.next()
                    playSound(player, spinningSound)
                }
                for (i in 0 until toReel2) {
                    Thread.sleep(slotData!!.sleep)
                    reels[2]!!.next()
                    reels[3]!!.next()
                    playSound(player, spinningSound)
                }
                for (i in 0 until toReel3) {
                    Thread.sleep(slotData!!.sleep)
                    reels[3]!!.next()
                    playSound(player, spinningSound)
                }

            }

            val nextTable=win?.getChangeTable()
            var payBack=0.0
            //当たりの処理
            if(win!=null){
                val totalStock=stock*win.payStock
                payBack=totalStock+win.prize
                player.sendMessage("${pluginTitle}§e§lおめでとうございます！${win.winName}§e§lです！")
                coinDrop(win)
                for(com in win.command) {
                    plugin.server.scheduler.runTask(plugin,Runnable{
                        plugin.server.dispatchCommand(
                            Bukkit.getConsoleSender(), com.replace("<player>", player.name)
                                .replace("<stock>", totalStock.toString())
                                .replace("<prize>", win.prize.toString())
                                .replace("<win>", win.winName!!)
                                .replace("<totalPrize>", payBack.toString())
                                .replace("<slot>", slotData!!.slotName ?: slotData!!.innerSlotName)
                                .replace("&&", "<and>")
                                .replace("&", "§")
                                .replace("<and>", "&")
                        )
                    })
                }

                playSound(player,win.winSound)
                playEffects(player,win.particleData)

                //ストックの処理
                vault.deposit(player,payBack)
                if(win.resetStock){
                    stock=slotData!!.preStock
                }
                stock+=win.addStock
                plugin.server.scheduler.runTask(plugin,Runnable{updateSign()})

                //残りテーブル数処理
                remainingTableCount+=win.addGameCount
            }
            else{
                executeCommand(player,slotData!!.stopSetting?.command)
                playSound(player,slotData!!.stopSetting?.soundData)
                playEffects(player,slotData!!.stopSetting?.particleData)
                player.sendMessage("${pluginTitle}§c外れました")
            }

            //テーブルの移動処理
            if(nextTable!=null){
                //winによって移動先が存在する場合
                executeTableSetting(player,nextTable.table!!.startTableSetting)
                remainingTableCount=nextTable.count
                table=nextTable.table!!
            }
            else if(remainingTableCount<=0&&table!=slotData!!.generalTable){
                //ゲーム数0でgeneral_tableへ移動する場合
                executeTableSetting(player,table.endTableSetting)
                executeTableSetting(player,slotData!!.generalTable.startTableSetting)
                table=slotData!!.generalTable
            }


            if(Main.useDB) {
                DataSaver.addSpinData(player,configManager.filename,slotData!!.slotName?:"null",slotData!!.priceItem,win,slotData!!.price
                ,payBack,table.innerTableName,remainingTableCount, Date()
                )
            }
            saveConfig()
            Main.spinners.remove(player)
            Main.spinnersAddress.remove(player.address.address.hostAddress)
            isSpinning.set(false)
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //slot実行処理のみで用いる関数
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    private fun playSound(player:Player,sound:SoundData?){
        if(sound==null)return
        leverLocation.world.playSound(leverLocation,sound.sound?:"",sound.volume,sound.pitch)
    }

    private fun playEffects(player:Player,particle:ParticleData?){
        if(particle==null)return
        val loc=reels[2]?.itemFrames?.get((reels[2]?.itemFrames?.size)?.div(2) ?:0)?.location?:return
        particle.particle?.let { loc.world.spawnParticle(it,loc,particle.count) }
    }

    private fun executeCommand(player:Player,commands:MutableList<String>?){
        if(commands==null)return
        for(str in commands) {
            plugin.server.scheduler.runTask(plugin,Runnable{
                Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    str.replace("<player>", player.name)
                        .replace("<game>", remainingTableCount.toString())
                        .replace("<slot>", slotData!!.innerSlotName)
                        .replace("<stock>", stock.toString())
                )
            })
        }
    }

    private fun executeTableSetting(player:Player,setting:SettingData?){
        if(setting==null)return
        executeCommand(player,setting.command)
        playSound(player,setting.soundData)
        playEffects(player,setting.particleData)
    }


    private fun payToSpinSlot(player: Player):Boolean{
        if(slotData!!.price<=0&&slotData!!.priceItem!=null){
            val priceItem=slotData!!.priceItem!!
            val item=ItemStackPlus(player.inventory.itemInMainHand)
            return if(item.type == priceItem.material && item.amount >= priceItem.amount && (priceItem.cmd == Integer.MIN_VALUE || (item.itemMeta.hasCustomModelData()&&item.itemMeta.customModelData == priceItem.cmd)) && item.itemMeta.displayName == (priceItem.name
                    ?: item.itemMeta.displayName)){
                player.inventory.itemInMainHand.amount-=priceItem.amount
                true
            } else{
                player.sendMessage("${pluginTitle}§cこのスロットを回すには${priceItem.name?:"§e${priceItem.material}"}§cが§e${priceItem.amount}個§c必要です")
                false
            }
        }
        return vault.withdraw(player,slotData!!.price)
    }

    private fun coinDrop(win:WinningData){
        if(!win.coinDrop)return
        plugin.server.scheduler.runTask(plugin,Runnable {
            val item = win.coinDropItem ?: Main.coin
            val drops=ArrayList<Item>()
            for (i in 0 until if(slotData!!.alone)7 else 2) {
                for (reel in reels.values) {
                    for (frame in reel.itemFrames) {
                        val loc = frame?.location?.clone() ?: continue
                        val dif =
                            loc.direction.crossProduct(Vector(0, 1, 0)).normalize().multiply(0.5 - Random.nextDouble())
                                .add(Vector(0.0, (1 - Random.nextDouble()) * 0.5, 0.0)).add(loc.direction.clone().normalize().multiply(0.3))
                        loc.add(dif)

                        val drop = loc.world.dropItem(loc, item)
                        drop.setCanMobPickup(false)
                        drop.setCanPlayerPickup(false)
                        drop.velocity =
                            loc.direction.normalize().add(Vector(0.0, Random.nextDouble(), 0.0)).multiply(Random.nextDouble()*0.3)
                        drops.add(drop)
                    }
                }
            }

            plugin.server.scheduler.runTaskLater(plugin,Runnable{
                for(drop in drops){
                    drop.remove()
                }
            },60)
        })
    }




}