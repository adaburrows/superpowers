Compiling the binary
--------------------

Add the following to your `~/.profile` or `~/.bashrc` with paths appropriate to where you've installed the Android SDK and Android NDK:

		export ANDROID_SDK=/home/dann/Applications/android-sdk-linux
		export NDK=/opt/home/dann/Applications/android-ndk-r8b
		export ANDROID_NDK=$NDK
		export ANDROID_NATIVE_API_LEVEL=android-16
		export PATH=$PATH:$ANDROID_SDK/tools:$ANDROID_SDK/platform-tools

Then update the project:

		android update project -p .

And build:

		ant debug

Finally, push your build to your phone to test:

		adb install bin/Superpowers-debug.apk

To see logging data:

		adb logcat

