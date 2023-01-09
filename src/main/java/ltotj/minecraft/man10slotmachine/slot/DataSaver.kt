package ltotj.minecraft.man10slotmachine.slot

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.slot.data.SlotData
import ltotj.minecraft.man10slotmachine.slot.data.WinningData
import ltotj.minecraft.man10slotmachine.utilities.MySQLManager.MySQLManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object DataSaver {


    private var mysql=MySQLManager(Main.plugin)
    private val queries=ArrayList<MySQLManager.InsertQuery>()
    var mysqlThread:Thread?=null
    var saturation=false
    var isRunning=false

    fun addSpinData(player: Player,fileName:String,slotName:String,priceItem:SlotData.PriceItem?,win: WinningData?,pay:Double,payBack:Double,tableName:String,tableCount:Int,date: Date){
        val query=mysql.getFreeInsertQuery("slot_record")
            .add("mcid",player.name)
            .add("uuid",player.uniqueId.toString())
            .add("slot_name",slotName)
            .add("slot_file",fileName)
            .addDouble("inmoney",pay)
            .addDouble("outmoney",payBack)
            .add("table_name",tableName)
            .addInt("table_count",tableCount)
            .add("date", SimpleDateFormat("yyyy-MM-dd HHH:mm:ss").format(date))

        if(win!=null){
            query.add("win_name",win.innerWinName?:"null")
                .addInt("win_level",win.level)
        }
        if(priceItem!=null){
            if(priceItem.name==null){
                query.add("in_item",priceItem.material.toString())
            }
            else{
                query.add("in_item",priceItem.name)
            }
        }
        queries.add(query)
    }

    fun fStart(){
        mysqlThread?.interrupt()
        mysqlThread=Thread{

            mysql.execute("CREATE TABLE IF NOT EXISTS slot_record(\n" +
                    "\n" +
                    "   id int unsigned auto_increment,\n" +
                    "   mcid varchar(16),\n" +
                    "   uuid varchar(36),\n" +
                    "   slot_name varchar(64),\n" +
                    "   slot_file varchar(64),\n" +
                    "   win_name varchar(64),\n" +
                    "   win_level int default -1,\n" +
                    "   inmoney double,\n" +
                    "   in_item varchar(128) null,\n" +
                    "   outmoney double default 0,\n" +
                    "   table_name varchar(64),\n" +
                    "   table_count int default -1,\n" +
                    "   date DATETIME,\n" +
                    "\n" +
                    "   primary key(id)\n" +
                    "\n" +
                    ")")

            saveSpinData()
        }
        isRunning=true
        mysqlThread!!.start()
    }

    fun start():Boolean{
        if(isRunning)return false
        mysql=MySQLManager(Main.plugin)
        mysqlThread?.interrupt()
        mysqlThread=Thread{

            mysql.execute("CREATE TABLE IF NOT EXISTS slot_record(\n" +
                    "\n" +
                    "   id int unsigned auto_increment,\n" +
                    "   mcid varchar(16),\n" +
                    "   uuid varchar(36),\n" +
                    "   slot_name varchar(64),\n" +
                    "   slot_file varchar(64),\n" +
                    "   win_name varchar(64),\n" +
                    "   win_level int default -1,\n" +
                    "   inmoney double,\n" +
                    "   in_item varchar(128) null,\n" +
                    "   outmoney double default 0,\n" +
                    "   table_name varchar(64),\n" +
                    "   table_count int default -1,\n" +
                    "   date DATETIME,\n" +
                    "\n" +
                    "   primary key(id)\n" +
                    "\n" +
                    ")")

            saveSpinData()
        }
        isRunning=true
        mysqlThread!!.start()
        return true
    }

    private fun saveSpinData(){
        val jobs=ArrayList<MySQLManager.InsertQuery>()
        var fail=false
        while (true){
            if(queries.size>300){
                saturation=true
                Main.enable.set(false)
            }
            if(saturation&& queries.size==0){
                saturation=false
                Main.enable.set(true)
            }
            jobs.clear()
            jobs.addAll(queries)
            for (query in jobs) {
                if (query.execute()) {
                    queries.remove(query)
                } else {
                    fail = true
                }
            }
            if (fail) {
                break
            }
            Thread.sleep(5000)
        }
        isRunning=false
        println("${Main.pluginTitle}§4データ保存でエラーが発生しました")
        Bukkit.getScheduler().runTaskLater(Main.plugin,
            Runnable {
                fail=false
                start()
                println("${Main.pluginTitle}§4データセーバーを再稼働しました")
        },100)

    }

}