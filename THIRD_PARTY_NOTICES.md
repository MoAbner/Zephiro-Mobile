# Third-Party Notices

## Meta Wearables Device Access Toolkit sample

Parts of this application originate from the Meta Wearables DAT CameraAccess sample. The required
`LICENSE` and `NOTICE` files are included at the repository root and must remain with redistributed
source code.

Source: `https://github.com/facebook/meta-wearables-dat-android`

## sherpa-onnx wake-word runtime and assets

The application uses the sherpa-onnx Android runtime and the bundled
`sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01` keyword-spotting package.

- Source: `https://github.com/k2-fsa/sherpa-onnx`
- Release asset source:
  `https://github.com/k2-fsa/sherpa-onnx/releases/download/kws-models/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01.tar.bz2`
- Project license: Apache License 2.0
  `https://github.com/k2-fsa/sherpa-onnx/blob/master/LICENSE`

## TensorFlow Lite EfficientDet Lite0 object detector

The application bundles `object_detector.tflite`, sourced from TensorFlow's public object detection
model distribution.

- Model download:
  `https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/object_detection/rpi/lite-model_efficientdet_lite0_detection_metadata_1.tflite`
- TensorFlow example referencing the same artifact:
  `https://github.com/tensorflow/examples/blob/master/lite/examples/object_detection/raspberry_pi/setup.sh`
- TensorFlow Models project license: Apache License 2.0
  `https://github.com/tensorflow/models/blob/master/LICENSE`

Before any use beyond the hackathon prototype, review the terms for the exact model artifact and
its training-data provenance. This project makes no claim that the model is appropriate for
safety-critical navigation.
