package ltotj.minecraft.man10slotmachine.utilities.MySQLManager

import ltotj.minecraft.man10jackpot.utilities.MySQLManager.MySQLResult
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.sql.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer
import java.util.logging.Level
import kotlin.text.StringBuilder


class MySQLManager(private val plugin: JavaPlugin) {

    private var connection: Connection? = null
    private var statement: Statement? = null
    private var insertQuery: InsertQuery?=null

    companion object{
        val executor: ExecutorService = Executors.newCachedThreadPool()
    }

    inner class InsertQuery(private val table:String){

        private val values=ArrayList<Pair<String,String>>()

        fun add(column: String,value:String): InsertQuery {
            values.add(Pair(column,"'${escapedString(value)}'"))
            return this
        }

        fun addInt(column: String,value:Int): InsertQuery {
            values.add(Pair(column,value.toString()))
            return this
        }

        fun addBoolean(column: String,value:Boolean): InsertQuery {
            values.add(Pair(column,value.toString()))
            return this
        }

        fun addLong(column: String,value: Long): InsertQuery {
            values.add(Pair(column,value.toString()))
            return this
        }

        fun addDouble(column: String,value:Double): InsertQuery {
            values.add(Pair(column,value.toString()))
            return this
        }

        fun execute():Boolean{
            val query = StringBuilder("insert into ").append(table).append("(")
            for(i in 0 until values.size){
                if(i!=0)query.append(",")
                query.append(values[i].first)
            }

            query.append(") values(")

            for(i in 0 until values.size){
                if(i!=0)query.append(",")
                query.append(values[i].second)
            }

            query.append(");")

            val returnBoolean=execute(query.toString())
            insertQuery = null
            return returnBoolean
        }

        fun asyncExecute(taskOnFail:Runnable?=null,taskOnSuccess:Runnable?=null) {
            executor.execute {
                if (this.execute()) {
                    if (taskOnSuccess != null) {
                        plugin.server.scheduler.runTask(plugin, taskOnSuccess)
                    }
                } else {
                    if (taskOnFail != null) {
                        plugin.server.scheduler.runTask(plugin, taskOnFail)
                    }
                }
            }
        }

    }

    private fun open() {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            this.connection = DriverManager.getConnection("jdbc:mysql://${plugin.config.getString("mysql.host")}:${plugin.config.getString("mysql.port").toString()}/${plugin.config.getString("mysql.db")}?useSSL=false", plugin.config.getString("mysql.user"), plugin.config.getString("mysql.pass"))
        } catch (var2: SQLException) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not connect to MySQL server, error code: " + var2.errorCode)
        } catch (var3: ClassNotFoundException) {
            Bukkit.getLogger().log(Level.SEVERE, "JDBC driver was not found in this machine.")
        }
    }

    fun execute(query: String):Boolean{
        open()
        if(this.connection==null){
            plugin.logger.info("failed to open MYSQL")
            return false
        }
        var ret=true
        try{
            this.statement= this.connection!!.createStatement()
            this.statement!!.execute(query)
        }catch (exception: SQLException){
            plugin.logger.info("Error executing statement: ${exception.errorCode}:${exception.localizedMessage}")
            plugin.logger.info(query)
            ret=false
        }

        close()
        return ret
    }

    fun asyncExecute(query: String,taskOnFail: Runnable?=null,taskOnSuccess: Runnable?=null){
        executor.execute {
            if (execute(query)) {
                if(taskOnSuccess!=null)
                plugin.server.scheduler.runTask(plugin,taskOnSuccess)
            }
            else{
                if(taskOnFail!=null){
                    plugin.server.scheduler.runTask(plugin,taskOnFail)
                }
            }
        }
    }

    fun close(){
        try{
            if(statement!=null){
                statement!!.close()
                statement=null
            }
            if(connection!=null){
                connection!!.close()
                connection=null
            }
        }catch (exception: SQLException){
            plugin.logger.info("Error occurred during closing SQL: ${exception.errorCode}:${exception.localizedMessage}")
        }
    }

    fun rawQuery(query: String?): ResultSet? {
        open()
        var rs: ResultSet? = null
        if (connection == null) {
            Bukkit.getLogger().info("failed to open MYSQL")
            return rs
        }
        try {
            statement = connection!!.createStatement()
            rs =statement!!.executeQuery(query)
        } catch (var4: SQLException) {
            plugin.logger.info("Error executing query: " + var4.errorCode)
            plugin.logger.info(query)
        }
        return rs
    }

    fun select(columns: Array<String>,query: String): MySQLResult {
        val r=rawQuery(query)
        val result= MySQLResult(r,columns)
        close()
        return result
    }

    fun select(column:String,table:String,conditions:String): MySQLResult {
        val r=rawQuery("select $column from $table $conditions;")
        val result= MySQLResult(r,arrayOf(column))
        close()
        return result
    }

    fun select(columns:Array<String>,table:String,conditions:String): MySQLResult {
        val query=StringBuilder("select ")
        for(i in columns.indices){
            query.append(columns[i])
            if(i!=columns.size-1){
                query.append(",")
            }
        }
        query.append(" from ").append(table).append(" ").append(conditions).append(";")
        return select(columns,query.toString())
    }

    fun asyncSelect(columns: Array<String>,query: String,task:Consumer<MySQLResult>?=null){
        executor.execute {
            task?.accept(select(columns, query))
        }
    }

    fun asyncSelect(column:String,table:String,conditions:String,task:Consumer<MySQLResult>?=null){
        executor.execute {
            task?.accept(select(column, table, conditions))
        }
    }

    fun asyncSelect(columns:Array<String>,table:String,conditions:String,task:Consumer<MySQLResult>?=null){
        executor.execute {
            task?.accept(select(columns, table, conditions))
        }
    }

    fun getInsertQuery(table:String): InsertQuery {
        insertQuery=InsertQuery(table)
        return insertQuery!!
    }

    fun escapedString(str:String):String{
        return str
                .replace("\\", "\\\\")
                .replace("\b","\\b")
                .replace("\n","\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\\x1A", "\\Z")
                .replace("\\x00", "\\0")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
    }

}