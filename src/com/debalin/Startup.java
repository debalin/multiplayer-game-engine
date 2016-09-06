package com.debalin;

import com.debalin.characters.Player;
import com.debalin.engine.MainEngine;

public class Startup {

  public MainEngine engine;
  public Player player;

  public static void main(String args[]) {
    Startup startup = new Startup();
    startup.initializeEngine();
    startup.initializePlayer();
    startup.registerCharacters();
  }

  private void registerCharacters() {
    engine.registerGameObject(player);
  }

  private void initializeEngine() {
    engine = new MainEngine();
  }

  private void initializePlayer() {
    player = new Player(engine);
  }

}
