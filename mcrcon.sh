#!/bin/sh

docker run --rm -it \
 --network mc_default itzg/rcon-cli \
 --host mc_testing_1 \
 --port 25575 --password minecraft

