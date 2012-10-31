#!/bin/sh
# Superpowers/build.sh
# Compiles third party libraries using autotools for Android
# Make sure $NDK is defined in your environment

ANDROID_PLATFORM_VERSION="14"
ANDROID_ARCH="arm"
NDK_TOOLCHAIN_VERSION="4.6"
NDK_TOOLCHAIN_HOST="$ANDROID_ARCH-linux-androideabi"
ANDROID_TOOLCHAIN="$NDK_TOOLCHAIN_HOST-$NDK_TOOLCHAIN_VERSION"
export PATH="$NDK/toolchains/$ANDROID_TOOLCHAIN/prebuilt/darwin-x86/bin/:$PATH"
export SYS_ROOT="$NDK/platforms/android-$ANDROID_PLATFORM_VERSION/arch-$ANDROID_ARCH/"
export CC="arm-linux-androideabi-gcc --sysroot=$SYS_ROOT"
export LD="arm-linux-androideabi-ld"
export AR="arm-linux-androideabi-ar"
export RANLIB="arm-linux-androideabi-ranlib"
export STRIP="arm-linux-androideabi-strip"

# FFTW dirs
FFTW_INSTALL_DIR="`pwd`/jni/fftw3"
FFTW_SRC_DIR="`pwd`/../fftw-3.3.2"

# Configure and make FFTW
cd $FFTW_SRC_DIR
mkdir -p $FFTW_INSTALL_DIR
./configure  --host=$NDK_TOOLCHAIN_HOST --build=i386-apple-darwin10.8.0 \
             --prefix=$FFTW_INSTALL_DIR LIBS="-lc -lgcc" CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=neon" \
             --disable-fortran --enable-shared
make
make install

exit 0
