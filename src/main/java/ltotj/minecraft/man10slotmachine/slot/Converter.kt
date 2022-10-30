package ltotj.minecraft.man10slotmachine.slot

import ltotj.minecraft.man10slotmachine.Main
import ltotj.minecraft.man10slotmachine.Main.Companion.pluginTitle
import ltotj.minecraft.man10slotmachine.utilities.ConfigManager.ConfigManager
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.LinkedHashMap


object Converter {

    var running=AtomicBoolean(false)

    fun convert(sender:CommandSender){

        if(running.get())return
        running.set(true)

        Thread {

            val oldSlotFiles =
                File("${Main.plugin.dataFolder.absolutePath}${File.separator}old_slots").listFiles() ?: return@Thread

            for (file in oldSlotFiles) {
                val fileName = file.name.split(".")
                if (fileName.size < 2 || fileName[1] != "yml") continue

                val oldConfig = YamlConfiguration.loadConfiguration(file)

                for (key in oldConfig.getKeys(false)) {

                    if (Main.slots.containsKey(key)) continue

                    val newConfig = ConfigManager(Main.plugin, key, "slots")

                    newConfig.setValue(
                        "${key}.general_setting.spin_setting",
                        oldConfig.get("${key}.spin_setting")
                    )

                    newConfig.setValue(
                        "${key}.general_setting.slot_name",
                        oldConfig.getString("${key}.general_setting.slot_name")
                    )
                    newConfig.setValue("${key}.general_setting.price", oldConfig.get("${key}.general_setting.price"))
                    newConfig.setValue("${key}.general_setting.wait", oldConfig.getInt("${key}.general_setting.wait"))
                    for (i in 1..3) {
                        newConfig.setValue(
                            "${key}.general_setting.items_reel${i}",
                            oldConfig.getString("${key}.general_setting.items_reel${i}")
                        )
                    }
                    newConfig.setValue("${key}.general_setting.sleep", oldConfig.getInt("${key}.general_setting.sleep"))
                    newConfig.setValue(
                        "${key}.general_setting.pre_stock",
                        oldConfig.get("${key}.general_setting.prestock")
                    )
                    newConfig.setValue("${key}.general_setting.stock", oldConfig.get("${key}.general_setting.stock"))
                    newConfig.setValue(
                        "${key}.general_setting.spinning_sound",
                        oldConfig.get("${key}.general_setting.spining_sound")
                    )

                    newConfig.setValue(
                        "${key}.general_setting.spin_setting",
                        oldConfig.get("${key}.general_setting.spin_setting")
                    )
                    newConfig.setValue(
                        "${key}.general_setting.stop_setting",
                        oldConfig.get("${key}.general_setting.stop_setting")
                    )
                    newConfig.setValue("${key}.general_setting.general_table", "table_name1")


                    val winKeys = oldConfig.getConfigurationSection("${key}.wining_setting")?.getKeys(false)

                    if (winKeys != null) {
                        var freezes = 1
                        for (win in winKeys) {
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.win_name",
                                oldConfig.get("${key}.wining_setting.${win}.name")
                            )
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.item",
                                oldConfig.get("${key}.wining_setting.${win}.item")
                            )
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.prize",
                                oldConfig.get("${key}.wining_setting.${win}.prize")
                            )
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.pay_stock",
                                oldConfig.get("${key}.wining_setting.${win}.stock")
                            )
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.reset_stock",
                                oldConfig.get("${key}.wining_setting.${win}.countreset")
                            )
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.coin_drop",
                                oldConfig.get("${key}.wining_setting.${win}.coindrop")
                            )
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.win_sound",
                                oldConfig.get("${key}.wining_setting.${win}.winsound")
                            )
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.win_particle",
                                oldConfig.get("${key}.wining_setting.${win}.win_particle")
                            )
                            newConfig.setValue("${key}.winning_setting.${win}.win_particle.chance", null)
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.command",
                                oldConfig.get("${key}.wining_setting.${win}.command")
                            )
                            newConfig.setValue(
                                "${key}.winning_setting.${win}.level",
                                oldConfig.get("${key}.wining_setting.${win}.level")
                            )

                            val ctList = oldConfig.getStringList("${key}.wining_setting.${win}.changetable")
                            val newList = ArrayList<String>()
                            for (str in ctList) {
                                val list2 = str.split("-")
                                newList.add("table_name${list2[0].toInt() + 1}-${list2[1]}-${list2[2]}")
                            }
                            newConfig.setValue("${key}.winning_setting.${win}.change_table", newList)

                            val chance = oldConfig.getDoubleList("${key}.wining_setting.${win}.chance.0")
                            for (i in 0 until chance.size) {
                                if (chance[i] < 0.00000001) continue
                                val list = newConfig.getStringList("${key}.table_setting.table_name${i + 1}.winnings")
                                list.add("${win}-${BigDecimal.valueOf(chance[i]).toPlainString()}")
                                newConfig.setValue("${key}.table_setting.table_name${i + 1}.winnings", list)
                            }

                            if (oldConfig.get("${key}.wining_setting.${win}.freeze") != null) {
                                newConfig.setValue(
                                    "${key}.freeze_setting.freeze_name${freezes}.step",
                                    oldConfig.get("${key}.wining_setting.${win}.freeze.step")
                                )
                                newConfig.setValue(
                                    "${key}.freeze_setting.freeze_name${freezes}.chance",
                                    oldConfig.get("${key}.wining_setting.${win}.freeze.chance")
                                )
                                newConfig.setValue(
                                    "${key}.freeze_setting.freeze_name${freezes}.predict_sound",
                                    oldConfig.get("${key}.wining_setting.${win}.freeze.predict_sound")
                                )
                                newConfig.setValue(
                                    "${key}.freeze_setting.freeze_name${freezes}.sleep",
                                    oldConfig.get("${key}.wining_setting.${win}.freeze.sleep")
                                )
                                for (i in 1 until 3) {
                                    newConfig.setValue(
                                        "${key}.freeze_setting.freeze_name${freezes}.stop${i}_sound",
                                        oldConfig.get("${key}.wining_setting.${win}.freeze.stop${i}_sound")
                                    )
                                }
                                freezes += 1
                            }
                        }

                    }

                    val tableList = newConfig.getConfigurationSection("${key}.table_setting")?.getKeys(false)
                    if (tableList != null) {
                        for (table in tableList) {
                            val winnings = newConfig.getStringList("${key}.table_setting.${table}.winnings")
                            val map = LinkedHashMap<String, Double>()
                            for (str in winnings) {
                                map[str] = str.split("-")[1].toDouble()
                            }
                            val newMap = map.toList().sortedBy { it.second }.toMap()
                            val newWinnings = ArrayList<String>()
                            for (str in newMap.keys) {
                                newWinnings.add(str)
                            }
                            newConfig.setValue("${key}.table_setting.${table}.winnings", newWinnings)
                        }
                    }

                    newConfig.save()
                }
                Main.loadSlotConfig()
                Main.loadDataConfig()
                Main.mSlotCommand.reloadOnTabComplete()
            }
            running.set(false)
            sender.sendMessage("${pluginTitle}§c変換が完了しました")
        }.start()
    }

}