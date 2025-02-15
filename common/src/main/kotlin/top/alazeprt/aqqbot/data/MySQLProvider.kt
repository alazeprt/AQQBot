package top.alazeprt.aqqbot.data

import me.regadpole.config.DatabaseSource
import taboolib.module.database.*
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBot
import java.io.File
import javax.sql.DataSource

class MySQLProvider(plugin: AQQBot) : DatabaseDataProvider(plugin) {
    override lateinit var host: Host<*>
    override lateinit var table: Table<*, *>
    override lateinit var dataSource: DataSource

    override fun loadData(type: DataStorageType) {
        val config = plugin.generalConfig
        val host = HostSQL(config.getString("storage.mysql.host"),
            config.getInt("storage.mysql.port").toString(),
            config.getString("storage.mysql.user"),
            config.getString("storage.mysql.password"),
            config.getString("storage.mysql.database"))
        val dataSourceFile = File(plugin.getDataFolder(), "datasource.yml")
        if (!dataSourceFile.exists()) {
            plugin.saveResource("datasource.yml", false)
        }
        val dataSourceConfig = YamlConfiguration.loadConfiguration(dataSourceFile)
        Database.settingsFile = DatabaseSource(dataSourceConfig)
        val dataSource by lazy { host.createDataSource() }
        table = Table("account_data", host) {
            add("userId") {
                type(ColumnTypeSQL.BIGINT) {
                    options(ColumnOptionSQL.PRIMARY_KEY)
                }
            }
            add("name") {
                type(ColumnTypeSQL.VARCHAR) {
                    options(ColumnOptionSQL.NOTNULL)
                }
            }
        }
        this.host = host
        this.dataSource = dataSource
        table.createTable(dataSource)
    }

    override fun getStorageType(): DataStorageType {
        return DataStorageType.MYSQL
    }

    override fun saveData(type: DataStorageType) {
        dataSource.connection.close()
    }
}