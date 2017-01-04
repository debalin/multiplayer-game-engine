#!/usr/bin/env bash

# This script simply takes the JAR produced as a result
# of the Travis build, compresses and pushes it to the release branch.
# Taken from http://www.steveklabnik.com/automatically_update_github_pages_with_travis_example/.

rev=$(git rev-parse --short HEAD)

cp -r scripts out/artifacts/multiplayer_game_engine_jar/
cd out/artifacts/multiplayer_game_engine_jar
zip multiplayer_game_engine.zip scripts multiplayer_game_engine.jar
rm -rf scripts
rm -rf multiplayer_game_engine.jar

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
