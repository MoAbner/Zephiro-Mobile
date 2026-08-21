# EfficientDet Lite0 object detector

`object_detector.tflite` is versioned in this repository so every team member can run the F004
controlled-environment MVP without downloading a model separately.

## Source and attribution

- Model: EfficientDet Lite0 object detection model with Task Vision metadata
- Official download URL:
  `https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/object_detection/rpi/lite-model_efficientdet_lite0_detection_metadata_1.tflite`
- Official TensorFlow example using this exact artifact:
  `https://github.com/tensorflow/examples/blob/master/lite/examples/object_detection/raspberry_pi/setup.sh`
- TensorFlow Models license: Apache-2.0
  `https://github.com/tensorflow/models/blob/master/LICENSE`
- The model is trained for COCO-style object categories. It is a general-purpose detector, not a
  navigation or safety-certified model.

The Kotlin runtime maps a conservative subset of labels to the controlled MVP: `person`, `chair`,
`bottle`, `car`, `motorcycle`, `bus`, `truck`, and `bicycle`.
