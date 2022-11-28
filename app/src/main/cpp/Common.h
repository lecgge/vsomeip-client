#ifndef VSOMEIP_COMMON_H
#define VSOMEIP_COMMON_H

#include <vsomeip/vsomeip.hpp>

#define SERVICE_ID  0x0001  //服务ID
#define INSTANCE_ID 0x0001  //服务实例ID

#define METHOD_ID_FIELD_SET   0x1000   //属性SET方法ID
#define METHOD_ID_FIELD_GET   0x1001   //属性GET方法ID
#define EVENT_ID_FIELD_NOTIFY 0x2000   //属性事件ID

#define METHOD_ID_FUNC_CALL    0x1002  //客户端调用服务端方法ID
#define EVENT_ID_ONLY_NOTIFY_0 0x2001  //服务端事件0
#define EVENT_GROUP_ID_1  0x0001 //事件组1

//打印日志
#include <android/log.h>
#define TAG "vsomeip-test"
#define LOGV(format, args...)  __android_log_print(ANDROID_LOG_VERBOSE, TAG, "[%s:%d]" format, basename(__FILE__), __LINE__, ##args)
#define LOGD(format, args...)  __android_log_print(ANDROID_LOG_DEBUG,   TAG, "[%s:%d]" format, basename(__FILE__), __LINE__, ##args)
#define LOGI(format, args...)  __android_log_print(ANDROID_LOG_INFO,    TAG, "[%s:%d]" format, basename(__FILE__), __LINE__, ##args)
#define LOGW(format, args...)  __android_log_print(ANDROID_LOG_WARN,    TAG, "[%s:%d]" format, basename(__FILE__), __LINE__, ##args)
#define LOGE(format, args...)  __android_log_print(ANDROID_LOG_ERROR,   TAG, "[%s:%d]" format, basename(__FILE__), __LINE__, ##args)

#endif //VSOMEIP_COMMON_H
