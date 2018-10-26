package net.granoeste.flutterlinelogin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.api.LineApiClient
import com.linecorp.linesdk.api.LineApiClientBuilder
import com.linecorp.linesdk.auth.LineLoginApi
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo


class FlutterLineLoginPlugin(registrar: Registrar) : MethodCallHandler, EventChannel.StreamHandler, PluginRegistry.ActivityResultListener {

    private val METHOD_CHANNEL = "net.granoeste/flutter_line_login"
    private val EVENT_CHANNEL = "net.granoeste/flutter_line_login_result"
    private var requestCode = 20000904

    companion object {
        val TAG: String = FlutterLineLoginPlugin::class.java.simpleName

        @JvmStatic
        fun registerWith(registrar: Registrar): Unit {
            FlutterLineLoginPlugin(registrar)
        }
    }

    private lateinit var eventSink: EventChannel.EventSink
    private val activity: Activity = registrar.activity()
    private var lineApiClient: LineApiClient;
    private var channelId = "";

    init {
        MethodChannel(registrar.messenger(), METHOD_CHANNEL).setMethodCallHandler(this)
        EventChannel(registrar.messenger(), EVENT_CHANNEL).setStreamHandler(this)
        registrar.addActivityResultListener(this)

        loadChannelIdFromMetadata(registrar.activeContext())
        lineApiClient = LineApiClientBuilder(registrar.activeContext(), channelId).build()
   }

    private fun loadChannelIdFromMetadata(context: Context) {
        var ai: ApplicationInfo? = null
        try {
            ai = context.packageManager.getApplicationInfo(
                    context.packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return
        }

        if (ai?.metaData == null) {
            return
        }

        val id = ai.metaData.get("line.channelId")
        if (id is String) {
            channelId = id
        }

        val code = ai.metaData.get("line.request_code_login")
        if (code is Int) {
            requestCode = code
        }

    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "startLogin" -> {
                if(BuildConfig.DEBUG) Log.d(TAG, "Method:startLogin")
                val loginIntent = LineLoginApi.getLoginIntent(activity, channelId)
                activity.startActivityForResult(loginIntent, requestCode)
                result.success(null)
            }
            "startWebLogin" -> {
                if(BuildConfig.DEBUG) Log.d(TAG, "Method:startWebLogin")
                val loginIntent = LineLoginApi.getLoginIntentWithoutLineAppAuth(activity, channelId)
                activity.startActivityForResult(loginIntent, requestCode)
                result.success(null)
            }
            "logout" -> {
                if(BuildConfig.DEBUG) Log.d(TAG, "Method:logout")
                launch {
                    async(CommonPool) {
                        return@async lineApiClient.logout()
                    }.await().let {
                        if (it.isSuccess) {
                            result.success(null)
                        } else {
                            result.error(it.responseCode.toString(),
                                    it.errorData.message,
                                    it.errorData.toString())
                        }
                    }
                }
            }
            "getProfile" -> {
                if(BuildConfig.DEBUG) Log.d(TAG, "Method:getProfile")
                launch {
                    async(CommonPool) {
                        return@async lineApiClient.profile
                    }.await().let {

                        if (it.isSuccess) {
                            val map: MutableMap<String, String?> = mutableMapOf(
                                    "userID" to it.responseData.userId,
                                    "displayName" to it.responseData.displayName)
                            if (it.responseData.pictureUrl != null) {
                                map["pictureUrl"] = it.responseData.pictureUrl.toString()
                            }
                            if (it.responseData.statusMessage != null) {
                                map["statusMessage"] = it.responseData.statusMessage
                            }
                            result.success(map)
                        } else {
                            result.error(it.responseCode.toString(),
                                    it.errorData.message,
                                    it.errorData.toString())
                        }
                    }
                }
            }
            "currentAccessToken" -> {
                if(BuildConfig.DEBUG) Log.d(TAG, "Method:currentAccessToken")
                launch {
                    async(CommonPool) {
                        return@async lineApiClient.currentAccessToken
                    }.await().let {
                        if (it.isSuccess) {
                            val map = mapOf(
                                    "accessToken" to it.responseData.accessToken,
                                    "expiresIn" to it.responseData.expiresInMillis.toString())
                            result.success(map)
                        } else {
                            result.success(null)
                        }
                    }
                }
            }
            "verifyToken" -> {
                if(BuildConfig.DEBUG) Log.d(TAG, "Method:verifyToken")
                launch {
                    async(CommonPool) {
                        return@async lineApiClient.verifyToken()
                    }.await().let {
                        if (it.isSuccess) {
                            result.success(null)
                        } else {
                            // Token is invalid
                            result.error(it.responseCode.toString(),
                                    it.errorData.message,
                                    it.errorData.toString())
                        }
                    }
                }
            }
            "refreshToken" -> {
                if(BuildConfig.DEBUG) Log.d(TAG, "Method:refreshToken")
                launch {
                    async(CommonPool) {
                        return@async lineApiClient.refreshAccessToken()
                    }.await().let {
                        if (it.isSuccess) {
                            val map = mapOf(
                                    "accessToken" to it.responseData.accessToken,
                                    "expiresIn" to it.responseData.expiresInMillis.toString())
                            result.success(map)
                        } else {
                            // Token is invalid
                            result.error(it.responseCode.toString(),
                                    it.errorData.message,
                                    it.errorData.toString())
                        }
                    }
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
        if(BuildConfig.DEBUG) Log.d(TAG, "onListen" + events)
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        if(BuildConfig.DEBUG) Log.d(TAG, "onCancel")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if(BuildConfig.DEBUG) Log.d(TAG, "onActivityResult")
        if (requestCode != this.requestCode) {
            if(BuildConfig.DEBUG) Log.d(TAG, "Unsupported Request")
            return false
        }

        val result = LineLoginApi.getLoginResultFromIntent(data)

        when (result.responseCode) {

            LineApiResponseCode.SUCCESS -> {
                if(BuildConfig.DEBUG) Log.d(TAG, "LINE Login Succeeded")

                val map = mutableMapOf(
                        "userID" to result.lineProfile?.userId,
                        "displayName" to result.lineProfile?.displayName,
                        "accessToken" to result.lineCredential?.accessToken?.accessToken,
                        "expiresIn" to result.lineCredential?.accessToken?.expiresInMillis.toString(),
                        "permissions" to result.lineCredential?.permission.toString()
                )
                if (result.lineProfile?.pictureUrl != null) {
                    map["pictureUrl"] = result.lineProfile?.pictureUrl.toString()
                }
                if (result.lineProfile?.statusMessage != null) {
                    map["statusMessage"] = result.lineProfile?.statusMessage
                }

                eventSink.success(map)
                return true
            }
            else -> {
                return false
            }
        }
    }

}
