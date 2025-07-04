package top.alazeprt.aqqbot.util

object AReflection {
    @Throws(IllegalAccessException::class)
    fun findFieldByType(instance: Any, type: String) : Any? {
        val clazz: Class<*> = instance.javaClass
        for (field in clazz.declaredFields) {
            if (!field.type.name.endsWith(type)) continue
            field.isAccessible = true
            return field[instance]
        }
        return null
    }
}