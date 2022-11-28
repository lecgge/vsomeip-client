package com.example.vsomeip

import android.util.Log

class VSomeIPClient(val appName: String,val listener: IClientListener) {
    init {
        create(appName,this)
    }

    private val startTask: StartTask =
        StartTask(this)


    companion object {
        var TAG = "SomeIPClient"

        init {
            System.loadLibrary("ClientJNI")
        }
    }


    @JvmName("getAppName1")
    fun getAppName(): String {
        return appName
    }

    fun initialize(): Boolean {
        return init()
    }

    /*启动客户端，启动run线程*/
    fun startClient() {
        startTask.run()
    }

    /*停止客户端*/
    fun stopClient() {
        stop()
        close()
    }

    /*请求服务*/
    fun requestService(serviceId: Int, instanceId: Int) {
        request_service(serviceId, instanceId)
    }

    /*请求事件*/
    fun requestEvent(serviceId: Int, instanceId: Int, eventId: Int, groupId: Int,isReliable: Boolean) {
        request_event(serviceId, instanceId, eventId, groupId,isReliable)
    }

    /*发送请求消息*/
    fun sendRequest(serviceId: Int, instanceId: Int, methodId: Int, payload: ByteArray?,isReliable: Boolean) {
        send_request(serviceId, instanceId, methodId, payload!!,isReliable)
    }

    /*订阅事件组*/
    fun subscribeEventGroup(serviceId: Int, instanceId: Int, groupId: Int) {
        subscribe_eventgroup(serviceId, instanceId, groupId)
    }

    /*取消订阅事件组*/
    fun unsubscribeEventGroup(serviceId: Int, instanceId: Int, groupId: Int) {
        unsubscribe_eventgroup(serviceId, instanceId, groupId)
    }

    /*订阅一个事件*/
    fun subscribeEvent(serviceId: Int, instanceId: Int, groupId: Int, eventId: Int) {
        subscribe_event(serviceId, instanceId, groupId, eventId)
    }

    fun onAvailability(serviceId: Int, instanceId: Int, isAvailability: Boolean) {
        // 暂时不处理，直接回调给用户监听
        listener.onAvailability(serviceId, instanceId, isAvailability)
    }

    fun onMessage(serviceId: Int, instanceId: Int, methodId: Int, payload: ByteArray?) {
        // 暂时不处理，直接回调给用户监听
        listener.onMessage(serviceId, instanceId, methodId, payload)
    }

    private external fun create(appName: String, client: VSomeIPClient)
    private external fun init(): Boolean
    private external fun request_service(serviceId: Int, instanceId: Int)
    external fun start()
    private external fun stop()
    private external fun close()
    private external fun send_request(
        serviceId: Int,
        instanceId: Int,
        methodId: Int,
        payload: ByteArray,
        boolean:Boolean
    )

    private external fun request_event(
        serviceId: Int,
        instanceId: Int,
        eventId: Int,
        eventGroupId: Int,
        isReliable: Boolean
    )

    private external fun subscribe_eventgroup(serviceId: Int, instanceId: Int, eventGroupId: Int)
    private external fun unsubscribe_eventgroup(serviceId: Int, instanceId: Int, eventGroupId: Int)
    private external fun subscribe_event(
        serviceId: Int,
        instanceId: Int,
        eventGroupId: Int,
        eventId: Int
    )



    // 客户端对外接口
    interface IClientListener {
        fun onMessage(serviceId: Int, instanceId: Int, methodId: Int, payload: ByteArray?)
        fun onAvailability(serviceId: Int, instanceId: Int, isAvailability: Boolean)
    }

    // 开启客户顿
    internal class StartTask(c: VSomeIPClient) : Thread() {
        private val client: VSomeIPClient
        override fun run() {
            try {
                client.start()
            } catch (e: RuntimeException) {
                Log.i(
                    TAG,
                    "caught RuntimeException during StartTask(): " + e.message
                )
            } finally {
            }
        }

        init {
            client = c
        }
    }
}