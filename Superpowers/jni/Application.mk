# Superpowers/jni/Application.mk
APP_ABI := armeabi-v7a
APP_STL := gnustl_static
APP_CPPFLAGS := -frtti -fexceptions
APP_MODULES := fftw3
APP_CFLAGS += -march=armv7-a -mfloat-abi=softfp -mfpu=neon