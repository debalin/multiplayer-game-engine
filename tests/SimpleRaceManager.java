package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.characters.SpawnPoint;
import com.debalin.characters.StandingStair;
import com.debalin.engine.*;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.network.GameClient;
import com.debalin.engine.network.GameServer;
import com.debalin.engine.util.TextRenderer;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleRaceManager extends Controller implements TextRenderer {

  public Player player;
  public SpawnPoint playerSpawnPoint;
  public Map<Integer, GameObject> otherPlayers;
  public Queue<GameObject> fallingStairs;
  public List<GameObject> standingStairs;
  public boolean serverMode;
  public GameServer gameServer;
  public GameClient gameClient;

  private int fallingStairsObjectID;
  private int standingStairsObjectID;
  private int otherPlayersObjectID;
  private int playerObjectID;

  private Integer clientConnectionID;
  Map<Integer, Queue<GameObject>> fromServerWriteQueues;

  DecimalFormat dateFormat;

  private int metricMaxStairsOnScreen = 0;

  public SimpleRaceManager(boolean serverMode) {
    this.serverMode = serverMode;
    fallingStairs = new ConcurrentLinkedQueue<>();
    standingStairs = new LinkedList<>();
    otherPlayers = new ConcurrentHashMap<>();
    fallingStairsObjectID = standingStairsObjectID = otherPlayersObjectID = playerObjectID = -1;

    clientConnectionID = -1;
    fromServerWriteQueues = new HashMap<>();

    dateFormat = new DecimalFormat();
    dateFormat.setMaximumFractionDigits(2);
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

  public String getTextContent() {
    String content = "";

    for (int index : otherPlayers.keySet()) {
      Player player = (Player) otherPlayers.get(index);
      content += "Player " + index + ": " + dateFormat.format(player.getScore()) + "\n";
    }

    content += "\nMy score: " + dateFormat.format(player.getScore());

    return content;
  }

  public PVector getTextPosition() {
    return Constants.SCORE_POSITION;
  }

  private void registerConstants() {
    System.out.println("Registering constants.");
    MainEngine.registerConstants(Constants.CLIENT_RESOLUTION, Constants.SERVER_RESOLUTION, Constants.SMOOTH_FACTOR, Constants.BACKGROUND_RGB, serverMode);
  }

  private void registerPlayer() {
    System.out.println("Registering Player.");
    playerObjectID = engine.registerGameObject(player, playerObjectID, true);
  }

  public Queue<GameObject> sendDataFromServer(int connectionID) {
    if (fromServerWriteQueues.get(connectionID) == null) {
      Queue<GameObject> fromServerWriteQueue = new LinkedList<>();
      synchronized(fromServerWriteQueues) {
        fromServerWriteQueues.put(connectionID, fromServerWriteQueue);
        synchronized (fromServerWriteQueue) {
          fromServerWriteQueue.addAll(standingStairs);
        }
      }
    }

    return fromServerWriteQueues.get(connectionID);
  }

  public void getDataFromServer(Queue<GameObject> gameObjects, int connectionID) {
    synchronized (clientConnectionID) {
      clientConnectionID = connectionID;
    }

    for (GameObject gameObject : gameObjects) {
      if (gameObject.getClass().getTypeName().equals(FallingStair.class.getTypeName())) {
        ((FallingStair) gameObject).engine = engine;
        fallingStairsObjectID = engine.registerGameObject(gameObject, fallingStairsObjectID, true);
        fallingStairs.add(gameObject);
        int temp = fallingStairs.size() + standingStairs.size();
        metricMaxStairsOnScreen = (temp > metricMaxStairsOnScreen) ? temp : metricMaxStairsOnScreen;
      }
      else if (gameObject.getClass().getTypeName().equals(StandingStair.class.getTypeName())) {
        ((StandingStair) gameObject).engine = engine;
        standingStairs.add(gameObject);
        standingStairsObjectID = engine.registerGameObject(gameObject, standingStairsObjectID, false);
      }
      else {
        Player player = (Player) gameObject;
        player.engine = engine;
        int playerIndex = player.getConnectionID();

        if (playerIndex != connectionID) {
          otherPlayers.put(playerIndex, player);
        }
      }
    }
  }

  public Queue<GameObject> sendDataFromClient() {
    Queue<GameObject> dataToSend = new LinkedList<>();
    synchronized (clientConnectionID) {
      if (clientConnectionID == -1)
        return null;
      player.setConnectionID(clientConnectionID);
    }
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
      registerTextRenderers();
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

  private void registerTextRenderers() {
    System.out.println("Registering text renderers.");
    engine.registerTextRenderer(this);
  }

  private void spawnStandingStairs() {
    for (int i = 0; i < Constants.STANDING_STAIR_COUNT; i++) {
      PVector stairColor = new PVector((int) engine.random(0, 255), (int) engine.random(0, 255), (int) engine.random(0, 255));
      PVector stairInitPosition = new PVector(engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.x - Constants.STANDING_STAIR_SIZE.x - Constants.STAIR_PADDING_X), engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.y - Constants.STANDING_STAIR_SIZE.x - Constants.STAIR_PADDING_X));
      StandingStair stair = new StandingStair(engine, stairColor, stairInitPosition);

      standingStairs.add(stair);
      standingStairsObjectID = engine.registerGameObject(stair, standingStairsObjectID, false);
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
      synchronized (fromServerWriteQueues) {
        for (Queue<GameObject> fromServerWriteQueue : fromServerWriteQueues.values()) {
          synchronized (fromServerWriteQueue) {
            fromServerWriteQueue.addAll(otherPlayers.values());
            fromServerWriteQueue.notify();
          }
        }
      }
    }
    else {
      registerOtherPlayers();
    }
    removeStairs();

    if (!serverMode) {
      if (fallingStairs.size() <= 0 && fallingStairsObjectID != -1) {
        System.out.println("10000 objects received: " + System.currentTimeMillis() + " Max objects on screen: " + metricMaxStairsOnScreen + " connection ID: " + clientConnectionID);
        System.exit(1);
      }
    }
  }

  private void registerOtherPlayers() {
    if (otherPlayers.size() > 0) {
      otherPlayersObjectID = engine.registerGameObjects((new LinkedList<>(otherPlayers.values())), otherPlayersObjectID, false);
    }
  }

  private void removeStairs() {
    Iterator<GameObject> i = fallingStairs.iterator();
    while (i.hasNext()) {
      FallingStair stair = (FallingStair) i.next();
      if (!stair.isVisible())
        i.remove();
    }
  }

  private void spawnFallingStair() {
    PVector stairColor = new PVector((int)engine.random(0, 255), (int)engine.random(0, 255), (int)engine.random(0, 255));
    PVector stairInitPosition = new PVector(engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.x - Constants.FALLING_STAIR_SIZE.x - Constants.STAIR_PADDING_X), Constants.FALLING_STAIR_START_Y);
    FallingStair stair = new FallingStair(engine, stairColor, stairInitPosition);

    fallingStairs.add(stair);
    synchronized (fromServerWriteQueues) {
      for (Queue<GameObject> fromServerWriteQueue : fromServerWriteQueues.values()) {
        synchronized (fromServerWriteQueue) {
          fromServerWriteQueue.add(stair);
          fromServerWriteQueue.notify();
        }
      }
    }

    fallingStairsObjectID = engine.registerGameObject(stair, fallingStairsObjectID, true);
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