#!/bin/bash
set -euo pipefail

readonly REMOTE_IMAGE_CONTEXT="${1:?must be set}"

mvn clean package -DskipTests

readonly image="aeo-memory-test:$(date +%s)"
readonly remote_image="gcr.io/$REMOTE_IMAGE_CONTEXT/$image"
echo "Building $image"

docker build . -t "$image"
docker tag "$image" "$remote_image"
docker push "$remote_image"

kubectl delete deployment aeo-memory-test || true
kubectl create deployment aeo-memory-test --image $remote_image

kubectl set env deployment aeo-memory-test WORKERS=2 EXECUTIONS=20
kubectl set env deployment aeo-memory-test CONTEXT="test6"
