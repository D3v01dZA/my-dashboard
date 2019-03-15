#!/bin/bash

set -e

target=$1
file=$2

if [[ -z "${target}" ]]; then
    echo "No property specified"
    exit 1
fi

if [[ -z "${file}" ]]; then
    echo "No properties file specfied"
    exit 1
fi

if [[ ! -f "${file}" ]]; then
    echo "Properties file ${file} does not exist"
    exit 1
fi

value=`cat "${file}" | grep -w "${target}" | cut -d'=' -f2`

if [[ -z "${value}" ]]; then
    echo "Value ${target} does not exist in ${file}"
    exit 1
fi

echo "${value}"