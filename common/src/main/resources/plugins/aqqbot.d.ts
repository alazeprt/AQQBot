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

declare class GroupMessageEvent {
    getSenderNickname(): string

    getSenderId(): number

    getMessage(): string

    getMessageId(): number

    getGroupId(): number

    getTime(): number

    getSelfId(): number

    getFont(): number
}

declare interface Cancelable {
    cancel(): void;
}

declare class ReceiveMessageEvent implements APIEvent {
    readonly event: GroupMessageEvent;
}

declare class PreBindEvent implements Cancelable, APIEvent {
    readonly groupId: number;
    readonly operatorId: number;
    readonly userId: number;
    readonly playerName: string;

    cancel(): void;
}

declare class PreUnbindEvent implements Cancelable, APIEvent {
    readonly groupId: number;
    readonly operatorId: number;
    readonly userId: number;
    readonly playerName: string;

    cancel(): void;
}

declare class PreInformationEvent implements Cancelable, APIEvent {
    readonly groupId: number;
    readonly userId: number;

    getType(): string;
    cancel(): void;
}

declare class PreRemoteCommandEvent implements Cancelable, APIEvent {
    readonly groupId: number;
    readonly senderId: number;
    readonly command: string;

    cancel(): void;
}

declare class PostBindEvent implements APIEvent {
    readonly groupId: number;
    readonly operatorId: number;
    readonly userId: number;
    readonly playerName: string;
    readonly isCanceled: boolean;

    getReasonMsg(): string;
}

declare class PostUnbindEvent implements APIEvent {
    readonly groupId: number;
    readonly operatorId: number;
    readonly userId: number;
    readonly playerName: string;
    readonly isCanceled: boolean;

    getReasonMsg(): string;
}

declare class PostInformationEvent implements APIEvent {
    readonly groupId: number;
    readonly userId: number;
    readonly isCanceled: boolean;

    getType(): string;
    getReasonMsg(): string;
}

declare class PostRemoteCommandEvent implements APIEvent {
    readonly groupId: number;
    readonly senderId: number;
    readonly command: string;
    readonly isCanceled: boolean;

    getReasonMsg(): string;
}

/**
 * AQQBot 插件对象
 */
declare const plugin: AQQBot

/**
 * AQQBot 事件管理器
 */
declare const eventManager: EventManager