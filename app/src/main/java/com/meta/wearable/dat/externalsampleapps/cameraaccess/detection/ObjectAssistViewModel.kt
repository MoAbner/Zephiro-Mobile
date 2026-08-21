package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

data class ObjectAssistUiState(
    val isActive: Boolean = false,
    val mode: ObjectAssistMode = ObjectAssistMode.CONTROLLED_ROOM,
    val isDetectorConfigured: Boolean = false,
    val lastAlert: ObjectAlert? = null,
    val lastDebugSummary: String? = null,
)

class ObjectAssistViewModel(
    application: Application,
    private val detector: ObjectDetector = ObjectDetectorFactory.create(application),
    private val mapper: ControlledObjectMapper = ControlledObjectMapper(),
    private val alertPolicy: ObjectAlertPolicy = ObjectAlertPolicy(),
) : AndroidViewModel(application) {
  private val _uiState = MutableStateFlow(ObjectAssistUiState())
  val uiState: StateFlow<ObjectAssistUiState> = _uiState.asStateFlow()

  init {
    _uiState.update { it.copy(isDetectorConfigured = detector.isConfigured) }
  }

  fun start(mode: ObjectAssistMode = ObjectAssistMode.CONTROLLED_ROOM): Boolean {
    alertPolicy.clearCooldowns()
    _uiState.update {
      it.copy(
          isActive = true,
          mode = mode,
          isDetectorConfigured = detector.isConfigured,
          lastAlert = null,
          lastDebugSummary =
              if (detector.isConfigured) {
                "Assistencia por objetos iniciada em modo controlado."
              } else {
                "Assistencia por objetos iniciada em modo controlado. ${(detector as? NoOpObjectDetector)?.reason ?: "O detector real ainda nao esta conectado."}"
              },
      )
    }
    return detector.isConfigured
  }

  fun stop() {
    _uiState.update {
      it.copy(
          isActive = false,
          isDetectorConfigured = detector.isConfigured,
          lastDebugSummary = "Assistencia por objetos interrompida.",
      )
    }
  }

  suspend fun processFrame(
      rgbaBytes: ByteArray,
      frameWidth: Int,
      frameHeight: Int,
  ): ObjectAlert? {
    if (!_uiState.value.isActive || !detector.isConfigured) {
      return null
    }

    val mapped =
        mapper.mapCandidates(
            candidates = detector.detect(rgbaBytes, frameWidth, frameHeight),
            frameWidth = frameWidth,
            frameHeight = frameHeight,
        )
    val alert = alertPolicy.selectAlert(mapped)
    if (alert != null) {
      _uiState.update {
        it.copy(
            lastAlert = alert,
            lastDebugSummary = "Alerta: ${alert.phrase} (${(alert.confidence * 100).toInt()}%)",
        )
      }
    }
    return alert
  }

  /** Runs inference on a downscaled camera still to cap allocations and battery use. */
  suspend fun processBitmap(bitmap: Bitmap): ObjectAlert? =
      withContext(Dispatchers.Default) {
        if (!_uiState.value.isActive || !detector.isConfigured) {
          return@withContext null
        }

        val scaled = bitmap.scaleForInference()
        try {
          val rgbaBytes = ByteArray(scaled.width * scaled.height * 4)
          java.nio.ByteBuffer.wrap(rgbaBytes).also { buffer -> scaled.copyPixelsToBuffer(buffer) }
          processFrame(rgbaBytes, scaled.width, scaled.height)
        } catch (error: Exception) {
          _uiState.update {
            it.copy(lastDebugSummary = "Falha na inferencia TFLite: ${error.message ?: "erro desconhecido"}")
          }
          null
        } finally {
          if (scaled !== bitmap) {
            scaled.recycle()
          }
        }
      }

  private fun Bitmap.scaleForInference(): Bitmap {
    val longestSide = maxOf(width, height)
    if (longestSide <= MAX_INFERENCE_SIDE_PX) return this
    val scale = MAX_INFERENCE_SIDE_PX.toFloat() / longestSide
    return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
  }

  class Factory(
      private val application: Application,
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(ObjectAssistViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return ObjectAssistViewModel(application) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }

  private companion object {
    const val MAX_INFERENCE_SIDE_PX = 320
  }
}
