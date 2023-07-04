#!/bin/bash

echo 'clean old workflow archiver files...'
rm -rf ./wf-store/*.war
echo 'done'

wf_spec_file=workflow_damaged_vehicle_license_plate_extraction.yaml
handler_file=workflow_damaged_vehicle_license_plate_extraction_handler.py

WORKFLOW_NAME=damaged_vehicle_license_plate_extraction_wf
CONTAINER_SPEC_FILE=/home/model-server/${wf_spec_file}
CONTAINER_HANDLER=/home/model-server/${handler_file}

HOST_HANDLER=$(pwd)/handlers/${handler_file}
HOST_WF_SPEC_FILE=$(pwd)/${wf_spec_file}

CMD_CREATE_WAR="torch-workflow-archiver -f --workflow-name ${WORKFLOW_NAME} --spec-file ${CONTAINER_SPEC_FILE} --handler ${CONTAINER_HANDLER} --export-path wf-store/"

docker run --rm --name war -v $(pwd)/wf-store:/home/model-server/wf-store -v $HOST_HANDLER:$CONTAINER_HANDLER -v $HOST_WF_SPEC_FILE:$CONTAINER_SPEC_FILE --entrypoint="" pytorch/torchserve:latest $CMD_CREATE_WAR