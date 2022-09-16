package ltotj.minecraft.man10slotmachine.command

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.Main.Companion.placedSlots
import ltotj.minecraft.man10slotmachine.Main.Companion.slots
import ltotj.minecraft.man10slotmachine.event.CreateChairEvent
import ltotj.minecraft.man10slotmachine.event.CreateSignEvent
import ltotj.minecraft.man10slotmachine.event.CreateSlotEvent
import ltotj.minecraft.man10slotmachine.slot.Converter
import ltotj.minecraft.man10slotmachine.slot.data.MadeSlotData
import ltotj.minecraft.man10slotmachine.slot.Simulator
import ltotj.minecraft.man10slotmachine.utilities.CommandManager.CommandArgumentType
import ltotj.minecraft.man10slotmachine.utilities.CommandManager.CommandManager
import ltotj.minecraft.man10slotmachine.utilities.CommandManager.CommandObject
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class MSlotCommand(plugin: JavaPlugin,pluginTitle:String) : CommandManager(plugin,"mslot", pluginTitle) {

    private val createObject=CommandObject(slots.keys,"スロット名")
        .addNextArgument(
            CommandObject(CommandArgumentType.STRING)
                .setComment("スロット識別名(任意の文字列)")
                .setExplanation("新規にスロットを設置します")
                .setFunction{
                    val sender=it.first
                    if(sender is Player){
                        if(placedSlots.containsKey(it.second[2])){
                            sender.sendMessage("${pluginTitle}§4その識別名のスロットは既に設置されています")
                            return@setFunction
                        }
                        if(!slots.containsKey(it.second[1])){
                            sender.sendMessage("${pluginTitle}§4${it.second[1]}は存在しません")
                            return@setFunction
                        }
                        CreateSlotEvent.frameMaker[sender.uniqueId]=MadeSlotData(it.second[2],it.second[1],sender)
                    }
                }
        )

    private val removeObject=CommandObject(placedSlots.keys,"スロット識別名")
        .setExplanation("スロットを撤去します")
        .setFunction{
            val sender=it.first
            if(!placedSlots.containsKey(it.second[1])) {
                sender.sendMessage("${pluginTitle}§4${it.second[1]}は存在しません")
                return@setFunction
            }
            placedSlots[it.second[1]]!!.delete()
            sender.sendMessage("${pluginTitle}§a${it.second[1]}を削除しました")
        }

    private val simulateObject=CommandObject(slots.keys,"スロット名")
        .addNextArgument(
            CommandObject(CommandArgumentType.Long)
                .setExplanation("スロットのシミュレーションを行います")
                .setComment("回転数")
                .setFunction{
                    val sender=it.first
                    if(!slots.containsKey(it.second[1])) {
                        sender.sendMessage("${pluginTitle}§4${it.second[1]}は存在しません")
                        return@setFunction
                    }
                    Simulator()
                        .simulate(sender,slots[it.second[1]]!!,it.second[2].toLong())
                }
        )

    private val flagObject=CommandObject(placedSlots.keys,"スロット名")
        .addNextArgument(
            CommandObject(CommandArgumentType.STRING)
                .setExplanation("スロットのフラグを設定します")
                .setComment("フラグ名")
                .setFunction{
                    val sender=it.first
                    if(!placedSlots.containsKey(it.second[1])) {
                        sender.sendMessage("${pluginTitle}§4${it.second[1]}は存在しません")
                        return@setFunction
                    }
                    if(placedSlots[it.second[1]]!!.slotData==null){
                        sender.sendMessage("${pluginTitle}§4スロットデータが空です")
                        return@setFunction
                    }
                    if(!placedSlots[it.second[1]]!!.slotData!!.winnings.containsKey(it.second[2])){
                        sender.sendMessage("${pluginTitle}§4${it.second[2]}は存在しません")
                        return@setFunction
                    }
                    sender.sendMessage("${pluginTitle}§a${it.second[2]}をセットしました")
                    placedSlots[it.second[1]]!!.setSlotFlag(it.second[2])
                }
        )

    private val tableObject=CommandObject(placedSlots.keys,"スロット名")
        .addNextArgument(
            CommandObject(CommandArgumentType.STRING)
                .setComment("テーブル名")
                .addNextArgument(
                    CommandObject(CommandArgumentType.INT)
                        .setComment("ゲーム数")
                        .setExplanation("現在のテーブルを設定します")
                        .setFunction{
                            val sender=it.first as Player
                            if(!placedSlots.containsKey(it.second[1])) {
                                sender.sendMessage("${pluginTitle}§4${it.second[1]}は存在しません")
                                return@setFunction
                            }
                            placedSlots[it.second[1]]!!.setSlotTable(sender,it.second[1],it.second[2].toInt())
                        }
                )
        )

    private val stockObject=CommandObject(placedSlots.keys,"スロット名")
        .addNextArgument(
            CommandObject(CommandArgumentType.DOUBLE)
                .setComment("ストック額")
                .setExplanation("現在のストックを設定します")
                .setFunction{
                    val sender=it.first as Player
                    if(!placedSlots.containsKey(it.second[1])) {
                        sender.sendMessage("${pluginTitle}§4${it.second[1]}は存在しません")
                        return@setFunction
                    }
                    val stock=it.second[2].toDouble()
                    if(stock<0){
                        sender.sendMessage("${pluginTitle}§4ストックは0未満に設定できません")
                        return@setFunction
                    }
                    placedSlots[it.second[1]]!!.setSlotStock(stock)
                    sender.sendMessage("${pluginTitle}§aストックを${stock}に設定しました")
                }
        )

    private val addChairObject=CommandObject(placedSlots.keys,"スロット名")
        .setOnlyPlayer(true)
        .setFunction{
            val sender=it.first as Player
            if(!placedSlots.containsKey(it.second[2])) {
                sender.sendMessage("${pluginTitle}§4${it.second[2]}は存在しません")
                return@setFunction
            }
            sender.sendMessage("${pluginTitle}§a椅子にしたいブロックを壊してください")
            CreateChairEvent.chairMaker[sender.uniqueId]=placedSlots[it.second[2]]!!
        }

    private val removeChairObject=CommandObject(placedSlots.keys,"スロット名")
        .setOnlyPlayer(true)
        .setFunction{
            val sender=it.first as Player
            if(!placedSlots.containsKey(it.second[2])) {
                sender.sendMessage("${pluginTitle}§4${it.second[2]}は存在しません")
                return@setFunction
            }
            placedSlots[it.second[2]]!!.removeChair(sender)
        }

    private val addSignObject=CommandObject(placedSlots.keys,"スロット名")
        .setOnlyPlayer(true)
        .setFunction{
            val sender=it.first as Player
            if(!placedSlots.containsKey(it.second[2])) {
                sender.sendMessage("${pluginTitle}§4${it.second[2]}は存在しません")
                return@setFunction
            }
            CreateSignEvent.signMaker[sender.uniqueId]= placedSlots[it.second[2]]!!
            sender.sendMessage("${pluginTitle}§a看板を設置してください")
        }

    private val removeSignObject=CommandObject(placedSlots.keys,"スロット名")
        .setOnlyPlayer(true)
        .setFunction{
            val sender=it.first as Player
            if(!placedSlots.containsKey(it.second[2])) {
                sender.sendMessage("${pluginTitle}§4${it.second[2]}は存在しません")
                return@setFunction
            }
            placedSlots[it.second[2]]!!.removeSign(sender)
        }

    fun reloadOnTabComplete(){
        createObject.setArguments(slots.keys)
        removeObject.setArguments(placedSlots.keys)
        simulateObject.setArguments(slots.keys)
        flagObject.setArguments(placedSlots.keys)
        addChairObject.setArguments(placedSlots.keys)
        removeChairObject.setArguments(placedSlots.keys)
        stockObject.setArguments(placedSlots.keys)
        tableObject.setArguments(placedSlots.keys)
    }


    init {
        setPermission("mslot.admin")

        addFirstArgument(
            CommandObject("on")
                .setExplanation("スロットを起動します")
                .setFunction{}
        )

        addFirstArgument(
            CommandObject("off")
                .setExplanation("スロットを停止します")
                .setFunction{}
        )

        addFirstArgument(
            CommandObject("reload")
                .setExplanation("コンフィグをリロードします")
                .setFunction{

                    Main.loadConfig()
                    Main.loadSlotConfig()
                    Main.loadDataConfig()
                    reloadOnTabComplete()
                    it.first.sendMessage("${Main.pluginTitle}§aリロード完了")

                }
        )

        addFirstArgument(
            CommandObject("create")
                .addNextArgument(
                    createObject
                )
        )

        addFirstArgument(
            CommandObject("remove")
                .addNextArgument(
                    removeObject
                )
        )

        addFirstArgument(
            CommandObject("simulate")
                .addNextArgument(
                    simulateObject
                )
        )

        addFirstArgument(
            CommandObject("flag")
                .addNextArgument(
                    flagObject
                )
        )

        addFirstArgument(
            CommandObject("chair")
                .addNextArgument(
                    CommandObject("add")
                        .addNextArgument(
                            addChairObject
                        )
                )
                .addNextArgument(
                    CommandObject("remove")
                        .addNextArgument(
                            removeChairObject
                        )
                )
                .setExplanation("椅子の設置/撤去を行います")
        )

        addFirstArgument(
            CommandObject("save")
                .setExplanation("データをコンフィグにセーブします")
                .setFunction{}
        )

        addFirstArgument(
            CommandObject("stock")
                .addNextArgument(
                    stockObject
                )
        )

        addFirstArgument(
            CommandObject("table")
                .addNextArgument(
                    tableObject
                )
        )

        addFirstArgument(
            CommandObject("sign")
                .addNextArgument(
                    CommandObject("add")
                        .addNextArgument(
                            addSignObject
                        )
                )
                .addNextArgument(
                    CommandObject("remove")
                        .addNextArgument(
                            removeSignObject
                        )
                )
                .setExplanation("ストック看板の設置/撤去を行います")
        )

        addFirstArgument(
            CommandObject("list")
                .setExplanation("設置可能なスロット一覧を表示します")
                .setFunction{
                    it.first.sendMessage("${pluginTitle}§eスロット一覧")
                    for(str in slots.keys){
                        it.first.sendMessage("§6$str")
                    }
                }
        )

        addFirstArgument(
            CommandObject("convert")
                .setExplanation("old_slotsのファイルを新しい形式に変換します")
                .setFunction{
                    Converter.convert()
                    it.first.sendMessage("${pluginTitle}§c変換が完了しました")
                }
        )


    }


}