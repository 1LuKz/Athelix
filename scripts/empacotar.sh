#!/usr/bin/env bash
set -euo pipefail

diretorio_script="$(dirname -- "${BASH_SOURCE[0]}")"
cd -- "$diretorio_script/.."

mkdir -p entrega
diretorio_temporario="$(mktemp -d entrega/.empacotar.XXXXXX)"
trap 'rm -rf -- "$diretorio_temporario"' EXIT

exclusoes=(
    '*/node_modules/*'
    '*/dist/*'
    '*/target/*'
    '*/__MACOSX/*'
    '*/.DS_Store'
    '*.tsbuildinfo'
)

for pasta_projeto in frontend backend docs; do
    arquivo_zip="$diretorio_temporario/$pasta_projeto.zip"
    zip -qr -X "$arquivo_zip" "$pasta_projeto" -x "${exclusoes[@]}"
    mv -f -- "$arquivo_zip" "entrega/$pasta_projeto.zip"
done

du -h entrega/frontend.zip entrega/backend.zip entrega/docs.zip
