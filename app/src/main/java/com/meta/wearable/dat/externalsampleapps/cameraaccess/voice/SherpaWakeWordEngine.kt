package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.k2fsa.sherpa.onnx.KeywordSpotter
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.getFeatureConfig
import com.k2fsa.sherpa.onnx.getKeywordsFile
import com.k2fsa.sherpa.onnx.getKwsModelConfig
import kotlin.concurrent.thread

/**
 * Real sherpa-onnx wake-word engine with permanent manual fallback preserved elsewhere in the UI.
 *
 * This first runtime slice uses the sherpa Android keyword-spotting demo model and injects the
 * project's wake-word candidates through `createStream(...)`. It keeps the integration local and
 * replaceable while avoiding vendor lock-in.
 */
class SherpaWakeWordEngine(
    private val context: Context,
    private val onDetected: () -> Unit,
    private val onError: (String) -> Unit,
) : WakeWordEngine {
  companion object {
    private const val TAG = "CameraAccess:SherpaWakeWord"
    private const val SAMPLE_RATE = 16000
    private const val DETECTION_INTERVAL_SECONDS = 0.1
  }

  private val mainHandler = Handler(Looper.getMainLooper())
  private val lock = Any()

  @Volatile private var isRunning = false
  private var recordingThread: Thread? = null
  private var audioRecord: AudioRecord? = null
  private var stream: OnlineStream? = null
  private var keywordSpotter: KeywordSpotter? = null

  override val isConfigured: Boolean
    get() = keywordSpotter != null

  init {
    keywordSpotter =
        runCatching { buildKeywordSpotter() }
            .onFailure { error -> Log.e(TAG, "Failed to initialize sherpa-onnx wake word", error) }
            .getOrNull()
  }

  override fun start(): Boolean {
    val kws: KeywordSpotter
    val recorder: AudioRecord
    val createdStream: OnlineStream
    synchronized(lock) {
      if (isRunning) {
        return true
      }

      kws = keywordSpotter ?: run {
        onError("Wake word sherpa-onnx indisponivel. Use o botao manual por enquanto.")
        return false
      }

      recorder = createAudioRecord() ?: run {
        onError("Nao foi possivel iniciar o microfone da wake word.")
        return false
      }

      if (recorder.state != AudioRecord.STATE_INITIALIZED) {
        recorder.release()
        onError("O microfone da wake word nao foi inicializado pelo Android.")
        return false
      }

      createdStream =
          runCatching { kws.createStream() }
              .onFailure { error -> Log.e(TAG, "Failed to create sherpa keyword stream", error) }
              .getOrNull()
              ?: run {
                recorder.release()
                onError(
                    "Nao foi possivel iniciar a wake word do pacote. Verifique se o keywords.txt do modelo esta presente."
                )
                return false
              }

      if (createdStream.ptr == 0L) {
        recorder.release()
        runCatching { createdStream.release() }
        onError(
            "Nao foi possivel iniciar a wake word do pacote. Verifique se o keywords.txt do modelo esta presente."
        )
        return false
      }

      runCatching { recorder.startRecording() }
          .onFailure { error ->
            Log.e(TAG, "Failed to start AudioRecord for wake word", error)
            recorder.release()
            runCatching { createdStream.release() }
            onError("O Android bloqueou a captura do microfone para a wake word.")
            return false
          }

      if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
        Log.e(TAG, "AudioRecord did not reach RECORDSTATE_RECORDING")
        runCatching { recorder.stop() }
        recorder.release()
        runCatching { createdStream.release() }
        onError("O microfone nao entrou em modo de gravacao para a wake word.")
        return false
      }

      audioRecord = recorder
      stream = createdStream
      isRunning = true
      recordingThread =
          thread(start = true, isDaemon = true, name = "SherpaWakeWord") {
            processSamples(kws, createdStream, recorder)
          }
      return true
    }
  }

  override fun stop() {
    val worker: Thread?
    val recorder: AudioRecord?
    synchronized(lock) {
      isRunning = false
      worker = recordingThread
      recorder = audioRecord
    }
    runCatching { recorder?.stop() }
    worker?.interrupt()
    runCatching { worker?.join(300) }
    synchronized(lock) {
      if (recordingThread === worker) {
        recordingThread = null
      }
    }
  }

  override fun release() {
    stop()
    keywordSpotter = null
  }

  private fun buildKeywordSpotter(): KeywordSpotter {
    val config =
        KeywordSpotterConfig(
            featConfig = getFeatureConfig(sampleRate = SAMPLE_RATE, featureDim = 80),
            modelConfig = getKwsModelConfig(type = SherpaWakeWordConfig.modelType)
                ?: error("No sherpa keyword model config available"),
            maxActivePaths = SherpaWakeWordConfig.maxActivePaths,
            keywordsFile = getKeywordsFile(type = SherpaWakeWordConfig.modelType),
            keywordsScore = SherpaWakeWordConfig.keywordsScore,
            keywordsThreshold = SherpaWakeWordConfig.keywordsThreshold,
            numTrailingBlanks = SherpaWakeWordConfig.numTrailingBlanks,
        )
    return KeywordSpotter(
        assetManager = context.assets,
        config = config,
    )
  }

  private fun createAudioRecord(): AudioRecord? {
    val minBufferSize =
        AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )

    if (minBufferSize <= 0) {
      Log.e(TAG, "Invalid AudioRecord buffer size: $minBufferSize")
      return null
    }

    return AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        minBufferSize * 2,
    )
  }

  private fun processSamples(
      keywordSpotter: KeywordSpotter,
      stream: OnlineStream,
      recorder: AudioRecord,
  ) {
    val bufferSize = (DETECTION_INTERVAL_SECONDS * SAMPLE_RATE).toInt()
    val buffer = ShortArray(bufferSize)
    var detected = false

    try {
      while (isRunning) {
        val read = recorder.read(buffer, 0, buffer.size)
        if (read == AudioRecord.ERROR_INVALID_OPERATION) {
          mainHandler.post { onError("O Android retornou operacao invalida ao ler o microfone da wake word.") }
          return
        }
        if (read == AudioRecord.ERROR_BAD_VALUE) {
          mainHandler.post { onError("O Android retornou configuracao invalida ao ler o microfone da wake word.") }
          return
        }
        if (read <= 0) {
          Log.w(TAG, "AudioRecord read returned $read")
          continue
        }

        val samples = FloatArray(read) { index -> buffer[index] / 32768.0f }
        stream.acceptWaveform(samples, sampleRate = SAMPLE_RATE)

        while (keywordSpotter.isReady(stream)) {
          keywordSpotter.decode(stream)
          val result = keywordSpotter.getResult(stream).keyword
          if (result.isNotBlank()) {
            keywordSpotter.reset(stream)
            detected = true
            break
          }
        }
        if (detected) {
          synchronized(lock) {
            isRunning = false
          }
          break
        }
      }
    } catch (error: Exception) {
      Log.e(TAG, "Wake-word loop failed", error)
      mainHandler.post { onError("A escuta hands-free falhou. O botao manual continua disponivel.") }
    } finally {
      cleanupWorkerResources(stream, recorder)
      if (detected) {
        mainHandler.post(onDetected)
      }
    }
  }

  private fun cleanupWorkerResources(
      workerStream: OnlineStream,
      workerRecorder: AudioRecord,
  ) {
    synchronized(lock) {
      if (stream === workerStream) {
        stream = null
      }
      if (audioRecord === workerRecorder) {
        audioRecord = null
      }
      if (recordingThread === Thread.currentThread()) {
        recordingThread = null
      }
    }
    runCatching { workerRecorder.stop() }
    workerRecorder.release()
    runCatching { workerStream.release() }
  }
}
