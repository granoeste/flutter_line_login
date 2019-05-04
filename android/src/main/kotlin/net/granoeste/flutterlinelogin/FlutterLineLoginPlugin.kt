package net.granoeste.flutterlinelogin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.api.LineApiClient
import com.linecorp.linesdk.api.LineApiClientBuilder
import com.linecorp.linesdk.auth.LineLoginApi
import com.linecorp.linesdk.auth.LineAuthenticationParams
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import com.linecorp.linesdk.Scope


class FlutterLineLoginPlugin(registrar: Registrar) : MethodCallHandler, PluginRegistry.ActivityResultListener {

    private val METHOD_CHANNEL = "net.granoeste/flutter_line_login"
    private val channel: MethodChannel
    private var requestCode = 20000904

    companion object {
        val TAG: String = FlutterLineLoginPlugin::class.java.simpleName

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            FlutterLineLoginPlugin(registrar)
        }
    }

    private val activity: Activity = registrar.activity()
    private var lineApiClient: LineApiClient
    private var lineAuthenticationParams: LineAuthenticationParams
    private var channelId = ""

    init {
        channel = MethodChannel(registrar.messenger(), METHOD_CHANNEL)
        channel.setMethodCallHandler(this)
        registrar.addActivityResultListener(this)

        loadChannelIdFromMetadata(registrar.activeContext())
        lineApiClient = LineApiClientBuilder(registrar.activeContext(), channelId).build()

        /*
         * Represents a scope. A scope is a permission that the user grants your app during the login process.
         *
         * Scope.OC_EMAIL - Permission to get the user's email address.
         * Scope.OPENID_CONNECT - Permission to get an ID token that includes the user information.
         * Scope.PROFILE - Permission to get the user's profile information.
        */
        lineAuthenticationParams = LineAuthenticationParams.Builder()
                .scopes(listOf(Scope.OC_EMAIL, Scope.OPENID_CONNECT, Scope.PROFILE))
                .build()
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
                if (BuildConfig.DEBUG) Log.d(TAG, "Method:startLogin")
                val loginIntent = LineLoginApi.getLoginIntent(activity, channelId, lineAuthenticationParams)
                activity.startActivityForResult(loginIntent, requestCode)
                result.success(null)
            }
            "startWebLogin" -> {
                if (BuildConfig.DEBUG) Log.d(TAG, "Method:startWebLogin")
                val loginIntent = LineLoginApi.getLoginIntentWithoutLineAppAuth(activity, channelId, lineAuthenticationParams)
                activity.startActivityForResult(loginIntent, requestCode)
                result.success(null)
            }
            "logout" -> {
                if (BuildConfig.DEBUG) Log.d(TAG, "Method:logout")
                GlobalScope.launch {
                    async(Dispatchers.Default) {
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
                if (BuildConfig.DEBUG) Log.d(TAG, "Method:getProfile")
                GlobalScope.launch {
                    async(Dispatchers.Default) {
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
                if (BuildConfig.DEBUG) Log.d(TAG, "Method:currentAccessToken")
                GlobalScope.launch {
                    async(Dispatchers.Default) {
                        return@async lineApiClient.currentAccessToken
                    }.await().let {
                        if (it.isSuccess) {
                            val map = mapOf(
                                    "accessToken" to it.responseData.tokenString,
                                    "expiresIn" to it.responseData.expiresInMillis.toString())
                            result.success(map)
                        } else {
                            result.success(null)
                        }
                    }
                }
            }
            "verifyToken" -> {
                if (BuildConfig.DEBUG) Log.d(TAG, "Method:verifyToken")
                GlobalScope.launch {
                    async(Dispatchers.Default) {
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
                if (BuildConfig.DEBUG) Log.d(TAG, "Method:refreshToken")
                GlobalScope.launch {
                    async(Dispatchers.Default) {
                        return@async lineApiClient.refreshAccessToken()
                    }.await().let {
                        if (it.isSuccess) {
                            val map = mapOf(
                                    "accessToken" to it.responseData.tokenString,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActivityResult")
        if (requestCode != this.requestCode) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Unsupported Request")
            return false
        }

        val result = LineLoginApi.getLoginResultFromIntent(data)
        if (BuildConfig.DEBUG) Log.d(TAG, "LineLoginResult:$result")

        when (result.responseCode) {

            LineApiResponseCode.SUCCESS -> {
                if (BuildConfig.DEBUG) Log.d(TAG, "LINE Login Succeeded")

                val map = mutableMapOf(
                        "userID" to result.lineProfile?.userId,
                        "displayName" to result.lineProfile?.displayName,
                        "accessToken" to result.lineCredential?.accessToken?.tokenString,
                        "expiresIn" to result.lineCredential?.accessToken?.expiresInMillis.toString(), // Compatibility
                        "permissions" to result.lineCredential?.scopes?.map { it.code }?.toList()
                )
                if (result.lineProfile?.pictureUrl != null) {
                    map["pictureUrl"] = result.lineProfile?.pictureUrl.toString()
                }
                if (result.lineProfile?.statusMessage != null) {
                    map["statusMessage"] = result.lineProfile?.statusMessage
                }

                channel.invokeMethod("loginSuccess", map);
                return true
            }
            else -> {
                Log.w(TAG, "LINE Login Failed with Error: ${result.responseCode},${result.errorData.message}")

                channel.invokeMethod("loginFailed", mapOf(
                        "responseCode" to result.responseCode.toString(),
                        "message" to result.errorData.message
                ))
                return false
            }
        }
    }

}
