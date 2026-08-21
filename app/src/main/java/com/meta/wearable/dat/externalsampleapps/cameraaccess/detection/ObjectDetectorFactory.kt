package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

import android.content.Context

object ObjectDetectorFactory {
  fun create(context: Context): ObjectDetector {
    val assetPath = TfliteObjectDetectorConfig.modelAssetPath
    val assetExists =
        runCatching { context.assets.open(assetPath).close() }
            .isSuccess

    if (!assetExists) {
      return NoOpObjectDetector(
          reason =
              "Modelo TFLite nao encontrado em assets/$assetPath. O fluxo da feature continua ativo, mas sem inferencia real.",
      )
    }

    return runCatching {
          TfliteObjectDetector(
              context = context.applicationContext,
              config = TfliteObjectDetectorConfig,
          )
        }
        .getOrElse { error ->
          NoOpObjectDetector(
              reason =
                  "Falha ao inicializar o detector TFLite: ${error.message ?: "erro desconhecido"}",
          )
        }
  }
}
