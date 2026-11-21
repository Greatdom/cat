# 接口文档 · cat

> **这份文档回答：cat（Java 业务主体）里每个类有哪些字段和方法、语义是什么。**
>
> 跳板侧的接口见 [`接口文档-跳板.md`](./接口文档-跳板.md)。
>
> 状态标记：✅ 已实现 · 🟡 部分实现 · ⬜ 空实现
>
> 本文按包组织，与源码目录一一对应。

---

## 目录

| 包 | 类数 | 说明 |
|---|---|---|
| [archive](#一archive存档) | 4 | 存档 |
| [message](#二message消息模型) | 8 | 消息模型 |
| [channel](#三channel渠道抽象) | 14 | 渠道抽象与实现 |
| [processor](#四processor三层处理器) | 12 | 三层处理器 |
| [service](#五service业务编排) | 12 | 业务编排 |
| [state](#六state状态系统) | 23 | 四套状态系统（空壳） |
| [ego](#七ego三我) | 5 | 三我（空壳） |
| [schedule](#八schedule计划与时钟) | 9 | 计划与时钟 |
| [web](#九webhttp-接口) | 13 | HTTP 接口 |
| [log](#十log日志) | 5 | 日志 |
| [exception](#十一exception异常) | 3 | 异常 |
| [common](#十二common基础设施) | 36 | 基础设施 |

---

## 一、`archive/` 存档

### `Archive`（record）✅

一个存档 = 一个模拟人。

| 字段 | 类型 | 说明 |
|---|---|---|
| `version` | `String` | 存档文件格式版本 |
| `id` | `long` | 雪花 ID，跨进程序列化时转字符串 |
| `name` | `String` | 存档名 |
| `enabled` | `Boolean` | 是否启用 |
| `memory` | `Memory` | 记忆库引用 |
| `session` | `Session` | 绑定的 DSH 会话 |
| `links` | `List<Link>` | 外部连接 |
| `contacts` | `List<ChatKey>` | 这个存档认识的聊天 |
| `pipeline` | `PipelineConfig` | 装配单 |
| `createdAt` / `updatedAt` | `String` | ISO 时间 |

```java
public boolean isEnabled()                        // Boolean.TRUE.equals(enabled)
public List<ChatKey> contactsOf(ChannelKind)      // 启用中且属于该渠道的 contacts
```

**内部 record**：

| 类型 | 字段 | 约束 |
|---|---|---|
| `Memory` | `storeId` | — |
| `Session` | `sessionId, status, title` | `isBound()` = `BOUND` 且 id 非空 |
| `Link` | `id, kind, enabled, account, baseUrl, token, deviceId` | 构造时校验：`kind` 必须属于 `EXTERNAL`，否则抛 `IllegalArgumentException` |

```java
public enum SessionStatus { BOUND, LOST }
```

### `ArchiveStore` ✅

存档的内存索引 + 落盘 + 校验。

| 方法 | 说明 |
|---|---|
| `List<Archive> all()` | 全部存档，按 id 升序 |
| `Optional<Archive> find(long)` | 按 id 查 |
| `List<Archive> enabled()` | 只返回启用的 |
| `Set<String> allAccounts()` | 所有已占用账号，格式 `{channel.wire()}:{account}` |
| `int maxCount()` | 存档数上限（读 `EditAbleDefaultConfig.archiveMaxCount`） |
| `Archive create(Archive)` | 分配雪花 id、填时间戳、落盘 |
| `Archive update(long, Archive)` | 保留 id 与 `createdAt`，其余按传入值 |
| `void delete(long)` | 从内存移除，目录移进 `data/rubbish/{id}-{ts}/` |
| `void markSession(long, SessionStatus)` | 只改会话状态，其余不变 |

**落盘**：`data/{id}/archive.json`，用 `writePretty` 写，先写 `.tmp` 再 `ATOMIC_MOVE`。

**五条校验**（`create` / `update` 都会跑）：

1. 存档名非空且唯一
2. 会话 id 不与其他存档重复
3. 同渠道账号不与其他存档重复
4. 同一次提交内账号不重复
5. 数量不超过 `archiveMaxCount`

### `ArchiveRegistry` ✅

「我在服务哪些存档」的快照。

```java
public boolean replaceAll(Map<Long, Archive> incoming)  // null = 不动，空 map = 清空
public synchronized void rebuildFromStore()             // 丢弃上报数据，回退到 ArchiveStore
public Map<Long, Archive> served()
public Optional<Archive> of(long archiveId)
public Set<ChatKey> allChats()
public static Set<ChatKey> chatsOf(Archive a)
```

> `served()` 有三态：上报过就返回上报的，没上报过就从 `ArchiveStore.enabled()` 推导。
> **当前没有生产代码调用它**，运行时枚举存档走 `ArchiveStore.enabled()`。

### `PipelineConfig`（record）✅

装配单：每个链路钩子用哪个实现。

| 字段 | 类型 |
|---|---|
| `preset` | `PresetKind` |
| `memory` | `MemoryKind` |
| `environment` | `EnvironmentKind` |
| `physiology` | `PhysiologyKind` |
| `understand` | `UnderstandKind` |
| `intent` | `IntentKind` |
| `observe` | `ObserveKind` |
| `missedSchedule` | `MissedPolicyKind` |

```java
public static PipelineConfig preset(PresetKind)    // ADVISOR / BLACK_CAT / 其他 → 默认
public static PipelineConfig defaultConfig()
```

---

## 二、`message/` 消息模型

### `Message`（abstract）✅

所有消息的基类。

| 字段 | 类型 | 说明 |
|---|---|---|
| `archiveId` | `long` | 归哪个存档 |
| `channel` | `ChannelKind` | 渠道类型 |
| `source` | `MessageSource` | 从哪来 + 谁发的 |
| `timestamp` | `long` | 毫秒 |
| `channelMessageId` | `String` | 渠道侧消息号，去重用 |
| `sessionId` | `String` | 归属的 DSH 会话 |

```java
public String chatType()      // source.origin.chatType
public String chatId()        // source.origin.chatId
public String senderId()      // source.sender.id
public ChatKey chatKey()      // 缺字段或非外部渠道时返回 null
```

### `NormalMessage` ✅

```java
+ raw: Object          // 原始报文，调试用
+ content: Content     // 真正的内容

public String text()   // Content.plainText(content)
```

### `InboundMessage` ✅

```java
+ assessment: Assessment        理解阶段的结果（当前恒为 null）
+ recallBundle: RecallBundle    回忆阶段的结果（当前恒为 null）
+ egoDecision: EgoDecision      三我裁决的结果（当前恒为 null）
```

### `OutboundMessage` ✅

```java
+ messages: List<NormalMessage>    模型一次回复经代码处理后的一串
+ inboundMessage: InboundMessage   它当初在回谁

public void add(NormalMessage)
```

### `Content`（sealed interface）✅

```java
public static String plainText(Content)   // 抽纯文本，给拼提示词用
public static Content text(String)
public static Content image(String url, String path)
```

| 实现 | 字段 | `plainText` 输出 |
|---|---|---|
| `Text` | `value` | 原文 |
| `FileRef` | `url, path, mime, fileKind` | `[图片]` / `[语音]` / `[视频]` / `[文件]` |
| `Coordinate` | `x, y, z` | `[坐标: x, y, z]` |
| `Multi` | `parts: List<Content>` | 递归拼接，空格分隔 |

### `ChatKey`（record）✅

会话标识，形如 `{channel}:{chatType}:{chatId}`。

| 字段 | 类型 |
|---|---|
| `type` | `ChatType`（`PRIVATE` / `GROUP`） |
| `id` | `String`（纯数字） |
| `channelKind` | `ChannelKind`（必须是外部渠道） |
| `enabled` | `Boolean` |

```java
public static ChatKey parse(String)   // "{channel}:{chatType}:{chatId}" → ChatKey，格式错抛 IAE
public String wire()                  // 反向
public boolean isGroup()
public boolean isEnabled()            // TRUE.equals(enabled)
```

> **`equals` / `hashCode` 排除 `enabled`**——它只是配置项，不参与会话身份比较。

### `MessageSource`（record）✅

```java
public record Origin(ChannelKind channel, String chatType, String chatId, String chatName)
public record Sender(String id, String name, String card, SenderKind kind)
public enum SenderKind { HUMAN, BOT, SYSTEM }
```

### `Assessment`（record）⬜

理解阶段的产物，字段已定义，**没有生产方**。

```java
Urgency urgency, double arousalGuess, double valenceGuess,
boolean addressedToMe, boolean isLateNight, String text

public static Assessment synthetic(String description)   // 内部触发用的合成结果
```

### `RecallBundle`（record）⬜

回忆阶段的产物，字段已定义，**没有生产方**。

```java
String memoryNarrative, PersonalityView personality, EmotionState emotion,
ShortTermMemory shortTerm, EnvironmentState environment,
PhysiologyState physiology, List<Capability> capabilities
```

---

## 三、`channel/` 渠道抽象

### 四个契约接口 ✅

```java
public interface ChannelPoller<T> {
    ChannelKind kind();
    List<T> poll(PollContext) throws PollException;
    default int suggestedPollIntervalMs() { return 2000; }
}

public interface InboundConvertor<T> {
    ChannelKind kind();
    List<NormalMessage> convert(List<T> raw, ConvertContext context);
}

public interface OutboundConvertor<P> {
    ChannelKind kind();
    List<P> convert(NormalMessage normalMessage);
}

public interface ChannelSender<P> {
    ChannelKind kind();
    int send(SendContext context, List<P> payloads);        // 返回成功条数
    default int suggestedSendIntervalMs() { return 2000; }
}
```

### 三个上下文 ✅

```java
public record PollContext(long archiveId, Archive.Link link, Set<ChatKey> chats, int limit) {
    public String account();
}

public record SendContext(long archiveId, Archive.Link link, String chatType, String chatId) {
    public String account();
}

public record ConvertContext(long archiveId, ChannelKind channel, String selfAccount,
                             Set<String> accountKeys, String sessionId) {
    public boolean isSelf(String senderId);
    public boolean isAnotherArchive(String senderId);
    public boolean shouldDrop(String senderId);      // isSelf || isAnotherArchive
    public static String key(ChannelKind, String account);   // "{channel}:{account}"
}
```

**`ConvertContext` 是防套娃的关键**：`shouldDrop` 为真时，转换器丢弃这条消息——避免存档读到自己的发言、或别的存档的发言。

### `ChannelRegistry` ✅

按 `ChannelKind` 把 poller ↔ convertor、convertor ↔ sender 配对。

```java
public Optional<InboundPair<?>> inboundPair(ChannelKind)
public Optional<OutboundPair<?>> outboundPair(ChannelKind)
public Set<ChannelKind> available()          // 入站或出站任一可用即算

public record InboundPair<T>(ChannelPoller<T> poller, InboundConvertor<T> convertor) {}
public record OutboundPair<P>(OutboundConvertor<P> convertor, ChannelSender<P> sender) {}
```

启动时（`@PostConstruct`）扫描所有 `@Component` 实现，按 `kind()` 配对。缺一半的组合会打 WARN 并跳过。

### `onebot/` —— 一个外部渠道的具体实现 ✅

| 类 | 职责 |
|---|---|
| `OneBotClient` | HTTP action 调用，判 `status/retcode` |
| `OneBotConnectionPool` | 每 `(archiveId, account)` 一条连接，url/token 变了就重建 |
| `OneBotPoller` | `get_group_msg_history` / `get_friend_msg_history` |
| `OneBotInboundConvertor` | 事件 → `NormalMessage`，过滤自己与别的存档 |
| `OneBotOutboundConvertor` | `NormalMessage` → 消息段数组 |
| `OneBotSender` | `send_group_msg` / `send_private_msg` |

```java
// OneBotClient
public JsonNode call(String action, Map<String, Object> payload) throws OneBotException
public boolean matches(String baseUrl, String token)

// OneBotConnectionPool
public OneBotClient forAccount(long archiveId, Archive.Link link)
public void evict(long archiveId)
public int size()
```

**核心不依赖它**：`ChannelRegistry` 只按 `kind()` 配对，从不 import `onebot` 包。

---

## 四、`processor/` 三层处理器

### `Processor<T>`（核心接口）✅

```java
public interface Processor<T extends Message> {
    ProcessorKind kind();
    int order();                        // 小的先执行
    List<T> process(List<T> in);
    default void validateKind() {}      // 三个子接口会重写它以校验 kind 与接口匹配
}
```

### 三个特化

| 接口 | 泛型 | 校验 |
|---|---|---|
| `PreProcessor` | `Processor<InboundMessage>` | `kind().getFamily() == PRE_PROCESSOR` |
| `ThinkProcessor` | `Processor<InboundMessage>` | `kind().getFamily() == THINK_PROCESSOR` |
| `PostProcessor` | `Processor<OutboundMessage>` | `kind().getFamily() == POST_PROCESSOR` |

`validateKind()` 在 `@PostConstruct` 里对每个实现调用一次，家族不匹配直接抛 `IllegalStateException`，**启动即失败**。

### 八个实现

| 类 | 层 | order | 状态 |
|---|---|---|---|
| `DedupProcessor` | 前处理 | 100 | ✅ |
| `UnderstandProcessor` | **前处理** | **200** | ⬜ |
| `MergeProcessor` | 前处理 | **300** | ✅ |
| `RecallProcessor` | 思考 | 100 | ⬜ |
| `EgoProcessor` | 思考 | 200 | ⬜ |
| `SlimProcessor` | 思考 | 300 | ⬜ |
| `ExpressProcessor` | 后处理 | 100 | ⬜ |
| `ObserveProcessor` | 后处理 | 200 | ⬜ |

**`DedupProcessor.process()`** 的丢弃条件（依次判断）：

1. `chatKey()` 为 null
2. 没有 `sessionId`
3. `sessionId` 不在 `AvailableSessions` 里
4. 消息时间戳（秒）< 该会话水位
5. `DeliveryState.tryAccept()` 返回 false（已投递 / 处理中未超时）

放行的会被标记为 `PROCESSING`，并在有输出时调 `deliveryState.save()`。

**`UnderstandProcessor.process()`** ⬜：

当前 `return in`。目标是把每条消息的 `Assessment` 填进 `InboundMessage.setAssessment()`：

| 字段 | 含义 |
|---|---|
| `urgency` | 急不急 |
| `arousalGuess` / `valenceGuess` | 情绪唤醒度 / 效价的粗估 |
| `addressedToMe` | 是不是在跟我说话 |
| `isLateNight` | 是不是深夜 |
| `text` | 原文 |

**`MergeProcessor.process()`** 的两阶段：

1. 入桶：按 `chatKey` 分桶，桶内按时间戳升序
2. 出桶：队头躺够 `mergeWindowMs` 才取，贪心合并相邻间隔 ≤ 窗口且总数 ≤ `mergeMaxCount` 的

合并后只保留**最后一条**的 `archiveId/channel/source/timestamp/channelMessageId/sessionId/raw/assessment`，`content` 拼成 `Content.Multi`。`recallBundle` 与 `egoDecision` 仍为 null——它们由后面的思考链填。

---

## 五、`service/` 业务编排

### `InboundService` ✅

```java
public void drainTo(List<InboundMessage> target)   // 取空缓存池
public int cacheSize()
// 内部：handle() 每 500ms 跑一次
```

`handle()` 遍历所有启用存档 × 启用连接，调 `poller.poll()` + `convertor.convert()`，转成 `InboundMessage` 写进缓存池。

**每个存档的失败被隔离**：单条连接抛异常只记 WARN，其他存档照常。

### `PreService` ✅

每 500ms：`drainTo` → 按 order 跑 `PreProcessor` 链 → `TriggerService.trigger()`。

> **即使没有新消息也要跑完整条链**——有状态的处理器（如合并）靠每轮吐出积压。

### `TriggerService` ✅

```java
public void trigger(List<InboundMessage> messages)
```

对每条消息：`pathDecider.decide()` → `REFLEX` 走 `ReflexService`，`DELIBERATION` 走 `ThinkService`。

`buildContext()` 目前返回空壳（四个状态系统都没实现）。

### `ThinkService` ✅

```java
public void run(List<InboundMessage> messages)
```

按 order 跑 `ThinkProcessor` 链,结果 `queues.enqueueAll()`。

### `ReflexService` ⬜

```java
public void run(InboundMessage message, DecisionContext ctx)   // 方法体只有 TODO
```

### `PostService` ✅

```java
public void enqueue(OutboundMessage message)
// 内部：handle() 每 500ms 跑
```

取空缓存池 → 按 order 跑 `PostProcessor` 链 → 逐条 `outBoundService.enqueue()`。

### `OutBoundService` ✅

```java
public void enqueue(OutboundMessage out)
public int cacheSize()
public void forget(long archiveId)
// 内部：handle() 每 500ms 跑
```

**分桶粒度是渠道**：`Map<ChannelKind, Queue<Pending>>`。每轮每个桶最多发一条，间隔由 `sender.suggestedSendIntervalMs()` 决定。

**发送先出队再发**：`bucket.poll()` 在 `sendOne()` 之前执行，成不成功都出队。

### `ScheduleService` ⬜

```java
public ScheduleItem create(ScheduleItem draft)     // 返回 null
public void cancel(String id)                       // 空
public List<ScheduleItem> list(long archiveId)      // 返回 null
// 内部：date() 每 20s、desire() 每 30min，都是 TODO
```

### `SettingsService` ✅

```java
public Map<String, Object> current()                // 默认值 + 用户覆盖
public Map<String, Object> overrides()              // 只含被改过的
public int save(Map<String, Object> incoming)       // 返回实际改动项数
public boolean reset(String key)                    // 单项恢复默认
public void clearAll()                              // 全部恢复默认
```

**`applyToRuntime()` 的顺序**：先写全部默认值，再写用户覆盖——这样删掉某一项覆盖后，它会退回默认。

**落盘**：`data/settings.json`，只存覆盖项。

### `service/tool/`

```java
public record DecisionContext(EmotionState emotion, Map<NeedKind, Double> needs,
                              EnvironmentState environment, PhysiologyState physiology,
                              List<ReflexRule> reflexRules)

public class PathDecider {
    public ThinkingPath decide(InboundMessage, DecisionContext)    // ✅ 恒返回 DELIBERATION
}

public class ReflexRule { }    // ⬜ 空类
```

---

## 六、`state/` 状态系统

**整个包只有数据形状与接口签名，没有任何实现类。**

### `memory/` 记忆

```java
public interface MemorySystem {
    RecallResult recall(RecallQuery query);
    PersonalityView personality();
    EmotionState emotion();
    ShortTermMemory shortTerm();
    void write(LongTermMemoryEntry entry);
    void appendShortTerm(ShortTermMemoryEntry entry);
    void resolveAsk(long archiveId, String entryId);
    MemoryKind kind();
}
```

| record | 关键字段 |
|---|---|
| `EmotionState` | `valence, arousal, dominance`（PAD 三维）、`Map<BasicEmotion, Double> mix`、`List<String> complex`、`updatedAt, fadeAt` |
| `PersonalityView` | `selfImage` / `behavior`（各是 `Map<TraitKind, Double>`）、两个 narrative、`bias`、`source`、`computedAt` |
| `LongTermMemoryEntry` | `id, kind, content, subject, importance, emotion, createdAt, lastUsedAt, supersededBy, tags, extra` |
| `ShortTermMemory` | `entries, emotion, windowStart, windowEnd` |
| `ShortTermMemoryEntry` | `id, at, kind, content, meta, resolved` |
| `RecallQuery` | `text, topicHints, aboutWhom, maxEntries, maxChars` |
| `RecallResult` | `narrative, entries, scores` |

**`EmotionState` 的方法**：

```java
public static EmotionState calm()              // ⬜ 返回 null
public IdSuperegoBias idSuperegoBias()         // ⬜ 返回 null
public String promptFragment()                 // ⬜ 返回 ""
public record IdSuperegoBias(double idMultiplier, double superegoMultiplier) {}
```

**`PersonalityView` 的方法**：

```java
public boolean hasBlindSpot()      // ✅ bias > 0.4
public String promptFragment()     // ⬜ 返回 ""
public static PersonalityView neutral()   // ⬜ 返回 null
```

### `environment/` 环境

```java
public interface EnvironmentSystem {
    EnvironmentState current();
    void update(EnvironmentUpdate update);
    EnvironmentKind kind();
}
```

三层结构：`SpaceGrid`（整个空间）→ `Zone`（区域）→ `Cell`（坐标），外加 `Prop`（道具）与 `Terrain`（地形）。

```java
public record EnvironmentState(SpaceGrid grid, Zone currentZone, Cell position,
                               Map<String, Double> ambience, List<Prop> nearby,
                               long updatedAt, String source) {
    public static EnvironmentState cozyHome();    // ⬜ 返回 null
}
```

### `physiology/` 生理

```java
public interface PhysiologySystem {
    PhysiologyState current();
    void tick(long elapsedMs);
    void applyComfort(EnvironmentState env);
    PhysiologyKind kind();
}

public record PhysiologyState(double health, double energy, double comfort,
                              CycleState cycle, Set<String> symptoms,
                              long updatedAt, String source) {
    public Map<NeedKind, Double> needBoost();     // ⬜ 返回空 map
    public double emotionSensitivity();           // ✅ 返回 1.0
}
```

### `need/` 需求

```java
public interface NeedPool {
    Map<NeedKind, Double> snapshot();
    void accumulate(long elapsedMs);        // 时间流逝 → 需求上涨
    void boost(NeedKind kind, double amount);
    void consume(NeedKind kind, double amount);
    List<Desire> ripeDesires();             // 超阈值的
}

public interface CapabilityProvider {
    ChannelKind kind();
    List<Capability> capabilities();
}
```

```java
public record Desire(NeedKind need, double intensity, String reason)
public record Capability(NeedKind need, String action, double effectiveness, ChannelKind channel)
```

---

## 七、`ego/` 三我

**整个包只有数据形状与接口签名。**

```java
public record IdWant(double intensity, String reason)

public record SuperegoRule(String id, String condition, SuperegoAction action,
                           double baseWeight, Map<TraitKind, Double> traitWeights) {
    public double effectiveWeight(PersonalityView p);   // ⬜ 返回 baseWeight
}

public record EgoDecision(NormalMessage normalMessage, String reconciliation,
                          Map<String, Object> trace) {
    public static EgoDecision silence(String why);      // ⬜ 返回 null
    public static EgoDecision respond(NormalMessage, String why);   // ⬜ 返回 null
}

public class EgoDecider {
    public EgoDecision decide(IdWant, List<SuperegoRule>, PersonalityView, EmotionState);
    // ⬜ 返回 null
}

public class SuperegoEngine {
    public List<SuperegoRule> evaluate(Trigger, PersonalityView);
    // ⬜ 返回 List.of()
}
```

> `SuperegoEngine.evaluate()` 的参数类型是 Spring 的 `org.springframework.scheduling.Trigger`——这是**导入错误**，正确的应该是业务里的 `Trigger`（尚未定义）。

---

## 八、`schedule/` 计划与时钟

```java
public interface Clock {
    long nowEpochSecond();
}

public interface MissedPolicy {
    MissedPolicyKind kind();
    void handle(ScheduleItem item, long lateByMs);
}

public record ScheduleItem(String id, long archiveId, ScheduleKind kind,
                           String at, String cron, NeedKind forNeed, String payload,
                           TimeAnchor anchor, long catchUpWindowMs,
                           boolean enabled, long lastFiredAt) {}
```

| 实现 | 状态 |
|---|---|
| `ScheduleStore.load(long)` / `save(long, List)` | ⬜ `load` 返回 null |
| `ArchiveClock.nowEpochSecond()` | ⬜ 返回 0 |
| `ArchiveClock.realNow()` | ✅ `System.currentTimeMillis()` |
| `ArchiveClock.flowRate()` | ✅ 返回 1.0 |
| `ArchiveClock.setMode(TimeMode)` | ⬜ 空 |
| `CatchUpPolicy` / `DropLogPolicy` / `BoostNeedPolicy` | ⬜ 方法体全是 TODO |
| `IgnorePolicy` | ✅ 刻意留空 |

```java
public enum TimeMode { INDEPENDENT, SYNCED, ACCELERATED }
```

---

## 九、`web/` HTTP 接口

### 端点总表

| 方法 | 路径 | 请求 | 响应 |
|---|---|---|---|
| GET | `/api/archives` | — | `Result<List<ArchiveView>>` |
| GET | `/api/archives/candidates` | — | `Result<CandidatesView>` |
| GET | `/api/archives/{id}` | — | `Result<ArchiveView>` |
| POST | `/api/archives` | `ArchiveRequest` | `Result<ArchiveView>` |
| DELETE | `/api/archives/{id}` | — | `Result<?>` |
| POST | `/api/heartbeat` | `HeartBeatRequest` | `Result<?>` |
| GET | `/api/health` | — | `Result<?>`（data 是字符串） |
| GET | `/api/inbound_message/get` | `?sessionIds=a&sessionIds=b` | `Result<InboundGetResponse>` |
| POST | `/api/inbound_message/ack` | `AckRequest` | `Result<?>` |
| POST | `/api/outbound_message/push` | `OutboundMessageRequest` | `Result<OutboundMessageResponse>` |
| GET | `/api/logs` | `?limit=&itemId=&source=` | `Result<MergedLogs>` |
| GET | `/api/settings` | — | `Result<SettingsView>` |
| POST | `/api/settings` | `Map<String,Object>` | `Result<{changed, values}>` |
| POST | `/api/settings/reset/{key}` | — | `Result<SettingsView>` |
| DELETE | `/api/settings` | — | `Result<SettingsView>` |

页面入口（`PageController`，全部 302 重定向到 `static/*.html`）：

| 路径 | 目标 |
|---|---|
| `/` | `/settings.html` |
| `/settings` | `/settings.html` |
| `/archives` | `/archives.html` |
| `/logs` | `/logs.html` |

### `ArchiveController`

```java
public record ArchiveRequest(String id, String name, Boolean enabled,
                             SessionView session, List<LinkView> links, List<String> chats)

public record SessionView(String sessionId, String status, String title)

public record LinkView(Long id, String kind, Boolean enabled, String account,
                       String baseUrl, String token, String deviceId,
                       Boolean hasStoredToken)

public record CandidatesView(Map<String, String> sessions, List<ChannelKind> channels)

public record ArchiveView(String id, String name, boolean enabled, SessionView session,
                          List<LinkView> links, List<String> chats,
                          String createdAt, String updatedAt)
```

**语义要点**：

- `id` 为空 = 新建，有值 = 更新
- `chats` 是 `wire()` 字符串列表，如 `{channel}:{chatType}:{chatId}`
- **`LinkView.token` 只进不出**：响应里恒为 `null`，靠 `hasStoredToken` 告诉页面"服务端存过"
- **更新时 token 留空 = 沿用旧值**：按 `kind + account` 从旧存档里找回
- `sessions` 来自跳板心跳；`channels` 只列 `EXTERNAL` 家族的

### `HeartBeatController`

```java
public record HeartBeatRequest(Map<String, String> sessions, List<LogEntry> logs)
public record LogEntry(long time, String level, String chatKey, String itemId,
                       String logger, String text)
```

**`sessions` 是整体覆盖**：空 map 等于清空（跳板没报任何会话）。日志同理覆盖。

`/api/health` 返回 `Result.ok("服务器运行中")`——**只表示进程活着**，不反映渠道连通性。

### `InboundMessageController`

```java
public record AckRequest(List<Item> items)
public record Item(String archiveId, String sessionId, String channelMessageId,
                   String chatKey, long timestamp)

public record InboundGetResponse(List<InboundMessageDto> messages)
```

**`get`**：按传入的 `sessionIds` 每个桶 `peek` 一条，**不删除**。

**`ack`**：对每条 → `queues.ack()` 真删 → `deliveryState.markDelivered()` 推进水位。

> `chatKey` 解析失败时只标状态、不推水位（容错不阻断）。

### `OutBoundMessageController`

```java
public record OutboundMessageRequest(String archiveId, String sessionId,
                                     InboundRef inbound, List<Item> messages)
public record InboundRef(String archiveId, String sessionId, String channel,
                         String chatType, String chatId, String senderId,
                         String senderName, String card, String senderKind,
                         String channelMessageId, long timestamp, String text)
public record Item(String chatType, String chatId, String text)

public record OutboundMessageResponse(String sessionId) {
    public static OutboundMessageResponse accepted(String sessionId);
}
```

**一轮一个 ack**：`push` 成功入队后返回 `{sessionId}`；以下情况返回 `{sessionId: null}`（等于不 ack）：

- 请求为空 / `messages` 为空
- 有效消息（非空白文本）为空

以下情况返回 `Result.fail`：

- 存档不存在
- 渠道名无法识别（缺 `inboundMessage`）

### `InboundMessageDto`

```java
public record InboundMessageDto(String archiveId, String sessionId, String channel,
                                String chatType, String chatId, String senderId,
                                String senderName, String card, String senderKind,
                                String channelMessageId, long timestamp,
                                Content content, Assessment assessment,
                                RecallBundle recallBundle, EgoDecisionView egoDecision) {
    public static InboundMessageDto from(InboundMessage msg);
    public record EgoDecisionView(String reconciliation, Map<String, Object> trace) {}
}
```

**两处转换**：

- `archiveId`：`long` → `String`（JS 只有 53 位精度）
- `EgoDecision`：只导出 `reconciliation` + `trace`，`normalMessage` 不外发

### `LogsController`

```java
public record MergedLogs(List<Map<String, Object>> entries, int count,
                         Map<String, Integer> sources)
```

参数：`limit`（默认 200，上限 500）、`itemId`（按消息筛选）、`source`（`java` / `linker`）。

两源合并后按时间排序——**只能保证大致有序**，两源时钟精度不同。

### `Result<T>`

```java
public static <T> Result<T> ok()
public static <T> Result<T> ok(T data)
public static <T> Result<T> ok(String message, T data)
public static <T> Result<T> fail()
public static <T> Result<T> fail(String message)
public static <T> Result<T> fail(int code, String message)
public static <T> Result<T> fail(int code, String message, T data)
public boolean isSuccess()      // code == 200
```

**序列化形状**：`{code, message, data, success}`。`success` 来自 `isSuccess()`（Jackson 对 `isXxx()` 的自动映射）。

---

## 十、`log/` 日志

### `LogBuffer` ✅

```java
public void add(Entry entry)
public List<Entry> recent(int limit)
public List<Entry> byItemId(String itemId, int limit)

public record Entry(long time, String level, String chatKey, String itemId,
                    String logger, String text) {
    public Instant instant();
}
```

容量 `UnEditAbleDefaultConfig.LOG_BUFFER_CAPACITY`（200 条），超出从头丢弃。

### `LinkerLogReader` ✅

```java
public void replace(List<LogBuffer.Entry> incoming)    // 整体覆盖，最多留 150 条
public List<LogBuffer.Entry> recent(int limit)
public List<LogBuffer.Entry> byItemId(String itemId, int limit)
public int size()
```

### `LogBufferAppender` ✅

logback 附加器，由 `logback-spring.xml` 实例化（**不是 Spring Bean**）。

```java
public static void bind(LogBuffer target)    // 由 LogBufferHolder 调用
```

只收 `com.wdd.cat` 包下的日志；`bind` 之前的事件丢弃。

### `LogBufferHolder` ✅

`@PostConstruct` 时把容器里的 `LogBuffer` 绑给 `LogBufferAppender`。

### `MdcScope` ✅

给日志打 `chatKey` / `itemId` 标签。**目前没有调用方**。

```java
public static MdcScope of(String chatKey, String itemId)
public static MdcScope chat(String chatKey)
public static MdcScope item(String itemId)
public static MdcScope clearing()
public void close()        // 恢复进作用域前的值
```

必须配 try-with-resources 使用——三个 Service 跑在线程池上，不恢复原值会串到下一个任务。

---

## 十一、`exception/` 异常

```java
public class OneBotException extends RuntimeException {
    public String action();
}

public class PollException extends Exception { }   // checked：拉失败 ≠ 没消息，逼调用方处理

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<?> handle(Exception e);    // 统一转 Result.fail(e.getMessage())
}
```

---

## 十二、`common/` 基础设施

### `common/cache/`

**`AvailableSessions`** ✅

```java
public boolean contains(String sessionId)
public String title(String sessionId)
public List<String> getSessionIds()
public void replace(Map<String, String> incoming)    // 整体覆盖，空表等于清空
public int size()
```

空清单 = 跳板还没报过，此时任何消息都送不出去。

**`DeliveryState`** ✅

```java
public enum Status { NEW, PROCESSING, DELIVERED }
public record Entry(Status status, long updatedAt)

public static String deliveryKey(long archiveId, String channelMessageId)   // "1:84"
public static String watermarkKey(long archiveId, ChatKey chat)             // "1:{channel}:{chatType}:{chatId}"

public boolean isDelivered(long, String)
public boolean isProcessingAlive(long, String, long now)
public boolean tryAccept(InboundMessage msg)         // 放行并标 PROCESSING
public long watermark(long, ChatKey)
public long watermarkOrInit(long, ChatKey, long nowSeconds)   // 首次见到的会话初始化为"此刻"
public void advanceWatermark(long, ChatKey, long timeSeconds) // 只增不减
public void markDelivered(long, String, long timeSeconds, ChatKey)
public void forget(long archiveId)
public synchronized void save()                      // 落盘 data/watermarks.json
```

**处理中多久算残留**：`UnEditAbleDefaultConfig.processingTimeoutMs()` = `mergeWindowMs + 10000`。

**`InboundMessageControllerQueues`** ✅

```java
public void enqueue(InboundMessage msg)
public void enqueueAll(List<InboundMessage> messages)
public List<InboundMessage> peek(List<String> sessionIds)    // 每桶只取队头，不删
public boolean hasPending(String sessionId)
public boolean ack(String sessionId, String channelMessageId)  // 真删
public int size()
public void forget(String sessionId)
```

### `common/config/`

**`EditAbleDefaultConfig`** ✅

可配置项的**唯一权威**：字段 + 元数据 + 反射写入都在一个类里。

```java
public static volatile int archiveMaxCount = 4;
public static volatile long mergeWindowMs = 1200;
public static volatile int mergeMaxCount = 8;
public static volatile long qqPollAndSendIntervalMs = 2000;

public record Item(String key, String label, String hint, String unit, long min, long max)
public record Group(String title, String description, List<String> keys)

public static final List<Item> ITEMS
public static final List<Group> GROUPS
public static final Set<String> ALL              // = ITEMS 的 key 集合

public static Item item(String key)
public static boolean needsRestart(String key)   // ✅ 恒 false
public static Map<String, Object> defaults()     // 出厂默认值副本
public static boolean write(String key, long value)
public static Object read(String key)
```

**约定**：`ITEMS` 里每个 key 必须有同名 `static volatile int/long` 字段。反射写入会缓存字段句柄；`ConfigFieldBindingTest` 逐项检查这条约定。

**出厂默认值 `DEFAULTS`** 在类初始化时从字段读一次并固化——不能拿字段当前值当默认值，否则 `applyToRuntime()` 无法把字段恢复成默认。

**`UnEditAbleDefaultConfig`** ✅

```java
ARCHIVE_VERSION = "1"
DATA_DIR = "data"
TICK_MS = 500                    // 四个 Service 的周期
SCHEDULE_DATE_MS = 20_000
SCHEDULE_DESIRE_MS = 1_800_000
POLL_LIMIT = 20                  // 一次拉多少条历史
CAT_SCHEDULER_POOL_LIMIT = 10
LOG_BUFFER_CAPACITY = 200
LINKER_LOG_CAPACITY = 150
LOGS_RESPONSE_LIMIT = 500
LOGS_DEFAULT_LIMIT = 200
PROCESSING_TIMEOUT_MARGIN_MS = 10_000

public static long processingTimeoutMs()    // mergeWindowMs + MARGIN
```

**`WebCorsConfig`** ✅：`/api/**` 允许任意来源，无凭据。

### `common/utils/`

| 类 | 状态 | 方法 |
|---|---|---|
| `SnowflakeId` | ✅ | `long next()` / `String nextString()`；时钟回拨 > 3s 拒绝发号 |
| `JsonCodec` | ✅ | `write` / `writePretty` / `read(String)` / `read(String, Class)` |
| `CatScheduler` | ✅ | `schedule(String name, long period, Runnable task)`；单任务异常不影响后续调度 |
| `ForwardExpander` | ⬜ | 空类 |
| `MediaSupport` | ⬜ | 空类 |
| `TextSupport` | ⬜ | 空类 |
| `TimeSupport` | ⬜ | 空类 |

### `common/enums/`（22 个）

| 枚举 | 取值 |
|---|---|
| `BasicEmotion` | `JOY, SADNESS, ANGER, FEAR, DISGUST, SURPRISE, TRUST, ANTICIPATION` |
| `ChannelKind` | `INTERNAL_DATE, INTERNAL_DESIRE, ADD_DATE, EXTERNAL_QQ`；`getFamily()` / `wire()` / `fromWire()` |
| `ContentKind` | `TEXT, FILE, COORDINATE, MULTI` |
| `EnvironmentKind` | `STATIC_HOME, STATIC_OFFICE, ESP32` |
| `FileKind` | `IMAGE, AUDIO, VIDEO, FILE` |
| `IntentKind` | `NEVER_INITIATE, DESIRE_V1` |
| `LongTermMemoryKind` | `FACT, EVENT, EMOTION, RELATION, PREFERENCE, PROCEDURE` |
| `MemoryKind` | `SOUL_MD, FISH_MEMORY, LONG_SHORT_TERM, NONE` |
| `MissedPolicyKind` | `BOOST_NEED, CATCH_UP, DROP_LOG, IGNORE` |
| `NeedKind` | 15 个马斯洛需求；`v1()` 返回首版启用的 5 个 |
| `ObserveKind` | `OFF, SHORT_TERM, AGGREGATED` |
| `PhysiologyKind` | `NONE, FEMALE_CYCLE, ENERGY_ONLY` |
| `PresetKind` | `ADVISOR, BLACK_CAT, NULL` |
| `ProcessorKind` | 8 个，各带 `order` 与 `family` |
| `ScheduleKind` | `ONCE, RECURRING` |
| `ShortTermEntryKind` | `USER_SAID, I_SAID, I_ASKED, I_DID, SENSED` |
| `SuperegoAction` | `FORBID, REQUIRE, SOFTER` |
| `ThinkingPath` | `REFLEX, DELIBERATION` |
| `TimeAnchor` | `WALL_CLOCK, ARCHIVE` |
| `TraitKind` | 大五人格 5 维 |
| `UnderstandKind` | `BASIC_RULES` |
| `Urgency` | `HIGH, NORMAL, LOW, UNKNOWN` |

**`ChannelKind` 的三个家族**：

| 家族 | 成员 | 含义 |
|---|---|---|
| `INTERNAL` | `INTERNAL_DATE`、`INTERNAL_DESIRE` | 服务器自己产生的，不进 poller |
| `ADD` | `ADD_DATE` | 大模型回复时产生的，从跳板进来 |
| `EXTERNAL` | `EXTERNAL_QQ` | 外部连接产生的，进出 poller |

> `NeedKind.wire()` / `fromWire()` 与 `TraitKind.wire()` / `fromWire()` 目前**返回 null**（TODO）。

---

## 文档地图

| 想知道 | 看哪个 |
|---|---|
| 系统长什么样、术语是什么 | [`架构总览.md`](./架构总览.md) |
| **每个类有哪些字段和方法**（本文：Java 侧） | `接口文档-cat.md` |
| 跳板侧接口 | [`接口文档-跳板.md`](./接口文档-跳板.md) |
| 包和类怎么摆、谁依赖谁 | [`图-类关系图.md`](./图-类关系图.md) |
| 一条消息的数据怎么变 | [`图-数据流图.md`](./图-数据流图.md) |
| 谁调谁、什么顺序 | [`图-时序图.md`](./图-时序图.md) |
| 下一步写什么 | [`开发计划-短期.md`](./开发计划-短期.md) |
