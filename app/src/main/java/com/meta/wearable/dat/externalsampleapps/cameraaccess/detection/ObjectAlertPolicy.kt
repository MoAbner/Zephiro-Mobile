package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

class ObjectAlertPolicy(
    private val confidenceThreshold: Float = 0.45f,
    private val cooldownMillis: Long = 4_000L,
    private val ranking: ObjectAlertRanking = ObjectAlertRanking.DISTANCE_FIRST,
    private val timeProviderMillis: () -> Long = { System.currentTimeMillis() },
) {
  private val lastAlertAtByClass = mutableMapOf<ControlledObjectClass, Long>()

  fun selectAlert(
      detections: List<DetectedObject>,
  ): ObjectAlert? {
    val now = timeProviderMillis()
    val best =
        detections
            .asSequence()
            .filter { it.confidence >= confidenceThreshold }
            .sortedWith(
                compareByDescending<DetectedObject> { score(it) }
                    .thenByDescending { it.confidence }
            )
            .firstOrNull()
            ?: return null

    val lastAlertAt = lastAlertAtByClass[best.objectClass] ?: 0L
    if (now - lastAlertAt < cooldownMillis) {
      return null
    }

    lastAlertAtByClass[best.objectClass] = now
    return ObjectAlert(
        objectClass = best.objectClass,
        phrase = DetectionPhraseBuilder.build(best.objectClass.spokenName, best.proximity),
        confidence = best.confidence,
        proximity = best.proximity,
    )
  }

  fun clearCooldowns() {
    lastAlertAtByClass.clear()
  }

  private fun score(detection: DetectedObject): Float {
    val proximityScore =
        when (detection.proximity) {
          ProximityLevel.VERY_CLOSE -> 3f
          ProximityLevel.APPROACHING -> 2f
          ProximityLevel.FAR -> 1f
        }
    // Class priority is normalized to the 0..1 range so it can be tuned against distance.
    val classScore = detection.objectClass.priority / 100f
    return (
        classScore * ranking.classPriorityWeight +
            proximityScore * ranking.proximityWeight +
            detection.confidence * ranking.confidenceWeight
        )
  }
}
