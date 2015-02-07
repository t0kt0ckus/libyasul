#!/bin/sh
#
# yasul: script to build all distributables.
#

#############################################################################
# Android SDK configuration
# Warning: ANDROID_SDK/ANDROID_NDK environment variables must be set properly
#
#
ANDROID_SDK_LEVEL=19 
ANDROID_JAR=$ANDROID_SDK/platforms/android-$ANDROID_SDK_LEVEL/android.jar
#
ANDROID_BUILDTOOLS_LEVEL=19.1.0
ANDROID_DX=$ANDROID_SDK/build-tools/$ANDROID_BUILDTOOLS_LEVEL/dx


#############################################################################
# SHOULD NOT EDIT BELLOW
#
PROJECT_DIR=`pwd`
TMP_DIR=$PROJECT_DIR/tmp
rm -rf $TMP_DIR
mkdir -p $TMP_DIR
DIST_DIR=$PROJECT_DIR/dist
rm -rf $DIST_DIR
mkdir -p $DIST_DIR
YASUL_JAR="${DIST_DIR}/yasul.jar"

# native libraries
#
YASUL_JNI_DIR="${PROJECT_DIR}/jni"
cd $YASUL_JNI_DIR
ndk-build clean
ndk-build
cd $PROJECT_DIR
cp -R libs $TMP_DIR/lib

# Java API
#
YASUL_JAVA_DIR="${PROJECT_DIR}/java"
CC_FLAGS="-classpath ${ANDROID_JAR}"
cd $YASUL_JAVA_DIR
find . -name "*.java" -print | xargs javac $CC_FLAGS -d $TMP_DIR
cd $PROJECT_DIR

# yasul.jar
#
cd $TMP_DIR
zip -r $YASUL_JAR .
cd $PROJECT_DIR
echo "Yasul library is ready: ${YASUL_JAR}" 

################### Documentation ###################
#
EXT_JLINKS="-link http://docs.oracle.com/javase/7/docs/api/ -link http://d.android.com/reference/"
APIDOC_DIR=$DIST_DIR/api
rm -rf $APIDOC_DIR
mkdir -p $APIDOC_DIR
javadoc $EXT_JLINKS -d $APIDOC_DIR -quiet -windowtitle Yasul -public -author -sourcepath $YASUL_JAVA_DIR -classpath $ANDROID_JAR org.openmarl.yasul

################### clean tmp objects ###################
rm -rf $TMP_DIR
rm -rf $PROJECT_DIR/libs
rm -rf $PROJECT_DIR/obj

