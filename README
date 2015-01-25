Android project in Eclipse
===
create new project:
	$ android create project --name <proj-name> --path </path/to/proj> --target android-10 --package pkgname.proj --activity MainActivity

available targets:
	$ android list target

generate build.xml:
	$ android update project -p .

build (and install) project:
	$ ant clean
	$ ant debug
	$ ant debug install

/ant.properties:
	key.store=path/to/my.keystore
	key.alias=mykeystore
	key.store.password=password
	key.alias.password=password

	$ ant release (builds APK)

<sdk>/tools/
create virtual device (AVD) for platform:
	$ android create avd -n <name> -t <targetID> --skin WVGA800 or 240x432 -p path/to/my/avd
	targetID: API level (use android list targets to get id)

run AVD:
	$ emulator -avd <name> -partition-size 512
	eg. emulator -avd WVGA800 -scale 96dpi -dpi-device 160

	$ adb wait-for-device

install app to AVD:
	$ ant install
	...or...
	$ adb install /path/to/.apk

launch app:
	$ adb shell am start -a android.intent.action.MAIN -n pkgname.proj/.MainActivity

move AVD:   $ android move avd -n <name>
update AVD: $ android update avd (recompute path to sys img)
delete AVD: $ android delete avd -n <name>

push {main|patch}.obb to device:
	$ adb push main.<versionCode>.<pkg-name>.obb <type>/obb/<package-name>/main.<versionCode>.<pkg-name>.obb
where <type> is one of:
	dev: /Android
	device: /data/media
	emulator: /mnt/shell/emulated
