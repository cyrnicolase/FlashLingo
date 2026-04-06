# 开始测试功能设计

## 概述

在首页新增「开始测试」入口，用户点击后从词库随机抽取指定数量的单词进行测试。测试结果不计入今日学习进度，仅用于检验学习效果。

## 架构设计

### 导航流程

```
HomeScreen → [点击开始测试] → LearningScreen(learningType=Test)
                                        ↓
                              随机抽取N个单词
                                        ↓
                              直接进入Quiz模式
                                        ↓
                              不更新今日学习进度
                                        ↓
                              CompletionScreen → HomeScreen
```

### 新增类型

`Screen.kt` - LearningType 枚举新增 `Test`：
```kotlin
enum class LearningType(val value: String) {
    Today("today"),
    Review("review"),
    WrongWords("wrong"),
    Favorites("favorites"),
    Test("test")  // 新增
}
```

### 配置项

`UserPreferences.kt` 新增配置：
- `test_word_count`: Int，默认 10

### 设置页面

在现有设置页新增「测试词数」选项，与「每日新词数」类似的下拉选择器（5, 10, 15, 20, 30）。

## 功能详情

### 首页入口

在 `HomeScreen.kt` 的 `TodayTaskCard` 或独立 `TestAccessCard` 展示「开始测试」按钮。

### 测试流程 (LearningViewModel)

1. **选词**：随机从 `words` 表抽取 N 个单词（N = test_word_count 配置）
2. **模式**：直接进入 Quiz 模式（跳过 Flashcard 展示）
3. **答题**：与现有 Quiz 逻辑一致，4 选 1
4. **结果记录**：
   - 不更新 `daily_progress`
   - 不更新单词的 `lastReviewAt` / `nextReviewAt`
   - 不更新 `correctStreak` / `wrongCount`
5. **完成页**：显示正确率，点击返回首页

### 复用点

- `LearningScreen` - UI 层不变，通过 state 区分
- `LearningViewModel` - 核心逻辑，新增 Test 模式分支
- `Quiz` 状态类 - 复用现有设计
- `CompletionScreen` - 复用现有完成页

## 文件改动清单

| 文件 | 改动内容 |
|------|----------|
| `Screen.kt` | LearningType 新增 Test |
| `UserPreferences.kt` | 新增 test_word_count |
| `UserPreferencesRepository.kt` | 新增 getTestWordCount() |
| `SettingsViewModel.kt` | 新增 testWordCount 状态和修改逻辑 |
| `SettingsScreen.kt` | 新增测试词数设置项 |
| `HomeScreen.kt` | 新增开始测试入口按钮 |
| `HomeViewModel.kt` | 导航时传递 testWordCount |
| `LearningViewModel.kt` | Test 模式：随机选词、直接Quiz、不更新进度 |
| `NavGraph.kt` | Learning route 新增 testConfig 参数（可选） |
