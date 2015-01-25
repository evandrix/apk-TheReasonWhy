#!/bin/bash -ue

TARGET_LANG=${1:-zh}

CURRENT_LANG=$([ "${TARGET_LANG}" == "en" ] && echo "zh" || echo "en")
CURRENT_PKG_NAME=$([ "${TARGET_LANG}" == "en" ] && echo "wei.he.wo.xin" || echo "the.reason.why")
TARGET_PKG_NAME=$([ "${TARGET_LANG}" == "en" ] && echo "the.reason.why" || echo "wei.he.wo.xin")

echo "Language: ${CURRENT_LANG} => ${TARGET_LANG} / ${CURRENT_PKG_NAME} -> ${TARGET_PKG_NAME}"
#read -p "Language: ${CURRENT_LANG} => ${TARGET_LANG}: Are you sure? [y/N] " -n 1 -r
#if [[ $REPLY =~ ^[Yy]$ ]]; then
# Java code import statements
FILES="src/com/google/android/vending/expansion/downloader/Helpers.java
src/com/google/android/vending/expansion/downloader/impl/DownloadNotification.java
src/com/google/android/vending/expansion/downloader/impl/V14CustomNotification.java
src/com/google/android/vending/expansion/downloader/impl/V3CustomNotification.java
src/the/reason/why/activity/BookListActivity.java
src/the/reason/why/activity/LangSelActivity.java
src/the/reason/why/activity/MainActivity.java
src/the/reason/why/activity/TocListActivity.java
src/the/reason/why/view/BannerView.java
src/the/reason/why/view/BookAdapter.java
src/the/reason/why/view/TocAdapter.java"
for file in $FILES; do
	sed -i'' "s/import ${CURRENT_PKG_NAME}.R;/import ${TARGET_PKG_NAME}.R;/g" ${file}
done

# /AndroidManifest.xml
# package name
sed -i'' "s|package=\"${CURRENT_PKG_NAME}\"|package=\"${TARGET_PKG_NAME}\"|g" AndroidManifest.xml
# app icon
sed -i'' "s|android:icon=\"@drawable/icon\"|android:icon=\"@drawable/icon_${TARGET_LANG}\"|g" AndroidManifest.xml
sed -i'' "s|android:icon=\"@drawable/icon_${CURRENT_LANG}\"|android:icon=\"@drawable/icon_${TARGET_LANG}\"|g" AndroidManifest.xml

# /res/values/strings.xml
DISPLAY_TEXT=$([ "${TARGET_LANG}" == "en" ] && echo "The Reason Why" || echo "为何我信")
sed -i'' -E "s|<string name=\"app_lang\">[^<]+</string>|<string name=\"app_lang\">${TARGET_LANG}</string>|g" res/values/strings.xml
for field in "app_name" "titlebar_text"; do
	sed -i'' -E "s|<string name=\"${field}\">[^<]+</string>|<string name=\"${field}\">${DISPLAY_TEXT}</string>|g" res/values/strings.xml
done

# /.obb
OBB_FILE=$(ls -lrt | awk '/main\.[0-9]+\..*\.obb$/ { f=$NF };END{ print f }')
mv -nf ${OBB_FILE} ${OBB_FILE//$CURRENT_PKG_NAME/$TARGET_PKG_NAME} >/dev/null 2>&1
#fi
