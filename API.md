# 远程 WebSocket 服务器 API 文档 (Webhook)

## 鉴权

注意: 所有操作均需鉴权

在 Header 中添加 `Authorization` 字段，值为 `Bearer <token>`，其中 `<token>` 为服务器颁发的访问令牌

## 其它说明

- 你可以在请求数据中添加 `echo`, 在返回时会附带相同的 `echo` 参数值
- 你需要将请求的接口放到请求的数据内 (键为 `action`)
- 所有接口请求/返回值均为 `JSON` 格式

## 接口

### /api/v1/server/uuid

请求参数: 无

返回参数:
- `uuid` (`string`): 服务器 UUID

返回结构:

```json
{
  "uuid": "00000000-0000-0000-000000000000"
}
```

获取服务器的唯一标识符 (UUID)

### /api/v1/server/name

请求参数: 无

返回参数:
- `name` (`string`): 服务器名称

返回结构:
```json
{
  "name": "Server1"
}
```

获取服务器的名称

### /api/v1/server/name/set

请求参数:
- `name` (`string`): 新的服务器名称

返回参数:
- `status` (`string`): 操作状态 (`success` 或 `failed`)
- `message` (`string`): 操作信息

返回结构:
```json
{
  "status": "success",
  "message": "操作成功!"
}
```

设置服务器的名称

### /api/v1/server/info

请求参数: 无

返回参数:
- `brand_name` (`string`): 服务端名称
- `version` (`string`): 服务器版本号

返回结构:
```json
{
  "brand_name": "Leaf",
  "version": "1.21.4"
}
```

获取服务器的一些版本信息

### /api/v1/server/players

请求参数: 无

返回参数:
- `players` (`list<string>`): 服务器玩家列表

返回结构:
```json
{
  "players": ["alazeprt", "mc506lw"]
}
```

获取服务器的玩家列表

### /api/v1/server/player

请求参数:
- `player` (`string`): 玩家名称

返回参数:
- `uuid` (`string`): 玩家 UUID
- `name` (`string`): 玩家名称
- `online` (`boolean`): 玩家是否在线
- `ip` (`string`): 若玩家在线, 玩家的 IP 地址

返回结构:
```json
{
  "uuid": "00000000-0000-0000-000000000000",
  "name": "alazeprt",
  "online": true,
  "ip": "127.0.0.1"
}
```

获取特定玩家的基本信息

### /api/v1/plugin/info

请求参数: 无

返回参数:
- `version` (`string`): 插件版本号
- `latest_version` (`string`): 插件最新版本号
- `latest_commit` (`string`): 插件最新提交号
- `config_version` (`string`): 插件配置文件版本
- `plugin_config_version` (`string`): 应要更新到的配置文件版本 (插件内包含的插件配置文件版本)
- `latest_config_version` (`string`): GitHub 上最新的配置文件版本

返回结构:
```json
{
  "version": "2.0-beta.8",
  "latest_version": "2.0-beta.10",
  "latest_commit": "10082ba66c8ed4e363b500fd8484fe5e097fc76d",
  "config_version": 17,
  "plugin_config_version": 18,
  "latest_config_version": 18
}
```

获取插件的基本信息

### /api/v1/onebot/info

请求参数: 无

返回参数:
- `host` (`string`): OneBot 后端服务器地址
- `port` (`int`): OneBot 后端服务器端口
- `access_token` (`string`): OneBot 后端服务器的访问令牌 (Token)

返回结构:
```json
{
  "host": "localhost",
  "port": 3001,
  "access_token": ""
}
```

### /api/v1/onebot/set

请求参数:
- `host` (`string`): OneBot 后端服务器地址
- `port` (`int`): OneBot 后端服务器端口
- `access_token` (`string`): OneBot 后端服务器的访问令牌 (Token)

返回参数:
- `status` (`string`): 操作状态 (`success` 或 `failed`)
- `message` (`string`): 操作信息

返回结构:
```json
{
  "status": "success",
  "message": "操作成功!"
}
```

### /api/v1/onebot/status

请求参数: 无

返回参数:
- `connected` (`boolean`): OneBot 后端是否已连接

返回结构:
```json
{
  "connected": true
}
```

### /api/v1/enable_groups

请求参数: 无

返回参数:
- `enable_groups` (`list<string>`): 启用的群号列表

返回结构:
```json
{
  "enable_groups": ["43295681", "395025691"]
}
```

获取当前启用的所有群组的列表

### /api/v1/enable_groups/add

请求参数:
- `group_id` (`string`): 群组 ID

返回参数:
- `status` (`string`): 操作状态 (`success` 或 `failed`)
- `message` (`string`): 操作信息

返回结构:
```json
{
  "status": "failed",
  "message": "该群组已存在!"
}
```

添加一个启用的群组

### /api/v1/enable_groups/remove

请求参数:
- `group_id` (`string`): 群组 ID

返回参数:
- `status` (`string`): 操作状态 (`success` 或 `failed`)
- `message` (`string`): 操作信息

返回结构:
```json
{
  "status": "success",
  "message": "操作成功!"
}
```

移除一个启用的群组

### /api/v1/enable_groups/users

请求参数:
- `group_id` (`string`): 群组 ID

返回参数:
- `$qq` (`string`): 对应的 QQ 号及其群昵称

返回结构:
```json
{
  "431876428": "[服主] alazeprt"
}
```

获取某群组内的所有成员

### /api/v1/users

请求参数: 
- `qq` (`string` 或 `list<string>`): 查询的 QQ 号 (可选)

返回参数:
- `$qq` (`list<string>`): 每个用户所绑定的账号

返回结构:
```json
{
  "431876428": ["alazeprt", "alazelucas"],
  "72150385": ["test1"]
}
```

获取所有用户/特定用户的 QQ 绑定记录

### /api/v1/users/bind

请求参数:
- `qq` (`string`): 绑定的 QQ 号
- `player` (`string`): 绑定的游戏名

返回参数:
- `status` (`string`): 操作状态 (`success` 或 `failed`)
- `message` (`string`): 操作信息

返回结构:
```json
{
  "status": "success",
  "message": "操作成功!"
}
```

### /api/v1/users/unbind

请求参数 (至少填一项):
- `qq` (`string`): 绑定的 QQ 号
- `player` (`string`): 绑定的游戏名

返回参数:
- `status` (`string`): 操作状态 (`success` 或 `failed`)
- `message` (`string`): 操作信息

返回结构:
```json
{
  "status": "success",
  "message": "操作成功!"
}
```

### /api/v1/command/execute

请求参数:
- `command` (`string`): 要执行的命令

返回参数:
- `result` (`string`): 命令执行结果

返回结构:
```json
{
  "result": "[AQQBot] 插件已重载"
}
```

远程执行服务器命令 (返回结果已经进行颜色符号格式化)

### /api/v1/config/get

请求参数:
- `key` (`string`): 要获取的配置项的键名
- `groupId` (`string`): 某个群的子配置 (可选参数)

返回参数:
- `value` (`any`): 配置项的值

返回结构:
```json
{
  "value": true
}
```

### /api/v1/config/set

请求参数:
- `key` (`string`): 要设置的配置项的键名
- `value` (`any`): 要设置的配置项的值
- `groupId` (`string`): 要设置在的群的子配置 (可选参数)

返回参数:
- `status` (`string`): 操作状态 (`success` 或 `failed`)
- `message` (`string`): 操作信息

返回结构:
```json
{
  "status": "success",
  "message": "操作成功!"
}
```

### /api/v1/custom/get

请求参数:
- `key` (`string`): 要获取的自定义配置项的键名

返回参数:
- `enable` (`boolean`): 是否启用该自定义配置项
- `command` (`list<string>`): 调用该自定义配置项的命令列表
- `execute` (`list<string>`): 绑定账户的玩家触发该自定义配置项所执行的命令
- `unbind_execute` (`list<string>`): 未绑定账户的玩家触发该自定义配置项所执行的命令
- `choose_account` (`int`): 应该使用用户的哪一个账户执行命令
- `output` (`list<string>`): 绑定账户的玩家触发自定义配置项的返回信息
- `unbind_output` (`list<string>`): 未绑定账户的玩家触发该自定义配置项所返回的信息
- `format` (`boolean`): 是否格式化返回信息

返回结构:
```json
{
  "enable": true,
  "command": ["/test", "test"],
  "execute": ["say 你好"],
  "unbind_execute": ["say 你还没有绑定账号"],
  "choose_account": 1,
  "output": ["Test!"],
  "unbind_output": ["你还没有绑定账号"],
  "format": true
}
```

获取自定义配置文件中特定的配置项

### /api/v1/custom/set

请求参数:
- `key` (`string`): 要设置的自定义配置项的键名
- `enable` (`boolean`): 是否启用该自定义配置项
- `command` (`list<string>`): 调用该自定义配置项的命令列表
- `execute` (`list<string>`): 绑定账户的玩家触发该自定义配置项所执行的命令
- `unbind_execute` (`list<string>`): 未绑定账户的玩家触发该自定义配置项所执行的命令
- `choose_account` (`int`): 应该使用用户的哪一个账户执行命令
- `output` (`list<string>`): 绑定账户的玩家触发自定义配置项的返回信息
- `unbind_output` (`list<string>`): 未绑定账户的玩家触发该自定义配置项所返回的信息
- `format` (`boolean`): 是否格式化返回信息

返回参数:
- `status` (`string`): 操作状态 (`success` 或 `failed`)
- `message` (`string`): 操作信息

返回结构:
```json
{
  "status": "success",
  "message": "操作成功!"
}
```

修改自定义配置文件中特定的配置项

