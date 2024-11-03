package flutter.plugins.screen

import android.provider.Settings
import android.view.WindowManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class ScreenPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var activityBinding: ActivityPluginBinding? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "github.com/clovisnicolas/flutter_screen")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "brightness" -> {
                result.success(getBrightness())
            }
            "setBrightness" -> {
                val brightness = call.argument<Double>("brightness")
                val window = activityBinding?.activity?.window ?: return
                val layoutParams = window.attributes
                layoutParams.screenBrightness = brightness?.toFloat() ?: 0f
                window.attributes = layoutParams
                result.success(null)
            }
            "isKeptOn" -> {
                val flags = activityBinding?.activity?.window?.attributes?.flags ?: 0
                result.success((flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0)
            }
            "keepOn" -> {
                val on = call.argument<Boolean>("on") ?: false
                val window = activityBinding?.activity?.window ?: return
                if (on) {
                    println("Keeping screen on")
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    println("Not keeping screen on")
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                result.success(null)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getBrightness(): Float {
        val window = activityBinding?.activity?.window ?: return 1.0f
        var result = window.attributes.screenBrightness
        if (result < 0) { // the application is using the system brightness
            try {
                val contentResolver = activityBinding?.activity?.contentResolver ?: return 1.0f
                result = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
            } catch (e: Settings.SettingNotFoundException) {
                result = 1.0f
                e.printStackTrace()
            }
        }
        return result
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
    }

    override fun onDetachedFromActivity() {
        activityBinding = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }
}