package ltotj.minecraft.man10slotmachine.slot

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.slot.data.SlotData
import ltotj.minecraft.man10slotmachine.slot.data.WinningData
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

class Simulator{

    companion object{
        val onSimulating=AtomicBoolean(false)
    }

    inner class WinLog(var times: Long,var prize:Double){

        var percentage=0.0

        fun calculatePercentage(times:Long){
            percentage=(this.times.toDouble()/times.toDouble())*100
        }

        fun add(prize:Double){
            times+=1
            this.prize+=prize
        }
    }

    fun simulate(sender:CommandSender, slot: SlotData, times:Long){
        if(onSimulating.get()){
            sender.sendMessage("${Main.pluginTitle}§aシミュレーション中です")
            return
        }
        onSimulating.set(true)
        Thread{
            var stock=slot.preStock
            var table=slot.generalTable
            var remainingTableCount=0
            var totalOutMoney=0.0
            var totalWins=0L
            val totalInMoney=slot.price*times
            val winCounter=HashMap<WinningData, WinLog>()
            for(win in slot.winnings.values){
                winCounter[win]=WinLog(0L,0.0)
            }

            for(i in 0 until times){
                stock+=if(table.stock==Double.MIN_VALUE)slot.stock else table.stock

                remainingTableCount= max(0,remainingTableCount-1)
                val win=if(remainingTableCount==0&&table.endWinning!=null)table.endWinning!! else table.getWinning()
                val nextTable=win?.getChangeTable()
                if(win!=null){
                    totalWins+=1L
                    totalOutMoney+=stock*win.payStock+win.prize
                    winCounter[win]!!.add(stock*win.payStock+win.prize)
                    if(win.resetStock)stock=slot.preStock
                    stock+=win.addStock
                    remainingTableCount+=win.addGameCount
                }

                if(nextTable!=null){
                    table=nextTable.table!!
                    remainingTableCount=nextTable.count
                }
                else if(remainingTableCount==0&&table!=slot.generalTable){
                    table=slot.generalTable
                }
            }

            for(log in winCounter.values){
                log.calculatePercentage(times)
            }

            sender.sendMessage("${Main.pluginTitle}§e====${slot.slotName}§eのシミュレーション結果§e====")
            sender.sendMessage("")
            sender.sendMessage("当たり名 / 当たり回数 / 当たり率 / 排出額")
            for(win in winCounter.keys){
                sender.sendMessage("§a${win.innerWinName}§a / ${winCounter[win]!!.times}回 / ${doubleToStr(winCounter[win]!!.percentage)}% / ${String.format("%,.0f",winCounter[win]!!.prize)}")
            }
            sender.sendMessage("")
            sender.sendMessage("§c総回転数：$times")
            sender.sendMessage("§c総当たり数：$totalWins")
            sender.sendMessage("§c総投入額：${String.format("%,.0f",totalInMoney)}")
            sender.sendMessage("§c総排出額：${String.format("%,.0f",totalOutMoney)}")
            if(totalInMoney>0.0){
                sender.sendMessage("§c還元率：§e${doubleToStr(100*totalOutMoney/totalInMoney)}%")
            }
            sender.sendMessage("${Main.pluginTitle}§e====================================")


            onSimulating.set(false)
        }.start()

    }

    private fun doubleToStr(double: Double):String{
        return BigDecimal.valueOf(double).toPlainString()
    }



}