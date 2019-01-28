#!/bin/bash
set -e

branch="${1}"

./down.sh "${branch}"
./up.sh "${branch}"
