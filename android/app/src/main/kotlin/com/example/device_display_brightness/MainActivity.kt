package com.example.device_display_brightness

import android.provider.Settings
import android.view.WindowManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    companion object {
        private const val METHOD_CHANNEL = "com.pnt/device_display_brightness"
    }

    private var channel: MethodChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        channel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL
        )
        channel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "getBrightness" -> result.success(getBrightness())

                "setBrightness" -> {
                    val brightness: Float = call.argument("brightness")!!
                    setBrightness(brightness)
                    result.success(null)
                }

                "resetBrightness" -> {
                    resetBrightness()
                    result.success(null)
                }

                "isKeptOn" -> {
                    result.success(isKeptOn())
                }

                "keepOn" -> {
                    val enabled: Boolean = call.argument("enabled")!!
                    keepOn(enabled)
                    result.success(null)
                }

                else -> result.notImplemented()
            }
        }
    }

    private fun getBrightness(): Float {
        var result: Float = activity.window.attributes.screenBrightness

        // if result < 0, then application is using the system brightness
        if (result < 0) {
            result = try {
                Settings.System.getInt(
                    activity.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS
                ) / 255.toFloat()
            } catch (e: Exception) {
                0.0f
            }
        }

        return result
    }

    private fun setBrightness(brightness: Float) {
        val layoutParams: WindowManager.LayoutParams = activity.window!!.attributes
        layoutParams.screenBrightness = brightness

        activity.window?.attributes = layoutParams
    }

    private fun resetBrightness() {
        val layoutParams: WindowManager.LayoutParams = activity.window!!.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

        activity.window?.attributes = layoutParams
    }

    private fun isKeptOn(): Boolean {
        val flags: Int? = activity.window?.attributes?.flags

        return (flags != null) && (flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0)
    }

    private fun keepOn(enabled: Boolean) {
        if (enabled) {
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        channel?.setMethodCallHandler(null)
        channel = null
    }
}
