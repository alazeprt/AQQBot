# AQQBot
ENGLISH | [简体中文](https://github.com/alazeprt/AQQBot/blob/master/README_zh.md)
## Description

AQQBot is a plugin for interconnecting QQ groups and servers based on the Onebot v11 protocol, which allows users to perform many operations through QQ groups (e.g., binding game accounts, querying server status).

## Function
- **Account Binding**: Players need to bind their accounts in the QQ group before they can enter the server
- **Information query**: Players can send specific commands in the QQ group to query the account's game status (e.g. walking distance), and also query the server status (e.g. server tps)
- **Chat interconnection**: messages sent by players in the QQ group will be forwarded to the server, and at the same time, messages sent by players in the server will be forwarded to the QQ group as well

## Features
- **Lightweight**: plugin size is only less than 600kb
- **Highly customizable**: you can freely switch on and off the various functions built into the plugin, just by editing the configuration file
- **Strong compatibility**: the plugin supports all servers based on Spigot/Paper and Velocity

## Installation
1. install a backend based on the Onebot v11 protocol (e.g. Lagrange.OneBot, LLOneBot)
2. Enable forward WS (Websocket) forwarding on these backends and memorize the port numbers.
3. Install the plugin, turn on the server and edit the bot.yml file:
```yaml
# Positive Websocket Configuration
ws:
  # host address
  host: "localhost"

  # port
  port: 3001

# Plugin-enabled group number
groups:
  - "114514"
```
- Change `ws.host` (host address) here to the address of the server where your backend is located (if your backend and server are on the same server, you don't need to change it).
- Change `ws.port` here to the port number you just memorized.
- Change `114514` in `groups` here to the QQ group number you want to enable bots.

4. Restart the server, if there is no error and warning, the connection is successful.

## License
This plugin is open source based on LGPL-2.1 agreement, please abide by the agreement, the final interpretation right belongs to *alazeprt* all rights reserved.
