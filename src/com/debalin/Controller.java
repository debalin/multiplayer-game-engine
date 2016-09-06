package com.debalin;

import com.debalin.characters.Player;
import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;

public class Controller {

  public MainEngine engine;
  public Player player;

  public static void main(String args[]) {
    Controller controller = new Controller();
    controller.registerConstants();
    controller.startEngine();
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

  public void setEngine(MainEngine engine) {
    this.engine = engine;
    initializePlayer();
    registerCharacters();
  }

  private void initializePlayer() {
    player = new Player(engine);
  }

}
