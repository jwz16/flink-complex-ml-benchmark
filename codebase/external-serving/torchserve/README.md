TorchServe framework
===

## How to use

### Prerequisites
- 16GB RAM
- Linux x86_64
- Docker
- Linux (Ubuntu)
- Apache Flink 16.1
- Java 11
- Maven
- Python3

Add the current user to docker group to avoid TorchServe build failures:
```
sudo groupadd docker
sudo usermod -aG docker $USER
```

### Usage
First, build the docker image
```
./build_docker_image.sh
```

Then, run/stop TorchServe server
```
# run TorchServe (--rebuild arg is only for first run)
./start_torch_serve.sh --rebuild (do not run it as sudo)

# stop TorchServe
./stop_torch_serve.sh
```

Health check
```
curl http://localhost:8080/ping
```
Expect:
```
{
  "status": "Healthy"
}
```

Then send an image to a model for test
```
curl http://localhost:8080/predictions/mobilenet_v1 -T any_test_image.jpg
```