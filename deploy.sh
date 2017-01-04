#!/usr/bin/env bash

rev=$(git rev-parse --short HEAD)

cp -r scripts out/artifacts/multiplayer_game_engine_jar/
cd out/artifacts

git init
git config user.name "Debalin Das"
git config user.email "debalin90@gmail.com"

git remote add upstream "https://$GH_TOKEN@github.com/debalin/multiplayer-game-engine.git"
git fetch upstream
git reset upstream/release

touch .

git add -A .
git commit -m "JAR created for ${rev}. [skip ci]"
git push upstream HEAD:release
