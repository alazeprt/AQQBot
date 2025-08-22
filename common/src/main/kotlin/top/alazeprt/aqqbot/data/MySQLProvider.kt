package top.alazeprt.aqqbot.data

import com.alessiodp.libby.Library
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

    override fun loadDataDependencies() {
        val databaseLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("taboolib-database")
            .version("1.0.4")
            .relocate("com{}google{}common", "top{}alazeprt{}aqqbot{}lib{}com{}google{}common")
            .build()
        val hikaricpLib = Library.builder()
            .groupId("com{}zaxxer")
            .artifactId("HikariCP")
            .version("4.0.3")
            .resolveTransitiveDependencies(true)
            .build()
        val guavaLib = Library.builder()
            .groupId("com{}google{}guava")
            .artifactId("guava")
            .version("21.0")
            .relocate("com{}google{}common", "top{}alazeprt{}aqqbot{}lib{}com{}google{}common")
            .resolveTransitiveDependencies(true)
            .build()
        val mysqlLib = Library.builder()
            .groupId("com{}mysql")
            .artifactId("mysql-connector-j")
            .version("8.3.0")
            .resolveTransitiveDependencies(true)
            .build()
        plugin.libraryManager.loadLibraries(hikaricpLib, guavaLib, mysqlLib, databaseLib)
    }

    override fun loadData(type: DataStorageType) {
        loadDataDependencies()
        val config = plugin.generalConfig
        val host = HostSQL(config.getString("storage.mysql.host", null),
            config.getInt("storage.mysql.port", null).toString(),
            config.getString("storage.mysql.user", null),
            config.getString("storage.mysql.password", null),
            config.getString("storage.mysql.database", null),)
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
                type(ColumnTypeSQL.VARCHAR, 128) {
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

    override fun saveData(type: Int) {
        dataSource.connection.close()
    }
}