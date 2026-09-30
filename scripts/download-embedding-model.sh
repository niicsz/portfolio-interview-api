#!/usr/bin/env sh
# Baixa o multilingual-e5-small (ONNX quantizado) fixado em um commit e confere o SHA-256.
set -eu

REPO="Xenova/multilingual-e5-small"
REVISION="761b726dd34fb83930e26aab4e9ac3899aa1fa78"
TARGET="${1:-models/multilingual-e5-small}"

mkdir -p "$TARGET"

download() {
  file="$1"
  output="$2"
  expected="$3"
  if [ -f "$TARGET/$output" ] && echo "$expected  $TARGET/$output" | sha256sum -c - >/dev/null 2>&1; then
    echo "$output já existe e confere"
    return
  fi
  echo "Baixando $output..."
  curl -fsSL -o "$TARGET/$output" "https://huggingface.co/$REPO/resolve/$REVISION/$file"
  echo "$expected  $TARGET/$output" | sha256sum -c -
}

download "onnx/model_quantized.onnx" "model_quantized.onnx" \
  "f80102d3f2a1229f387d3c81909990d8945513e347b0eab049f7de3c6f98c193"
download "tokenizer.json" "tokenizer.json" \
  "0b44a9d7b51c3c62626640cda0e2c2f70fdacdc25bbbd68038369d14ebdf4c39"
