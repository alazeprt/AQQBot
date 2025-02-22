package top.alazeprt.aqqbot.data

import me.regadpole.config.DatabaseSource
import taboolib.module.database.*
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBot
import java.io.File
import javax.sql.DataSource

class SQLiteProvider(plugin: AQQBot) : DatabaseDataProvider(plugin) {
    override lateinit var host: Host<*>
    override lateinit var table: Table<*, *>
    override lateinit var dataSource: DataSource

    override fun loadData(type: DataStorageType) {
        val host = HostSQLite(File(plugin.getDataFolder(), plugin.generalConfig.getString("storage.sqlite.file")?: "aqqbot.db"))
        val dataSourceFile = File(plugin.getDataFolder(), "datasource.yml")
        if (!dataSourceFile.exists()) {
            plugin.saveResource("datasource.yml", false)
        }
        val dataSourceConfig = YamlConfiguration.loadConfiguration(dataSourceFile)
        Database.settingsFile = DatabaseSource(dataSourceConfig)
        val dataSource by lazy { host.createDataSource() }
        table = Table("account_data", host) {
            add("userId") {
                type(ColumnTypeSQLite.INTEGER) {
                    options(ColumnOptionSQLite.PRIMARY_KEY)
                }
            }
            add("name") {
                type(ColumnTypeSQLite.TEXT) {
                    options(ColumnOptionSQLite.NOTNULL)
                }
            }
        }
        this.host = host
        this.dataSource = dataSource
        table.createTable(dataSource)
    }

    override fun getStorageType(): DataStorageType {
        return DataStorageType.SQLITE
    }

    override fun saveData(type: DataStorageType) {
        dataSource.connection.close()
    }
}