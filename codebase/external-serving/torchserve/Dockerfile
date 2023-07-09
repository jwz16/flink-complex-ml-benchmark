FROM pytorch/torchserve:latest
RUN pip install transformers yolov5 sentencepiece protobuf==3.20.*
ENTRYPOINT ["/usr/local/bin/dockerd-entrypoint.sh"]
CMD ["serve"]