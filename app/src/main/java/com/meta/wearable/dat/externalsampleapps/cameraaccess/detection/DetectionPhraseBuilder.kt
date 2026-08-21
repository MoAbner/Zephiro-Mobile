package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

object DetectionPhraseBuilder {
  fun build(spokenName: String, proximity: ProximityLevel): String =
      when (proximity) {
        ProximityLevel.VERY_CLOSE -> "$spokenName muito perto"
        ProximityLevel.APPROACHING -> "$spokenName se aproximando"
        ProximityLevel.FAR -> spokenName
      }
}
