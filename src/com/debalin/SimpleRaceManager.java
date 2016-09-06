package com.debalin;

import com.debalin.characters.Player;
import com.debalin.engine.Controller;
import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;

public class SimpleRaceManager extends Controller{

  public Player player;

  public static void main(String args[]) {
    SimpleRaceManager simpleRaceManager = new SimpleRaceManager();
    simpleRaceManager.registerConstants();
    simpleRaceManager.startEngine();
  }

  private void startEngine() {
    MainEngine.startEngine(this);
  }

  private void registerConstants() {
    MainEngine.registerConstants(Constants.RESOLUTION, Constants.SMOOTH_FACTOR, Constants.BACKGROUND_RGB);
  }

  private void registerCharacters() {
    MainEngine.registerGameObject(player);
  }

  public void initialize() {
    initializePlayer();
    registerCharacters();
  }

  private void initializePlayer() {
    player = new Player(engine);
  }

}