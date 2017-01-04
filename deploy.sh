#!/usr/bin/env bash

rev=$(git rev-parse --short HEAD)

git config user.name "Debalin Das"
git config user.email "debalin90@gmail.com"

git remote add upstream "https://$GH_TOKEN@github.com/debalin/multiplayer-game-engine.git"

git add out/artifacts/multiplayer_game_engine_jar/multiplayer-game-engine.jar
git commit -m "JAR created for ${rev}."
git push -q upstream HEAD
