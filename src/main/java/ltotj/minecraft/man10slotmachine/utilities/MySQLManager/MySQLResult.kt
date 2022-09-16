package ltotj.minecraft.man10jackpot.utilities.MySQLManager

import ltotj.minecraft.man10slotmachine.utilities.MySQLManager.MySQLData
import java.sql.ResultSet

class MySQLResult(result:ResultSet?,column:Array<String>){

    private val result=ArrayList<MySQLData>()
    private var row=-1

    init {
        if(result!=null) {
            while (result.next()) {
                this.result.add(MySQLData(result,column))
            }
            result.close()
        }
    }

    fun setRow(row:Int){
        this.row=row
    }

    fun next():Boolean{
        return if(row<result.size-1){
            row++
            true
        }
        else {
            false
        }
    }

    fun back():Boolean{
        return if(row>0){
            row--
            true
        }
        else{
            false
        }
    }

    fun row():Int{
        return row
    }

    fun size():Int{
        return result.size
    }

    fun execute(action: (MySQLData) -> Unit){
        action(result[row])
    }

    fun execute(row:Int,action: (MySQLData) -> Unit){
        action(result[row])
    }

    fun forEach( action:(MySQLData) -> Unit){
        for(mySQLData in result){
            action(mySQLData)
        }
    }

}