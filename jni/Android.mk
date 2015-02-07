LOCAL_PATH := $(call my-dir)

# libyasul: 
##
include $(CLEAR_VARS)
LOCAL_MODULE := yasul
LOCAL_SRC_FILES := log.c buf.c ostools.c session.c pthout.c ptherr.c kworker.c yasul.c yasul_jni.c
LOCAL_CFLAGS := -g -Wall
include $(BUILD_SHARED_LIBRARY)

# test: 
##
include $(CLEAR_VARS)
LOCAL_MODULE := test
LOCAL_SRC_FILES := main.c
LOCAL_CFLAGS := -g -Wall 
LOCAL_SHARED_LIBRARIES := yasul 
include $(BUILD_EXECUTABLE)

