package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

import android.content.Context
import android.graphics.Bitmap
import java.nio.ByteBuffer
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector as TaskVisionObjectDetector

class TfliteObjectDetector(
    context: Context,
    config: TfliteObjectDetectorConfig,
) : ObjectDetector {
  private val detector =
      TaskVisionObjectDetector.createFromFileAndOptions(
          context,
          config.modelAssetPath,
          TaskVisionObjectDetector.ObjectDetectorOptions.builder()
              .setScoreThreshold(config.scoreThreshold)
              .setMaxResults(config.maxResults)
              .build(),
      )

  override val isConfigured: Boolean = true

  override suspend fun detect(
      rgbaBytes: ByteArray,
      frameWidth: Int,
      frameHeight: Int,
  ): List<DetectionCandidate> {
    val bitmap = rgbaBytes.toBitmap(frameWidth, frameHeight)
    val detections = detector.detect(TensorImage.fromBitmap(bitmap))
    return detections.mapNotNull { detection ->
      val category = detection.categories.maxByOrNull { it.score } ?: return@mapNotNull null
      val box = detection.boundingBox
      DetectionCandidate(
          label = category.label,
          confidence = category.score,
          box =
              DetectionBox(
                  left = box.left,
                  top = box.top,
                  right = box.right,
                  bottom = box.bottom,
              ),
      )
    }
  }

  private fun ByteArray.toBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(this))
    return bitmap
  }
}
