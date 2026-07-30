# Docker、Redis 与 Redis Insight 使用说明

> 更新日期：2026-07-29  
> 适用环境：Windows + Docker Desktop（WSL 2 后端）

## 1. Docker 安装信息

| 项目 | 当前信息 |
| --- | --- |
| Docker Desktop | 4.84.0（234817） |
| Docker CLI | 29.6.2，build dfc4efb |
| 程序安装目录 | `C:\Program Files\Docker\Docker` |
| Docker CLI | `C:\Program Files\Docker\Docker\resources\bin\docker.exe` |
| 用户运行数据 | `C:\Users\褚俊辉\AppData\Local\Docker` |
| 用户配置 | `C:\Users\褚俊辉\AppData\Roaming\Docker`、`C:\Users\褚俊辉\.docker` |
| WSL 数据虚拟磁盘 | `C:\Users\褚俊辉\AppData\Local\Docker\wsl\disk\docker_data.vhdx` |

镜像、容器和 Docker 数据卷主要保存在 `docker_data.vhdx` 中。不要手动编辑或删除该文件，否则可能造成全部 Docker 数据丢失。

## 2. Docker 使用前提

1. 启动 Docker Desktop。
2. 等待 Docker Engine 完成启动。
3. 打开一个新的 PowerShell 窗口。
4. 执行以下命令确认 Client 和 Server 均正常：

```powershell
docker version
```

如果只能看到 Client，或者提示无法连接 `docker_engine`，说明 Docker Desktop 尚未启动完成。

## 3. Docker 常用命令

### 3.1 查看运行状态

```powershell
# 查看正在运行的容器
docker ps

# 查看所有容器，包括已经停止的容器
docker ps -a

# 查看本地镜像
docker images

# 查看 Docker 系统信息
docker info

# 查看 Docker 磁盘占用
docker system df

# 实时查看容器资源使用情况
docker stats
```

### 3.2 容器生命周期

```powershell
# 启动容器
docker start <容器名>

# 停止容器
docker stop <容器名>

# 重启容器
docker restart <容器名>

# 查看容器详情
docker inspect <容器名>

# 查看容器日志
docker logs <容器名>

# 持续跟踪日志
docker logs -f <容器名>
```

### 3.3 镜像操作

```powershell
# 下载镜像
docker pull <镜像名>:<版本>

# 删除不再使用的镜像（删除前确认没有容器依赖它）
docker rmi <镜像ID或镜像名>
```

### 3.4 删除操作

以下命令会删除容器或数据，执行前需要确认目标名称：

```powershell
# 删除已停止的容器
docker rm <容器名>

# 删除数据卷，会永久删除卷中的数据
docker volume rm <数据卷名>
```

不要随意执行 `docker system prune --volumes`，该命令可能删除未使用的数据卷和重要数据。

## 4. 已安装服务：Redis

| 项目 | 当前信息 |
| --- | --- |
| 容器名称 | `aicode-redis` |
| Docker 镜像 | `redis:7.4-alpine` |
| Redis 实际版本 | 7.4.10 |
| 运行模式 | standalone |
| 主机地址 | `localhost` |
| 主机端口 | `6379` |
| 容器端口 | `6379` |
| 默认数据库 | `0` |
| 密码 | 当前未设置 |
| 当前重启策略 | `no` |
| 数据目录（容器内） | `/data` |
| 当前数据卷 | `68878f29796f3d5bd865d2dda70478396bb1717839a6c171fb0a971e17ca77c0` |

当前 Redis 数据卷是 Docker 自动生成的匿名卷，其 Docker Linux 路径为：

```text
/var/lib/docker/volumes/68878f29796f3d5bd865d2dda70478396bb1717839a6c171fb0a971e17ca77c0/_data
```

物理数据实际位于 Docker 的 WSL 虚拟磁盘 `docker_data.vhdx` 中，不应直接修改。

## 5. Redis 容器常用操作

```powershell
# 查看 Redis 是否正在运行
docker ps --filter "name=aicode-redis"

# 启动 Redis
docker start aicode-redis

# 停止 Redis
docker stop aicode-redis

# 重启 Redis
docker restart aicode-redis

# 查看 Redis 日志
docker logs aicode-redis

# 持续跟踪 Redis 日志
docker logs -f aicode-redis

# 进入 Redis 命令行
docker exec -it aicode-redis redis-cli

# 直接测试 Redis 连接
docker exec -it aicode-redis redis-cli ping
```

`PING` 返回 `PONG` 表示 Redis 服务正常。

### 设置 Redis 随 Docker Desktop 自动启动

当前容器的重启策略是 `no`，默认不会自动重启。只需执行一次：

```powershell
docker update --restart unless-stopped aicode-redis
```

设置后，只要 Docker Desktop 正常启动，Redis 容器通常也会自动启动。可用下面的命令确认：

```powershell
docker inspect aicode-redis --format '{{.HostConfig.RestartPolicy.Name}}'
```

期望输出：

```text
unless-stopped
```

## 6. Redis 常用命令

先进入 Redis 控制台：

```powershell
docker exec -it aicode-redis redis-cli
```

### 6.1 基础检查

```redis
PING
INFO
INFO server
INFO memory
SELECT 0
DBSIZE
```

### 6.2 查询键

```redis
# 分批扫描所有键，推荐使用
SCAN 0

# 按名称扫描
SCAN 0 MATCH *chat* COUNT 100

# 查看键的数据类型
TYPE keyName

# 检查键是否存在
EXISTS keyName
```

生产或数据较多的环境不要使用 `KEYS *`，它可能长时间阻塞 Redis，应使用 `SCAN`。

### 6.3 String

```redis
SET demo:key "hello"
GET demo:key
SETEX demo:temp 60 "value"
DEL demo:key
EXPIRE demo:key 3600
TTL demo:key
```

### 6.4 Hash

```redis
HSET demo:user name "MarcusChu" role "admin"
HGET demo:user name
HGETALL demo:user
HDEL demo:user role
```

### 6.5 List

```redis
LPUSH demo:list "item1"
RPUSH demo:list "item2"
LRANGE demo:list 0 -1
LPOP demo:list
```

### 6.6 Set

```redis
SADD demo:set "a" "b" "c"
SMEMBERS demo:set
SISMEMBER demo:set "a"
SREM demo:set "a"
```

### 6.7 Sorted Set

```redis
ZADD demo:rank 100 "user1" 90 "user2"
ZRANGE demo:rank 0 -1 WITHSCORES
ZREVRANGE demo:rank 0 -1 WITHSCORES
```

## 7. AICodeGenerate 项目连接配置

项目当前使用以下连接信息：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      ttl: 3600
```

该项目使用 Redis 保存 LangChain4j 聊天记忆。Maven 中的 Redis 依赖只是 Java 客户端，Redis 容器必须处于运行状态。

## 8. Redis Insight 图形化客户端

Redis Insight 是 Redis 官方提供的桌面图形化管理工具，可用于查看和编辑键值、执行 Redis 命令、分析内存和查看慢查询。

### 8.1 安装信息

| 项目 | 当前信息 |
| --- | --- |
| 客户端 | Redis Insight |
| 当前版本 | 2.70.1 |
| 发布者 | Redis Ltd. / Redis Inc. |
| 安装目录 | `C:\Users\褚俊辉\AppData\Local\Programs\Redis Insight` |
| 主程序 | `C:\Users\褚俊辉\AppData\Local\Programs\Redis Insight\Redis Insight.exe` |

可以在 Windows 开始菜单中搜索 `Redis Insight` 启动客户端。

### 8.2 添加本地 Redis 连接

1. 确认 Docker Desktop 和 `aicode-redis` 容器正在运行。
2. 打开 Redis Insight。
3. 点击 `+ Add Redis database`。
4. 点击 `Connection Settings`，填写下面的参数。
5. 点击 `Test Connection`，连接成功后点击 `Add Redis Database`。

| 参数 | 配置值 |
| --- | --- |
| Database Alias | `aicode-redis`，也可以自定义 |
| Host | `127.0.0.1` |
| Port | `6379` |
| Username | `default` 或留空 |
| Password | 留空 |
| Timeout | `30` |
| Logical Database | `0` |
| Security / TLS | 关闭 |

也可以直接使用连接 URL：

```text
redis://127.0.0.1:6379
```

连接设置界面如下：

![Redis Insight 连接设置](images/redis-insight-connection-settings.png)

### 8.3 浏览和搜索 Redis 数据

连接数据库后，点击左侧钥匙形状的 `Browser` 图标：

- 左侧显示 Redis 键列表。
- 顶部搜索框可以按键名或通配符过滤。
- `All Key Types` 可以按 String、Hash、List、Set 等类型筛选。
- 点击某个键，可在右侧查看它的数据类型、TTL 和内容。
- `+ Key` 可以新建键；`Bulk Actions` 可以批量操作。

![Redis Insight 数据浏览器](images/redis-insight-browser.png)

删除、覆盖或批量修改键会直接改变 Redis 中的数据，操作前应确认键名和当前环境。

### 8.4 使用 Workbench 执行 Redis 命令

点击左侧的 `Workbench` 图标，在上方编辑区输入 Redis 命令，然后点击 `Run`。例如：

```redis
PING
DBSIZE
SCAN 0 MATCH *chat* COUNT 100
INFO memory
```

Workbench 支持一次输入多条命令，并在下方展示执行结果：

![Redis Insight Workbench](images/redis-insight-workbench.png)

### 8.5 查看项目缓存数据

启动 AICodeGenerate 后，可以在 Browser 的搜索框中输入项目实际使用的键名前缀，例如：

```text
*chat*
```

如果列表为空，请依次检查：

1. 项目是否连接的是 `localhost:6379`。
2. Redis Insight 当前是否选择了 `db0`。
3. 项目是否已经执行过会写入缓存或聊天记忆的功能。
4. Redis 键是否已经因 TTL 到期而自动删除。

## 9. IDEA Database 连接 Redis（备选）

在 IDEA Ultimate 中打开：

```text
View → Tool Windows → Database → + → Data Source → Redis
```

填写：

```text
Connection type: standalone
Host: localhost
Port: 6379
Database: 0
Authentication: No auth
```

如果提示缺少驱动，点击 `Download missing driver files`，然后点击 `Test Connection`。

当前 IDEA 2023.3.4 使用 JBR 17，Redis JDBC 驱动可能提示与当前 JRE 不兼容。出现该问题时优先使用 Redis Insight，不需要修改 Redis 容器配置。

## 10. 常见问题

### PowerShell 无法识别 docker

关闭旧 PowerShell 窗口并重新打开，再执行：

```powershell
docker --version
```

### 无法连接 docker_engine

启动 Docker Desktop，并等待 Docker Engine 完成初始化。

### Redis 连接被拒绝

```powershell
docker ps --filter "name=aicode-redis"
docker start aicode-redis
docker exec -it aicode-redis redis-cli ping
```

### Redis Insight 无法连接

先确认 Redis 容器及端口：

```powershell
docker ps --filter "name=aicode-redis"
docker port aicode-redis
docker exec -it aicode-redis redis-cli ping
```

如果 `PING` 返回 `PONG`，请检查 Redis Insight 是否使用：

```text
Host: 127.0.0.1
Port: 6379
Password: 空
TLS: 关闭
```

### 端口 6379 被占用

```powershell
Get-NetTCPConnection -LocalPort 6379 -ErrorAction SilentlyContinue
```

### 容器名称已存在

不要重复执行 `docker run --name aicode-redis ...`。已有容器应使用：

```powershell
docker start aicode-redis
```
