package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

import android.content.Context
import android.util.Log

object WakeWordEngineFactory {
  private const val TAG = "CameraAccess:WakeWordFactory"

  fun create(
      context: Context,
      onDetected: () -> Unit,
      onError: (String) -> Unit,
  ): WakeWordEngine {
    if (!hasSherpaAssets(context)) {
      Log.i(
          TAG,
          "Sherpa assets not found in ${SherpaWakeWordConfig.assetsDirectory}. Falling back to manual wake word mode.",
      )
      return ManualOnlyWakeWordEngine()
    }

    return SherpaWakeWordEngine(
        context = context,
        onDetected = onDetected,
        onError = onError,
    )
  }

  private fun hasSherpaAssets(context: Context): Boolean =
      runCatching {
            context.assets.list(SherpaWakeWordConfig.assetsDirectory)?.isNotEmpty() == true
          }
          .getOrDefault(false)
}
