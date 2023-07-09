# Flink Complex ML Benchmark Suite

## Prerequisites

- Linux (Ubuntu)
- Apache Flink
- Java 11
- Maven
- Python3
- Docker
- 16GB RAM

> To keep the repository clean, all the model weights are deleted. The codebase may not runnable at this moment due to the lack of model weights.

## Install dependencies

### System dependencies
```bash
sudo apt update && sudo apt install -y libgl1
```

### Model conversion dependencies
```python
python3 -m venv .venv
source .venv/bin/activate

pip3 install onnx onnxruntime
pip3 install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu
pip3 install matplotlib transformers sentencepiece yolov5 protobuf==3.20.*
```

## Run experiments
See [Codebase README](./codebase/flink-complex-ml-benchmark/README.md)

## Build
```bash
./scripts/build.sh
```

## TODO
- [x] TorchServe support
- [x] ONNX support
- [ ] TF-Saved support
- [ ] ND4j support
- [ ] TF-serving support
