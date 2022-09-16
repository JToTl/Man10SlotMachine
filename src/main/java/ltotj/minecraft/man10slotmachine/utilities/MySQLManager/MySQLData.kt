package ltotj.minecraft.man10slotmachine.utilities.MySQLManager

import java.math.BigDecimal
import java.sql.ResultSet

class MySQLData(result: ResultSet, column: Array<String>){

    private val resultData=HashMap<String, Any>()

    init{
        for(key in column){
            resultData[key]=result.getObject(key)
        }
    }

    fun getString(column: String):String{
        return resultData[column].toString()
    }

    fun getBoolean(column: String):Boolean{
        return resultData[column] as Boolean
    }

    fun getInt(column: String):Int{
        val obj=resultData[column]
        if(obj is BigDecimal){
            return obj.intValueExact()
        }
        return obj.toString().toIntOrNull()?:-1
    }


    fun getDouble(column: String):Double{
        return resultData[column].toString().toDoubleOrNull()?:-1.0
    }

    fun getLong(column: String):Long{
        return resultData[column].toString().toLongOrNull()?:-1
    }

    fun getKeys():MutableSet<String>{
        return resultData.keys
    }
}