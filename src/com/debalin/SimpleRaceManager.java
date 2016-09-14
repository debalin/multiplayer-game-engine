package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.engine.*;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleRaceManager extends Controller {

  public Player player;
  public ConcurrentLinkedQueue<GameObject> stairs;
  public boolean serverMode;
  public GameServer gameServer;
  public GameClient gameClient;

  public SimpleRaceManager(boolean serverMode) {
    this.serverMode = serverMode;
    stairs = new ConcurrentLinkedQueue<>();
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

  public ConcurrentLinkedQueue<GameObject> sendDataFromServer() {
    return stairs;
  }

  public void getDataFromServer(ConcurrentLinkedQueue<GameObject> gameObjects) {
    for (GameObject gameObject : gameObjects) {
      ((FallingStair) gameObject).engine = engine;
    }
    this.stairs = gameObjects;
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
    if (serverMode) {
      if (engine.frameCount % Constants.STAIR_SPAWN_INTERVAL == 0) {
        spawnStair();
      }
      removeStairs();
    }
    else {
      registerStairs();
    }
  }

  private void registerStairs() {
    ArrayList<GameObject> gameObjects = new ArrayList<>();
    gameObjects.add(player);
    gameObjects.addAll(stairs);

    engine.registerGameObjects(gameObjects);
  }

  private void removeStairs() {
    synchronized (stairs) {
      Iterator<GameObject> i = stairs.iterator();
      while (i.hasNext()) {
        FallingStair stair = (FallingStair) i.next();
        if (!stair.isVisible())
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
