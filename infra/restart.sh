#!/bin/bash

branch="${1}"
port="${2}"

./down.sh "${branch}"
./up.sh "${branch}" "${port}"