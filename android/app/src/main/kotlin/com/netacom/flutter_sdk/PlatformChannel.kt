package com.netacom.flutter_sdk

import com.netacom.lite.entity.ui.user.NeUser
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

class PlatformChannel {
    private lateinit var sendChannel: MethodChannel
    private val sendChannelName = "callbacks"
    lateinit var receiveChannel: MethodChannel
    private val receiveChannelName = "vn.netacom.sdk/flutter_channel"

    companion object {
        var instance = PlatformChannel()
    }

    fun init(binaryMessenger: BinaryMessenger) {
        sendChannel = MethodChannel(binaryMessenger, sendChannelName)
        receiveChannel = MethodChannel(binaryMessenger, receiveChannelName)
    }

    fun sendEventNetAloSessionExpire() {
        sendChannel.invokeMethod("netAloSessionExpire", null)
    }

    fun sendEventNetAloPushNotification(data: Map<String, String>) {
        sendChannel.invokeMethod("netAloPushNotification", data)
    }

    fun sendEventNetAloChatPushNotification(data: NeUser) {
        sendChannel.invokeMethod("netAloChatPushNotification", data)
    }
}