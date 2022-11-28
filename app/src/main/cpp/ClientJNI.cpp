#include <jni.h>
#include <string>
#include <unistd.h>
#include <iostream>
#include <iomanip>
#include "include/context.h"
#include "include/NativeLog.h"

using namespace std;

std::shared_ptr<context> g_ctx;
std::condition_variable condition;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_vsomeip_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}



//查看类的方法签名： javap -s com.lxl.someiplib.SomeIPClient
void on_message(const std::shared_ptr<vsomeip::message> &_message) {

    LOGD("in message");
    int status;
    JNIEnv *env = NULL;
    status = g_ctx->vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        status = g_ctx->vm->AttachCurrentThread(&env, NULL);
        if (status < 0) {
            env = NULL;
            return;
        }
    }

    jclass clz = env->GetObjectClass(g_ctx->app_object);
    if (clz == nullptr) { return; }
    //获取要回调的方法
    jmethodID jMethodId = env->GetMethodID(clz, "onMessage", "(III[B)V");
    if (jMethodId == nullptr) { return; }

    // 获取消息信息
    int serviceId = _message->get_service();
    int instanceId = _message->get_instance();
    int methodId = _message->get_method();
    std::shared_ptr<vsomeip::payload> its_payload = _message->get_payload();
    // 转jbyteArray
    jbyte *by = (jbyte *) its_payload->get_data();
    int dataSize = its_payload->get_length();
    jbyteArray jarray = env->NewByteArray(its_payload->get_length());

    env->SetByteArrayRegion(jarray, 0, dataSize, by);
    env->CallVoidMethod(g_ctx->app_object, jMethodId, serviceId, instanceId, methodId, jarray);

    env->DeleteLocalRef(clz);
    g_ctx->vm->DetachCurrentThread();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_create(JNIEnv *env, jobject thiz, jstring app_name,
                                              jobject client) {
    // 初始化context
    g_ctx = std::make_shared<context>();
    env->GetJavaVM(&g_ctx->vm);

    const char *appNameConverted = env->GetStringUTFChars(app_name, 0);
    char *appNameCopied = (char *) malloc(sizeof(char) * (strlen(appNameConverted) + 1));
    strcpy(appNameCopied, appNameConverted);
    g_ctx->app = vsomeip::runtime::get()->create_application(appNameConverted);
    g_ctx->app_name = appNameCopied;

    //生成一个全局引用，回调的时候findclass才不会为null
    jobject callback = env->NewGlobalRef(client);
    g_ctx->app_object = callback;
    // 释放资源
    env->ReleaseStringUTFChars(app_name, appNameConverted);
}

void on_availability(int serviceId, int instanceId, bool is_available) {

    if (is_available) {
        LOGD("CLIENT: Service [%d.%d] 是 available", serviceId, instanceId);
    } else {
        LOGD("CLIENT: Service [%d.%d] 是 NOT available.", serviceId, instanceId);
    }
    if (is_available) {
        condition.notify_one();
//        g_ctx->app->subscribe(serviceId, instanceId, 0x2000);
    }

    int status;
    JNIEnv *env = NULL;
    status = g_ctx->vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        status = g_ctx->vm->AttachCurrentThread(&env, NULL);
        if (status < 0) {
            env = NULL;
            return;
        }
    }

    jclass clz = env->GetObjectClass(g_ctx->app_object);
    if (clz == nullptr) { return; }
    //获取要回调的方法
    jmethodID jMethodId = env->GetMethodID(clz, "onAvailability", "(IIZ)V");
    if (jMethodId == nullptr) { return; }
    if (is_available) {
        env->CallVoidMethod(g_ctx->app_object, jMethodId, serviceId, instanceId, JNI_TRUE);
    } else {
        env->CallVoidMethod(g_ctx->app_object, jMethodId, serviceId, instanceId, JNI_FALSE);
    }
    env->DeleteLocalRef(clz);
    g_ctx->vm->DetachCurrentThread();


}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_vsomeip_VSomeIPClient_init(JNIEnv *env, jobject thiz) {
    bool ret = g_ctx->app->init();
    return ret;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_request_1service(JNIEnv *env, jobject thiz, jint service_id,
                                                        jint instance_id) {
    g_ctx->app->request_service(service_id, instance_id);
    g_ctx->app->register_availability_handler(service_id, instance_id, on_availability);
    g_ctx->app->register_message_handler(service_id, instance_id, vsomeip::ANY_METHOD, on_message);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_start(JNIEnv *env, jobject thiz) {
    g_ctx->app->start();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_stop(JNIEnv *env, jobject thiz) {
    g_ctx->app->stop();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_close(JNIEnv *env, jobject thiz) {
    // 释放资源
    env->DeleteGlobalRef(g_ctx->app_object);
    free(g_ctx->app_name);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_send_1request(JNIEnv *env, jobject thiz, jint service_id,
                                                     jint instance_id, jint method_id,
                                                     jbyteArray payload,jboolean is_relia) {
    // 构建请求消息
    std::shared_ptr<vsomeip::message> request = vsomeip::runtime::get()->create_request();
    request->set_service(service_id);
    request->set_instance(instance_id);
    request->set_method(method_id);
    request->set_reliable(is_relia);
    // 填充Payload
    std::shared_ptr<vsomeip::payload> its_payload = vsomeip::runtime::get()->create_payload();
    jbyte *bBuffer = env->GetByteArrayElements(payload, 0);
    int payloadLen = env->GetArrayLength(payload);
    std::unique_ptr<vsomeip::byte_t[]> its_data(new vsomeip::byte_t[payloadLen]());
    memcpy(its_data.get(), bBuffer, payloadLen);
    its_payload->set_data(its_data.get(), payloadLen);
    request->set_payload(its_payload);
    // 发送请求
    g_ctx->app->send(request);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_request_1event(JNIEnv *env, jobject thiz, jint service_id,
                                                      jint instance_id, jint event_id,
                                                      jint event_group_id,jboolean isreliable) {
    std::set<vsomeip::eventgroup_t> its_groups;
    its_groups.insert(event_group_id);
    if(isreliable){
        g_ctx->app->request_event(service_id, instance_id, event_id, its_groups,
                                  vsomeip_v3::event_type_e::ET_EVENT,
                                  vsomeip_v3::reliability_type_e::RT_RELIABLE);
    } else{
        g_ctx->app->request_event(service_id, instance_id, event_id, its_groups,
                                  vsomeip_v3::event_type_e::ET_EVENT,
                                  vsomeip_v3::reliability_type_e::RT_UNRELIABLE);
    }


}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_subscribe_1eventgroup(JNIEnv *env, jobject thiz,
                                                             jint service_id, jint instance_id,
                                                             jint event_group_id) {
    g_ctx->app->subscribe(service_id, instance_id, event_group_id);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_unsubscribe_1eventgroup(JNIEnv *env, jobject thiz,
                                                               jint service_id, jint instance_id,
                                                               jint event_group_id) {
    g_ctx->app->unsubscribe(service_id, instance_id, event_group_id);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vsomeip_VSomeIPClient_subscribe_1event(JNIEnv *env, jobject thiz, jint service_id,
                                                        jint instance_id, jint event_group_id,
                                                        jint event_id) {
    g_ctx->app->subscribe(service_id, instance_id, event_group_id, vsomeip_v3::ANY_MAJOR, event_id);
}
