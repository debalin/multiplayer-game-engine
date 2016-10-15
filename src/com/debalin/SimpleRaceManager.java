package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
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
  public Map<Integer, GameObject> otherPlayers;
  public Queue<GameObject> stairs;
  public boolean serverMode;
  public GameServer gameServer;
  public GameClient gameClient;

  private int stairsObjectID;
  private int otherPlayersObjectID;
  private int playerObjectID;

  private int clientConnectionID;

  public SimpleRaceManager(boolean serverMode) {
    this.serverMode = serverMode;
    stairs = new ConcurrentLinkedQueue<>();
    otherPlayers = new ConcurrentHashMap<>();

    stairsObjectID = otherPlayersObjectID = playerObjectID = -1;

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
    dataToSend.addAll(stairs);
    dataToSend.addAll(otherPlayers.values());

    return dataToSend;
  }

  public void getDataFromServer(Queue<GameObject> gameObjects, int connectionID) {
    clientConnectionID = connectionID;

    this.stairs.clear();

    int i = 0;
    for (GameObject gameObject : gameObjects) {
      if (gameObject.getClass().getTypeName().equals(FallingStair.class.getTypeName())) {
        ((FallingStair) gameObject).engine = engine;
        this.stairs.add(gameObject);
      }
      else {
        Player player = (Player) gameObject;
        player.engine = engine;
        int playerIndex = i - this.stairs.size();

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
      registerStairsForClient();
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

  private void registerStairsForClient() {
    List<GameObject> gameObjects = new LinkedList<>();
    gameObjects.addAll(stairs);

    if (stairsObjectID == -1) {
      stairsObjectID = engine.registerGameObjects(gameObjects, stairsObjectID, false);
    }
    else {
      engine.registerGameObjects(gameObjects, stairsObjectID, false);
    }
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
    PVector stairInitPosition = new PVector(engine.random(Constants.STAIR_PADDING, Constants.CLIENT_RESOLUTION.x - Constants.STAIR_SIZE.y - Constants.STAIR_PADDING), Constants.STAIR_START_Y);
    FallingStair stair = new FallingStair(engine, stairColor, stairInitPosition);

    stairs.add(stair);

    if (stairsObjectID == -1) {
      stairsObjectID = engine.registerGameObject(stair, stairsObjectID, true);
    }
    else {
      engine.registerGameObject(stair, stairsObjectID, true);
    }
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
