# Epic 库存 (Epic Inventory)

独立第三方 Android 应用，展示 Epic Games 账号的游戏库存、自动领取周免游戏、统计成就、提供桌面小组件。

## 关键原则
- ❌ **不修改 / 不打包 / 不重新分发** Epic 官方 APK
- ✅ 只调用 Epic 公开 API：OAuth、Library、Catalog、Achievements、FreePromotions
- ✅ Token 用 `EncryptedSharedPreferences` (AES-256, AndroidKeyStore) 本地加密存储
- ✅ 不运营任何后端服务器
- ✅ 完全开源，可审计

## 功能 (全部实现)
- [x] Epic OAuth 登录 (WebView) + Token 本地加密
- [x] 游戏库存展示 + 搜索
- [x] 统计 (总数 / 已安装 / 移动端支持 / 有定价)
- [x] 周免费游戏提醒 (WorkManager + 推送)
- [x] 自动领取免费游戏
- [x] 成就统计 (解锁率、稀有度排行、年度分布、最近解锁)
- [x] 明暗主题切换 (DataStore 持久化，支持系统/浅/深)
- [x] 桌面 Widget (Glance, 可点击跳 App)
- [x] **多账号管理** (切换 / 删除 / 加密保存)
- [x] **About 屏幕** + 隐私政策 + 跳转 Epic ToS
- [x] 自适应图标 (Adaptive Icon, 支持 Android 13+ themed icon)
- [x] ProGuard / R8 release 混淆
- [x] GitHub Actions CI (lint + test + build APK)
- [x] 单元测试 (JUnit4)

## 项目结构
```
epic-inventory-android/
├── .github/workflows/android.yml        # CI
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/epicinv/
│       │   │   ├── EpicInventoryApp.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── EpicRepository.kt
│       │   │   │   ├── api/EpicApi.kt
│       │   │   │   ├── auth/TokenStore.kt        # 多账号加密
│       │   │   │   ├── freegame/FreeGameRepository.kt
│       │   │   │   ├── stats/StatsRepository.kt
│       │   │   │   └── model/{Library, Catalog}.kt
│       │   │   ├── notify/
│       │   │   │   ├── Channels.kt
│       │   │   │   ├── Prefs.kt
│       │   │   │   ├── WeeklyFreeGameWorker.kt
│       │   │   │   └── AutoClaimWorker.kt
│       │   │   ├── ui/
│       │   │   │   ├── AppShell.kt               # 4 Tab + NavHost
│       │   │   │   ├── InventoryScreen.kt
│       │   │   │   ├── InventoryViewModel.kt
│       │   │   │   ├── auth/EpicAuthWebView.kt
│       │   │   │   ├── freegame/FreeGameScreen.kt
│       │   │   │   ├── stats/StatsScreen.kt
│       │   │   │   ├── settings/AccountScreen.kt
│       │   │   │   ├── settings/AboutScreen.kt
│       │   │   │   └── theme/{ThemeRepository, ThemeScreen}.kt
│       │   │   └── widget/InventoryWidget.kt     # Glance
│       │   └── res/
│       │       ├── mipmap-anydpi-v26/ic_launcher.xml
│       │       ├── drawable/ic_launcher_foreground.xml
│       │       ├── values/{themes, strings, ic_launcher_background}.xml
│       │       ├── values-night/themes.xml
│       │       └── xml/network_security_config.xml
│       └── test/
│           └── java/com/example/epicinv/
│               ├── data/api/EpicApiTest.kt
│               └── data/freegame/FreeGameItemTest.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/{gradle-wrapper.jar, gradle-wrapper.properties}
├── LICENSE
├── PRIVACY.md
└── README.md
```

## 编译 & 运行

### 1. 注册 Epic OAuth 应用
1. 打开 https://dev.epicgames.com/portal
2. 创建一个新的 Web 应用，得到 `CLIENT_ID`
3. 允许的 `redirect_uri`: `https://www.epicgames.com/redirect`
4. 勾选 scope: `basic_profile`

### 2. 构建 APK
```bash
cd epic-inventory-android
./gradlew assembleDebug -PEPIC_CLIENT_ID=你的client_id
```

### 3. 安装到设备
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Release (R8 混淆)
```bash
./gradlew assembleRelease -PEPIC_CLIENT_ID=你的client_id
# APK 在 app/build/outputs/apk/release/
```

## CI
每次 push 触发：
- ✅ 编译 Debug APK
- ✅ 单元测试
- ✅ Lint
- ✅ 上传 APK 到 GitHub Artifacts

需要在 GitHub repo 的 Settings → Secrets → Actions 添加 `EPIC_CLIENT_ID`。

## 法律声明
本项目与 Epic Games, Inc. 无任何关联。"Epic Games" 是 Epic Games, Inc. 的商标。
本项目仅使用 Epic 公开 API，请遵守 https://www.epicgames.com/site/en-US/tos

## 隐私
请见 [PRIVACY.md](PRIVACY.md)。
