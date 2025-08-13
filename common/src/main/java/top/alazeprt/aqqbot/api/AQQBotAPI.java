package top.alazeprt.aqqbot.api;

import top.alazeprt.aconfiguration.file.FileConfiguration;
import top.alazeprt.aqqbot.AQQBot;
import top.alazeprt.aqqbot.adapter.AQQBotAdapter;
import top.alazeprt.aqqbot.api.event.APIEvent;
import top.alazeprt.aqqbot.api.event.SubscribeAQQBotEvent;
import top.alazeprt.aqqbot.config.MessageManager;
import top.alazeprt.aqqbot.util.GroupConfiguration;

import java.lang.reflect.Method;
import java.util.*;

public class AQQBotAPI {
    private static AQQBot instance;
    private static Map<Method, Object> eventMap = new HashMap<>();

    public static void setInstance(AQQBot instance) {
        AQQBotAPI.instance = instance;
    }

    public static AQQBot getInstance() {
        return instance;
    }

    public static void registerEvent(Object clazz) {
        for (Method method : clazz.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(SubscribeAQQBotEvent.class)) {
                System.out.println(1);
                eventMap.put(method, clazz);
            }
        }
    }

    public static void unregisterEvent(Object clazz) {
        for (Method method : clazz.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(SubscribeAQQBotEvent.class)) {
                eventMap.remove(method);
            }
        }
    }

    public static void fireEvent(APIEvent event) {
        for (Map.Entry<Method, Object> entry : eventMap.entrySet()) {
            try {
                if (entry.getKey().getParameterCount() == 0) {
                    entry.getKey().invoke(entry.getValue());
                } else if (entry.getKey().getParameterCount() == 1 && entry.getKey().getParameterTypes()[0].equals(event.getClass())) {
                    entry.getKey().invoke(entry.getValue(), event);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to fire event", e);
            }
        }
    }

    public static AQQBotAdapter getAdapter() {
        return instance.getAdapter();
    }

    public static GroupConfiguration getGeneralConfig() {
        return instance.getGeneralConfig();
    }

    public static FileConfiguration getMessageConfig() {
        return instance.getMessageConfig();
    }

    public static FileConfiguration getBotConfig() {
        return instance.getBotConfig();
    }

    public static MessageManager getMessageManager() {
        return instance.getMessageManager();
    }
}
