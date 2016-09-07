package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.engine.Controller;
import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.util.ArrayList;

public class SimpleRaceManager extends Controller{

  public Player player;
  public ArrayList<FallingStair> stairs;

  public SimpleRaceManager() {
    stairs = new ArrayList<>();
  }

  public static void main(String args[]) {
    SimpleRaceManager simpleRaceManager = new SimpleRaceManager();
    simpleRaceManager.startEngine();
  }

  private void startEngine() {
    registerConstants();
    MainEngine.startEngine(this);
  }

  private void registerConstants() {
    MainEngine.registerConstants(Constants.RESOLUTION, Constants.SMOOTH_FACTOR, Constants.BACKGROUND_RGB);
  }

  private void registerPlayer() {
    engine.registerGameObject(player);
  }

  public void initialize() {
    initializePlayer();
    registerPlayer();
    registerKeypressUsers();
  }

  public void manage() {
    if (engine.frameCount % Constants.STAIR_SPAWN_INTERVAL == 0) {
      spawnStair();
    }
  }

  private void spawnStair() {
    PVector stairColor = new PVector((int)engine.random(0, 255), (int)engine.random(0, 255), (int)engine.random(0, 255));
    PVector stairInitPosition = new PVector(engine.random(Constants.STAIR_PADDING, Constants.RESOLUTION.x - Constants.STAIR_SIZE.y - Constants.STAIR_PADDING), Constants.STAIR_START_Y);
    FallingStair stair = new FallingStair(engine, stairColor, stairInitPosition);

    stairs.add(stair);
    engine.registerGameObject(stair);
  }

  private void registerKeypressUsers() {
    engine.registerKeypressUser(player);
  }

  private void initializePlayer() {
    player = new Player(engine);
  }

}
