package top.alazeprt.aqqbot.data

enum class DataStorageType {
    MYSQL,
    SQLITE,
    FILE;

    companion object {
        fun valueOf(name: String?): DataStorageType {
            return when (name?.toLowerCase()) {
                "file" -> FILE
                "sqlite" -> SQLITE
                "mysql" -> MYSQL
                else -> FILE
            }
        }
    }
}