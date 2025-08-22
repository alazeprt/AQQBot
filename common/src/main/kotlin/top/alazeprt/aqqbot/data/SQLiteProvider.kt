package top.alazeprt.aqqbot.data

import com.alessiodp.libby.Library
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
        val sqliteLib = Library.builder()
            .groupId("org{}xerial")
            .artifactId("sqlite-jdbc")
            .version("3.49.0.0")
            .resolveTransitiveDependencies(true)
            .build()
        plugin.libraryManager.loadLibraries(hikaricpLib, guavaLib, sqliteLib, databaseLib)
    }

    override fun loadData(type: DataStorageType) {
        loadDataDependencies()
        val host = HostSQLite(File(plugin.getDataFolder(), plugin.generalConfig.getString("storage.sqlite.file", null)))
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

    override fun saveData(type: Int) {
        dataSource.connection.close()
    }
}