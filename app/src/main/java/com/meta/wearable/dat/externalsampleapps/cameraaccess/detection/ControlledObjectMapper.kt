package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

class ControlledObjectMapper(
    private val proximityEstimator: ProximityEstimator = ProximityEstimator(),
) {
  fun mapCandidates(
      candidates: List<DetectionCandidate>,
      frameWidth: Int,
      frameHeight: Int,
  ): List<DetectedObject> =
      candidates.mapNotNull { candidate ->
        val objectClass = ControlledObjectClass.fromModelLabel(candidate.label) ?: return@mapNotNull null
        DetectedObject(
            objectClass = objectClass,
            confidence = candidate.confidence,
            box = candidate.box,
            proximity = proximityEstimator.estimate(candidate.box, frameWidth, frameHeight),
        )
      }
}
