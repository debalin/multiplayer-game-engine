package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.characters.SpawnPoint;
import com.debalin.characters.StandingStair;
import com.debalin.engine.*;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.network.GameClient;
import com.debalin.engine.network.GameServer;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleRaceManager extends Controller {

  public Player player;
  public SpawnPoint playerSpawnPoint;
  public Map<Integer, GameObject> otherPlayers;
  public Queue<GameObject> fallingStairs;
  public Queue<GameObject> standingStairs;
  public boolean serverMode;
  public GameServer gameServer;
  public GameClient gameClient;

  private int fallingStairsObjectID;
  private int standingStairsObjectID;
  private int otherPlayersObjectID;
  private int playerObjectID;

  private int clientConnectionID;

  private boolean oneTimeSend = false;

  public SimpleRaceManager(boolean serverMode) {
    this.serverMode = serverMode;
    fallingStairs = new ConcurrentLinkedQueue<>();
    standingStairs = new ConcurrentLinkedQueue<>();
    otherPlayers = new ConcurrentHashMap<>();

    fallingStairsObjectID = standingStairsObjectID = otherPlayersObjectID = playerObjectID = -1;

    clientConnectionID = -1;
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
    MainEngine.registerConstants(Constants.CLIENT_RESOLUTION, Constants.SERVER_RESOLUTION, Constants.SMOOTH_FACTOR, Constants.BACKGROUND_RGB, serverMode);
  }

  private void registerPlayer() {
    System.out.println("Registering Player.");

    if (playerObjectID == -1)
      playerObjectID = engine.registerGameObject(player, playerObjectID, true);
    else
      engine.registerGameObject(player, playerObjectID, true);
  }

  public Queue<GameObject> sendDataFromServer() {
    Queue<GameObject> dataToSend = new ConcurrentLinkedQueue<>();
    dataToSend.addAll(fallingStairs);
    dataToSend.addAll(otherPlayers.values());
    if (!oneTimeSend) {
      dataToSend.addAll(standingStairs);
      oneTimeSend = true;
    }

    return dataToSend;
  }

  public void getDataFromServer(Queue<GameObject> gameObjects, int connectionID) {
    clientConnectionID = connectionID;

    fallingStairs.clear();

    int i = 0;
    for (GameObject gameObject : gameObjects) {
      if (gameObject.getClass().getTypeName().equals(FallingStair.class.getTypeName())) {
        ((FallingStair) gameObject).engine = engine;
        fallingStairs.add(gameObject);
      }
      else if (gameObject.getClass().getTypeName().equals(StandingStair.class.getTypeName())) {
        ((StandingStair) gameObject).engine = engine;
        standingStairs.add(gameObject);
      }
      else {
        Player player = (Player) gameObject;
        player.engine = engine;
        int playerIndex = i - fallingStairs.size();

        if (playerIndex != connectionID)
          otherPlayers.put(playerIndex, player);
      }
      i++;
    }
  }

  public Queue<GameObject> sendDataFromClient() {
    Queue<GameObject> dataToSend = new ConcurrentLinkedQueue<>();
    dataToSend.add(player);
    return dataToSend;
  }

  public void getDataFromClient(Queue<GameObject> gameObjects, int connectionID) {
    Player player = (Player) gameObjects.poll();
    player.engine = engine;
    otherPlayers.put(connectionID, player);
  }

  public void initialize() {
    if (!serverMode) {
      initializePlayer();
      registerPlayer();
      registerKeypressUsers();
    }
    else {
      spawnStandingStairs();
    }

    if (serverMode) {
      registerServer();
    }
    else {
      registerClient();
    }
  }

  private void spawnStandingStairs() {
    for (int i = 0; i < Constants.STANDING_STAIR_COUNT; i++) {
      PVector stairColor = new PVector((int) engine.random(0, 255), (int) engine.random(0, 255), (int) engine.random(0, 255));
      PVector stairInitPosition = new PVector(engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.x - Constants.STANDING_STAIR_SIZE.x - Constants.STAIR_PADDING_X), engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.y - Constants.STANDING_STAIR_SIZE.x - Constants.STAIR_PADDING_X));
      StandingStair stair = new StandingStair(engine, stairColor, stairInitPosition);

      standingStairs.add(stair);

      if (standingStairsObjectID == -1) {
        standingStairsObjectID = engine.registerGameObject(stair, standingStairsObjectID, false);
      } else {
        engine.registerGameObject(stair, standingStairsObjectID, false);
      }
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
      if (engine.frameCount % Constants.FALLING_STAIR_SPAWN_INTERVAL == 0) {
        spawnFallingStair();
      }
      removeStairs();
    }
    else {
      registerFallingStairsForClient();
      registerStandingStairsForClient();
    }

    registerOtherPlayers();
  }

  private void registerOtherPlayers() {
    if (otherPlayers.size() > 0) {
      if (otherPlayersObjectID == -1) {
        otherPlayersObjectID = engine.registerGameObjects((new ArrayList<>(otherPlayers.values())), otherPlayersObjectID, false);
      } else {
        engine.registerGameObjects((new ArrayList<>(otherPlayers.values())), otherPlayersObjectID, false);
      }
    }
  }

  private void registerFallingStairsForClient() {
    List<GameObject> gameObjects = new LinkedList<>();
    gameObjects.addAll(fallingStairs);

    if (fallingStairsObjectID == -1) {
      fallingStairsObjectID = engine.registerGameObjects(gameObjects, fallingStairsObjectID, false);
    }
    else {
      engine.registerGameObjects(gameObjects, fallingStairsObjectID, false);
    }
  }

  private void registerStandingStairsForClient() {
    if (standingStairsObjectID == -1 && standingStairs.size() > 0) {
      List<GameObject> gameObjects = new LinkedList<>();
      gameObjects.addAll(standingStairs);

      standingStairsObjectID = engine.registerGameObjects(gameObjects, standingStairsObjectID, false);
    }
  }

  private void removeStairs() {
    synchronized (fallingStairs) {
      Iterator<GameObject> i = fallingStairs.iterator();
      while (i.hasNext()) {
        FallingStair stair = (FallingStair) i.next();
        if (!stair.isVisible())
          i.remove();
      }
    }
  }

  private void spawnFallingStair() {
    PVector stairColor = new PVector((int)engine.random(0, 255), (int)engine.random(0, 255), (int)engine.random(0, 255));
    PVector stairInitPosition = new PVector(engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.x - Constants.FALLING_STAIR_SIZE.x - Constants.STAIR_PADDING_X), Constants.FALLING_STAIR_START_Y);
    FallingStair stair = new FallingStair(engine, stairColor, stairInitPosition);

    fallingStairs.add(stair);

    if (fallingStairsObjectID == -1) {
      fallingStairsObjectID = engine.registerGameObject(stair, fallingStairsObjectID, true);
    }
    else {
      engine.registerGameObject(stair, fallingStairsObjectID, true);
    }
  }

  private void registerKeypressUsers() {
    System.out.println("Registering Keypress Users.");
    engine.registerKeypressUser(player);
  }

  private void initializePlayer() {
    System.out.println("Initializing player.");
    playerSpawnPoint = new SpawnPoint(new PVector(engine.random(Constants.PLAYER_PADDING_X, Constants.CLIENT_RESOLUTION.x - Constants.PLAYER_PADDING_X), Constants.PLAYER_SPAWN_Y));
    player = new Player(engine, playerSpawnPoint, fallingStairs, standingStairs);
  }

}
