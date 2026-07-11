#!/system/bin/sh
# Build script — 需要 Android SDK 环境
# 用法：在 PC/Mac 上装好 Android Studio 后，把项目导入就能编译
# 或者把 mimocode 二进制放进 app/src/main/assets/ 后用此脚本在 CI 中编译

echo "MiMoCode APK Build"
echo ""
echo "需要准备："
echo "  1. 下载 mimocode-linux-arm64 放到 app/src/main/assets/mimocode"
echo "  2. 安装 Android SDK（Android Studio）"
echo "  3. 用 Android Studio 打开本目录，Build → Build APK"
echo ""
echo "或者命令行："
echo "  export ANDROID_HOME=/path/to/sdk"
echo "  ./gradlew assembleRelease"
echo ""
echo "APK 输出在 app/build/outputs/apk/release/"
