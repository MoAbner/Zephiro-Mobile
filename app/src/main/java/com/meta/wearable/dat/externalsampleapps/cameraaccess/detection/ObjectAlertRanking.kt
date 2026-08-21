package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

/** Weights that determine which single detection is announced first. */
data class ObjectAlertRanking(
    val classPriorityWeight: Float,
    val proximityWeight: Float,
    val confidenceWeight: Float,
) {
  companion object {
    // Best default for rooms: what is closest to the user is normally most actionable.
    val DISTANCE_FIRST =
        ObjectAlertRanking(
            classPriorityWeight = 1f,
            proximityWeight = 3f,
            confidenceWeight = 0.5f,
        )

    // Future urban profile: hazardous classes receive more weight, while distance still matters.
    val SAFETY_FIRST =
        ObjectAlertRanking(
            classPriorityWeight = 3f,
            proximityWeight = 2f,
            confidenceWeight = 0.5f,
        )
  }
}
