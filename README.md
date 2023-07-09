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
