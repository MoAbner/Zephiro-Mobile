Sherpa wake-word assets

The complete wake-word package is versioned in this repository so hands-free works after a clone.
The app enables hands-free mode only when the sherpa model directory exists under:

`app/src/main/assets/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01`

Expected source:

`https://github.com/k2-fsa/sherpa-onnx/releases/download/kws-models/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01.tar.bz2`

Project source and license:

- sherpa-onnx project: `https://github.com/k2-fsa/sherpa-onnx`
- Apache-2.0 license: `https://github.com/k2-fsa/sherpa-onnx/blob/master/LICENSE`

Expected result after extraction:

- `app/src/main/assets/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01/encoder-epoch-12-avg-2-chunk-16-left-64.onnx`
- `app/src/main/assets/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01/decoder-epoch-12-avg-2-chunk-16-left-64.onnx`
- `app/src/main/assets/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01/joiner-epoch-12-avg-2-chunk-16-left-64.onnx`
- `app/src/main/assets/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01/tokens.txt`
- `app/src/main/assets/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01/keywords.txt`

Hands-free stays disabled if the folder above does not exist.
