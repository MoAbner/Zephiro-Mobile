package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

object SherpaWakeWordConfig {
  // type = 1 maps to the English GigaSpeech keyword-spotting model in sherpa-onnx.
  // This is a better fit for keywords such as "zephiro" / "zefiro" than the Chinese model.
  const val modelType: Int = 1
  const val assetsDirectory: String = "sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01"

  // Tunable keyword-spotting behavior. Start conservative for hackathon demos and adjust after
  // testing on-device.
  const val maxActivePaths: Int = 4
  const val keywordsScore: Float = 1.5f
  const val keywordsThreshold: Float = 0.25f
  const val numTrailingBlanks: Int = 1

  val packagedProfile =
      WakeWordProfile(
          id = "packaged_keywords",
          displayName = "Sherpa packaged keywords",
          examples =
              listOf(
                  "hello world",
                  "hey siri",
                  "alexa",
                  "hi google",
              ),
          activationPrompt =
              "Modo hands-free ativado. Diga uma wake word do pacote, como alexa ou hello world, e depois um comando.",
      )

  val zephiroPlannedProfile =
      WakeWordProfile(
          id = "zephiro_custom",
          displayName = "Zephiro custom keywords",
          examples =
              listOf(
                  "zephiro",
                  "zefiro",
                  "oi zephiro",
                  "ei zefiro",
              ),
          activationPrompt =
              "Modo hands-free ativado. Diga Zephiro e depois um comando.",
          notReadyReason =
              "O perfil Zephiro ainda depende de um keywords.txt tokenizado compativel com o sherpa-onnx.",
      )

  // Current runtime profile: validated packaged keywords. Switch this to the Zephiro profile only
  // after shipping a compatible custom keywords.txt with the app assets.
  val activeProfile: WakeWordProfile = packagedProfile
}
