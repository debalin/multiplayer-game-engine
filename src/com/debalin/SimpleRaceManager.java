package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.engine.Controller;
import com.debalin.engine.GameClient;
import com.debalin.engine.GameServer;
import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class SimpleRaceManager extends Controller {

  public Player player;
  public ArrayList<FallingStair> stairs;
  public boolean serverMode;
  public GameServer gameServer;
  public GameClient gameClient;

  public SimpleRaceManager(boolean serverMode) {
    this.serverMode = serverMode;
    stairs = new ArrayList<>();
  }

  public static void main(String args[]) {
    SimpleRaceManager simpleRaceManager;

    if (args[0].toLowerCase().equals("s")) {
      System.out.println("Starting as server.");
      simpleRaceManager = new SimpleRaceManager(true);
    }
    else {
      System.out.println("Starting as client.");
      simpleRaceManager = new SimpleRaceManager(false);
    }

    simpleRaceManager.startEngine();
  }

  private void startEngine() {
    registerConstants();

    System.out.println("Starting engine.");
    MainEngine.startEngine(this);
  }

  private void registerConstants() {
    System.out.println("Registering constants.");
    MainEngine.registerConstants(Constants.RESOLUTION, Constants.SMOOTH_FACTOR, Constants.BACKGROUND_RGB);
  }

  private void registerPlayer() {
    System.out.println("Registering Player.");
    engine.registerGameObject(player);
  }

  public ArrayList<Serializable> sendDataFromServer() {
    return null;
  }

  public void initialize() {
    initializePlayer();
    registerPlayer();
    registerKeypressUsers();
    if (serverMode) {
      registerServer();
    }
    else {
      registerClient();
    }
  }

  private void registerClient() {
    System.out.println("Registering Client.");
    gameClient = engine.registerClient(Constants.SERVER_ADDRESS, Constants.SERVER_PORT, this);
  }

  private void registerServer() {
    System.out.println("Registering Server.");
    gameServer = engine.registerServer(Constants.SERVER_PORT, this);
  }

  public void manage() {
    if (engine.frameCount % Constants.STAIR_SPAWN_INTERVAL == 0) {
      spawnStair();
    }
    removeStairs();
  }

  private void removeStairs() {
    synchronized (stairs) {
      Iterator<FallingStair> i = stairs.iterator();
      while (i.hasNext()) {
        FallingStair stair = i.next();
        if (!stair.isVISIBLE())
          i.remove();
      }
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
    System.out.println("Registering Keypress Users.");
    engine.registerKeypressUser(player);
  }

  private void initializePlayer() {
    System.out.println("Initializing player.");
    player = new Player(engine, stairs);
  }

}
