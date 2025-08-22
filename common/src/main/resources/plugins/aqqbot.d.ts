declare interface AQQBot {
    /**
     * 输出日志
     * @param level 日志等级 (0 代表 trace, 1 代表 debug, 2 代表 info, 3 代表 warn, 4 代表 error, 5 代表 fatal)
     * @param message 日志内容
     */
    log(level: number, message: string): void;

    /**
     * 获取服务端名称
     */
    getBrandName(): string;

    /**
     * 获取服务端版本号
     */
    getServerVersion(): string;

    /**
     * 判断在白名单数据中是否有玩家
     * @param name 玩家名称
     */
    hasPlayer(name: string): boolean;

    /**
     * 判断在白名单数据中是否有 QQ 号
     * @param qq QQ 号
     */
    hasQQ(qq: number): boolean;

    /**
     * 添加白名单数据
     * @param qq QQ 号
     * @param name 玩家名称
     */
    addPlayer(qq: number, name: string): void;

    /**
     * 移除白名单数据
     * @param name 玩家名称
     */
    removePlayer(name: string): void;

    /**
     * 移除白名单数据
     * @param qq QQ 号
     * @param name 玩家名称
     */
    removePlayer(qq: number, name: string): void;

    /**
     * 移除白名单数据
     * @param qq QQ 号
     */
    removePlayer(qq: number): void;

    /**
     * 获取玩家绑定的 QQ 号
     * @param name 玩家名称
     */
    getQQByPlayer(name: string): number;

    /**
     * 获取绑定的所有玩家名称
     * @param qq QQ 号
     */
    getPlayerNameByQQ(qq: number): string[];
}

declare interface EventManager {
    /**
     * 注册事件
     * @param name 事件名称, 可选: ReceiveMessageEvent, PreBindEvent, PostBindEvent, PreUnbindEvent, PostUnbindEvent, PreInformationEvent, PostInformationEvent, PreRemoteCommandEvent, PostRemoteCommandEvent
     * @param callback 事件触发时的回调方法
     */
    register(name: string, callback: (event: APIEvent) => void): void;

    /**
     * 注销事件
     * @param name 事件名称, 可选: ReceiveMessageEvent, PreBindEvent, PostBindEvent, PreUnbindEvent, PostUnbindEvent, PreInformationEvent, PostInformationEvent, PreRemoteCommandEvent, PostRemoteCommandEvent
     * @param callback 事件触发时的回调方法
     */
    unregister(name: string, callback: Function): void;
}

declare interface APIEvent {

}

/**
 * 群消息事件 (AOneBot)
 */
declare class GroupMessageEvent {
    /**
     * 发送者群昵称
     */
    getSenderNickname(): string

    /**
     * 发送者 QQ 号
     */
    getSenderId(): number

    /**
     * 发送的消息
     */
    getMessage(): string

    getMessageId(): number

    /**
     * 发送的消息所在的群号
     */
    getGroupId(): number

    getTime(): number

    getSelfId(): number

    getFont(): number
}

declare class BotManager {
    /**
     * 发送群消息
     * @param groupId 群号
     * @param message 消息内容
     */
    sendGroupMessage(groupId: number, message: string): void;

    /**
     * 发送私聊消息
     * @param userId QQ 号
     * @param message 消息内容
     */
    sendPrivateMessage(userId: number, message: string): void;
}

declare interface Cancelable {
    cancel(): void;
}

/**
 * QQ 群收到消息事件
 */
declare class ReceiveMessageEvent implements APIEvent {
    readonly event: GroupMessageEvent;
}

/**
 * QQ 群绑定处理前事件
 */
declare class PreBindEvent implements Cancelable, APIEvent {
    readonly groupId: number;
    readonly operatorId: number;
    readonly userId: number;
    readonly playerName: string;

    cancel(): void;
}

/**
 * QQ 群解绑处理前事件
 */
declare class PreUnbindEvent implements Cancelable, APIEvent {
    readonly groupId: number;
    readonly operatorId: number;
    readonly userId: number;
    readonly playerName: string;

    cancel(): void;
}

/**
 * QQ 群获取服务器信息处理前事件
 */
declare class PreInformationEvent implements Cancelable, APIEvent {
    readonly groupId: number;
    readonly userId: number;

    getType(): string;
    cancel(): void;
}

/**
 * QQ 群远程执行命令处理前事件
 */
declare class PreRemoteCommandEvent implements Cancelable, APIEvent {
    readonly groupId: number;
    readonly senderId: number;
    readonly command: string;

    cancel(): void;
}

/**
 * QQ 群绑定处理后事件
 */
declare class PostBindEvent implements APIEvent {
    readonly groupId: number;
    readonly operatorId: number;
    readonly userId: number;
    readonly playerName: string;
    readonly isCanceled: boolean;

    getReasonMsg(): string;
}

/**
 * QQ 群解绑处理后事件
 */
declare class PostUnbindEvent implements APIEvent {
    readonly groupId: number;
    readonly operatorId: number;
    readonly userId: number;
    readonly playerName: string;
    readonly isCanceled: boolean;

    getReasonMsg(): string;
}

/**
 * QQ 群获取服务器信息处理后事件
 */
declare class PostInformationEvent implements APIEvent {
    readonly groupId: number;
    readonly userId: number;
    readonly isCanceled: boolean;

    getType(): string;
    getReasonMsg(): string;
}

/**
 * QQ 群远程执行命令处理后事件
 */
declare class PostRemoteCommandEvent implements APIEvent {
    readonly groupId: number;
    readonly senderId: number;
    readonly command: string;
    readonly isCanceled: boolean;

    getReasonMsg(): string;
}

/**
 * 玩家加入服务器处理前事件
 */
declare class PrePlayerJoinEvent implements Cancelable, APIEvent {
    readonly name: string;
    readonly userId: number;

    cancel(): void;
    cancel(reason: string): void;
}

/**
 * 玩家加入服务器处理后事件
 */
declare class PostPlayerJoinEvent implements APIEvent {
    readonly name: string;
    readonly userId: number;
    readonly isCanceled: boolean;

    getReasonMsg(): string;
}

/**
 * 玩家退出服务器事件
 */
declare class PlayerQuitEvent implements APIEvent {
    readonly name: string;
    readonly userId: number;
}

/**
 * 玩家聊天处理前事件
 */
declare class PrePlayerChatEvent implements APIEvent {
    readonly name: string;
    readonly userId: number;
    readonly message: string;

    cancel(): void;
}

/**
 * 玩家聊天处理后事件
 */
declare class PostPlayerChatEvent implements APIEvent {
    readonly name: string;
    readonly userId: number;
    readonly message: string;
    readonly isCanceled: boolean;

    getReasonMsg(): string;
}

/**
 * 玩家死亡事件
 */
declare class PlayerDeathEvent implements APIEvent {
    readonly name: string;
    readonly userId: number;
    readonly reason: string;
}

/**
 * 插件启动时触发的事件
 */
declare class PluginStartEvent implements APIEvent {

}

/**
 * 插件停止时触发的事件
 */
declare class PluginStopEvent implements APIEvent {

}

/**
 * 收到申请入群时触发的事件
 */
declare class GroupRequestEvent implements APIEvent {
    readonly selfId: number;
    readonly groupId: number;
    readonly userId: number;
    readonly comment: string;
    readonly isInvite: boolean;
}

/**
 * 新成员加入群时触发的事件
 */
declare class GroupMemberIncreaseEvent implements APIEvent {
    readonly selfId: number;
    readonly groupId: number;
    readonly userId: number;
    readonly operatorId: number;
}

/**
 * 成员退出群时触发的事件
 */
declare class GroupMemberDecreaseEvent implements APIEvent {
    readonly selfId: number;
    readonly groupId: number;
    readonly userId: number;
    readonly operatorId: number;
}

/**
 * AQQBot 插件对象
 */
declare const plugin: AQQBot

/**
 * AQQBot 事件管理器
 */
declare const eventManager: EventManager

/**
 * AQQBot 机器人管理器
 */
declare const botManager: BotManager