# FlashLingo

程序员英语词汇背诵应用 | English Vocabulary Flashcard App for Programmers

## 功能特点

- **闪卡学习**: 先看单词，再看释义，点击翻转
- **测试巩固**: 四选一测试，连续答对2次视为掌握
- **智能复习**: 根据遗忘曲线安排复习，答错自动加入错词本
- **收藏管理**: 收藏喜欢的单词，方便复习
- **每日统计**: 追踪学习进度、正确率和连续学习天数
- **深色模式**: 支持浅色/深色主题切换

## 技术栈

| 技术 | 版本 |
|------|------|
| Kotlin | 2.0.0 |
| Jetpack Compose | Material3 |
| Room | 数据库 |
| Hilt | 依赖注入 |
| Retrofit + OkHttp | 网络 |
| MVVM | 架构 |

最低 Android 版本: API 24 (Android 7.0)

## 构建

```bash
# 环境要求
export JAVA_HOME=/opt/homebrew/opt/openjdk@21

# Debug 构建
make debug

# Release 构建
make release

# 安装到设备
make install

# 代码检查
make lint
```

## 项目结构

```
app/
├── src/main/java/com/english/flashcard/
│   ├── data/           # 数据层 (Room, Retrofit)
│   ├── domain/         # 领域层 (Repository interfaces, UseCases)
│   └── ui/             # UI层 (Compose screens, ViewModels)
├── src/main/assets/
│   └── database/       # 预置词库 (words.db)
└── src/main/res/       # 资源文件
```

## 学习流程

1. **新词**: 闪卡初识 → 测试巩固
2. **复习**: 错词/待复习词 → 测试模式
3. **掌握判定**: 连续答对 2 次 → isMastered = true
4. **错词移除**: 错词连续答对 3 次 → 移出错词本

## 词库

内置词库位于 `app/src/main/assets/database/words.db`，来源为 JSON 文件 `data/BeiShiGaoZhong_1.json`。

词库数据来自 [english-vocabulary](https://github.com/KyleBing/english-vocabulary)，感谢贡献。

重新转换词库: `python3 scripts/convert_to_sqlite.py`

## License

MIT
