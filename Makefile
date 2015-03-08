#############################################################################
# Android SDK configuration
# Warning: ANDROID_SDK/ANDROID_NDK environment variables must be set properly
#
#
ANDROID_SDK_LEVEL=19
ANDROID_JAR=$(ANDROID_SDK)/platforms/android-$(ANDROID_SDK_LEVEL)/android.jar
#
ANDROID_BUILDTOOLS_LEVEL=19.1.0
ANDROID_DX=$(ANDROID_SDK)/build-tools/$(ANDROID_BUILDTOOLS_LEVEL)/dx


#############################################################################
# SHOULD NOT EDIT BELLOW
#
TMP_DIR=tmp
DIST_DIR=dist
NATIVE_SRC_DIR=jni
JAVA_SRC_DIR=java

# javadoc
JAVA_API_DIST_DIR=$(DIST_DIR)/api

javadoc:
	mkdir -p $(JAVA_API_DIST_DIR)
	rm -rf $(JAVA_API_DIST_DIR)/*
	javadoc -d $(JAVA_API_DIST_DIR) -quiet -windowtitle Yasul -public -author -sourcepath $(JAVA_SRC_DIR) -classpath $(ANDROID_JAR) org.openmarl.yasul


