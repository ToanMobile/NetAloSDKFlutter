package com.netacom.flutter_sdk

import android.os.Bundle
import androidx.annotation.NonNull
import com.netacom.base.chat.logger.Logger
import com.netacom.full.ui.sdk.NetAloSDK
import com.netacom.lite.define.ErrorCodeDefine
import com.netacom.lite.define.GalleryType
import com.netacom.lite.define.NavigationDef
import com.netacom.lite.entity.ui.local.LocalFileModel
import com.netacom.lite.entity.ui.user.NeUser
import com.netacom.lite.util.CallbackResult
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : FlutterActivity() {
    private var currentResult: MethodChannel.Result? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Click notification open chat
        intent.extras?.getParcelable<NeUser>(NavigationDef.ARGUMENT_NEUSER)?.let { neUser ->
            NetAloSDK.openNetAloSDK(
                    this@MainActivity,
                    NeUser(
                            id = neUser.id,
                            token = neUser.token ?: "",
                            username = neUser.username ?: ""
                    )
            )
        }
        //Event SDK
        CoroutineScope(Dispatchers.Default).launch {
            NetAloSDK.netAloEvent?.let { event ->
                //Gallery
                event.receive<ArrayList<LocalFileModel>>().collect { listPhoto ->
                    Logger.e("SELECT_PHOTO_VIDEO==$listPhoto")
                    currentResult?.success(listPhoto)
                }

                event.receive<LocalFileModel>().collect { document ->
                    Logger.e("SELECT_FILE==$document")
                }
                //Notification
                event.receive<Map<String, String>>().collect { notification ->
                    Logger.e("Notification:data==$notification")
                }
                //Event
                event.receive<Int>().collect { errorEvent ->
                    Logger.e("Event:==$errorEvent")
                    when (errorEvent) {
                        ErrorCodeDefine.ERRORCODE_FAILED_VALUE -> {
                            Logger.e("Event:Socket error")
                        }
                        ErrorCodeDefine.ERRORCODE_EXPIRED_VALUE -> {
                            Logger.e("Event:Session expired")
                        }
                        ErrorCodeDefine.ERRORCODE_LOGIN_CONFLICT_VALUE -> {
                            Logger.e("Event:Login conflict")
                        }
                    }
                }
            }
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        PlatformChannel.instance.init(flutterEngine.dartExecutor.binaryMessenger)
        PlatformChannel.instance.receiveChannel.setMethodCallHandler { call, result ->
            currentResult = result
            when (call.method) {
                "openChatConversation" -> openChatConversation(call, result)
                "openChatWithUser" -> openChatWithUser(call, result)
                "setNetaloUser" -> setNetaloUser(call, result)
                "pickImages" -> openImagePicker(call, result)
                "blockUser" -> blockUser(call, result, isBlock = true)
                "unBlockUser" -> blockUser(call, result, isBlock = false)
            }
        }
    }

    private fun openImagePicker(call: MethodCall, result: MethodChannel.Result) {
        val type = when (call.argument("type") as? Int ?: 1) {
            1 -> GalleryType.GALLERY_ALL
            2 -> GalleryType.GALLERY_PHOTO
            3 -> GalleryType.GALLERY_VIDEO
            else -> GalleryType.GALLERY_ALL
        }
        NetAloSDK.openGallery(
                context = this,
                maxSelections = call.argument("maxImages") as? Int ?: 1,
                autoDismissOnMaxSelections = call.argument("autoDismissOnMaxSelections") ?: true,
                galleryType = type
        )
    }

    private fun blockUser(call: MethodCall, result: MethodChannel.Result, isBlock: Boolean) {
        NetAloSDK.blockUser(
                userId = call.arguments as? Long ?: 0,
                isBlock = isBlock,
                callbackResult = object : CallbackResult<Boolean> {
                    override fun callBackError(error: String?) {
                        result.success(false)
                    }

                    override fun callBackSuccess(isSuccess: Boolean) {
                        result.success(isSuccess)
                    }
                }
        )
    }

    private fun setNetaloUser(call: MethodCall, result: MethodChannel.Result) {
        NetAloSDK.setNetAloUser(NeUser(
                id = call.argument("id") as? Long ?: 0,
                token = call.argument("token") as? String ?: "",
                username = call.argument("username") ?: "",
                avatar = call.argument("avatar") as? String ?: ""
        ))
        result.success(true)
    }

    private fun openChatConversation(call: MethodCall, result: MethodChannel.Result) {
        NetAloSDK.openNetAloSDK(this)
        result.success(true)
    }

    private fun openChatWithUser(call: MethodCall, result: MethodChannel.Result) {
        val target: HashMap<String, Any> = call.argument("target")!!
        NetAloSDK.openNetAloSDK(
                this,
                NeUser(
                        id = target["id"] as? Long ?: 0,
                        token = target["token"] as? String ?: "",
                        username = target["username"] as? String ?: "",
                        avatar = target["avatar"] as? String ?: ""
                )
        )
        result.success(true)
    }
}
