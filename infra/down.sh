#!/bin/bash
set -e

branch="${1}"

if [[ -z "${branch}" ]]; then
    echo "No branch specified"
    exit 1
fi

file="${branch}/pid.txt"
echo "Trying to kill process in ${file}"

if [[ -f "${file}" ]]; then
        echo "Was running"
        read pid < ${file}
        if [[ -e "/proc/${pid}" ]]; then
                echo "Killing ${pid}"
                kill "${pid}"
        else
                echo "Not currently running"
        fi
        while [[ -e "/proc/${pid}" ]]; do
                echo "Waiting for ${pid} to die"
                sleep 0.1;
        done
        echo "Killed ${pid}"
        rm "${file}"
else
        echo "Not currently running"
fi
