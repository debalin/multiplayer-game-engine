package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.characters.SpawnPoint;
import com.debalin.characters.StandingStair;
import com.debalin.engine.*;
import com.debalin.engine.events.Event;
import com.debalin.engine.events.EventHandler;
import com.debalin.engine.events.EventManager;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.network.GameClient;
import com.debalin.engine.network.GameServer;
import com.debalin.engine.util.EngineConstants;
import com.debalin.engine.util.TextRenderer;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleRaceManager extends Controller implements TextRenderer {

  public Player player;
  public SpawnPoint playerSpawnPoint;
  public Map<Integer, GameObject> otherPlayers;
  public Queue<GameObject> fallingStairs;
  public List<GameObject> standingStairs;
  public Map<Long, GameObject> stairMap;
  public long stairCount;
  public boolean serverMode;
  public GameServer gameServer;
  public GameClient gameClient;

  public int fallingStairsObjectID;
  public int standingStairsObjectID;
  public int otherPlayersObjectID;
  public int playerObjectID;

  Map<Integer, Queue<Event>> fromServerWriteQueues;

  DecimalFormat dateFormat;

  private EventHandler eventHandler;

  public SimpleRaceManager(boolean serverMode) {
    this.serverMode = serverMode;
    fallingStairs = new ConcurrentLinkedQueue<>();
    standingStairs = new LinkedList<>();
    stairMap = new HashMap<>();
    stairCount = 0;
    otherPlayers = new ConcurrentHashMap<>();
    fallingStairsObjectID = standingStairsObjectID = otherPlayersObjectID = playerObjectID = -1;
    fromServerWriteQueues = new HashMap<>();

    dateFormat = new DecimalFormat();
    dateFormat.setMaximumFractionDigits(2);

    eventHandler = new GameEventHandler(this);
  }

  public static void main(String args[]) {
    SimpleRaceManager simpleRaceManager;

    if (args[0].toLowerCase().equals("s")) {
      System.out.println("Starting as server.");
      simpleRaceManager = new SimpleRaceManager(true);
    } else {
      System.out.println("Starting as client.");
      simpleRaceManager = new SimpleRaceManager(false);
    }

    simpleRaceManager.startEngine();
  }

  @Override
  public void mirrorGameObjects(List<Queue<GameObject>> gameObjectsCluster) {
    stairMap.clear();
    synchronized (gameObjectsCluster) {
      for (int i = 0; i <= gameObjectsCluster.size() - 1; i++) {
        Queue<GameObject> gameObjects = gameObjectsCluster.get(i);
        GameObject gameObject = gameObjects.peek();
        if (gameObject == null)
          continue;
        String type = gameObject.getClass().getTypeName();
        if (type.equals(Player.class.getTypeName()) && gameObject.getConnectionID() == getClientConnectionID().intValue()) {
          playerObjectID = i;
          player = (Player) gameObject;
          player.fallingStairs = fallingStairs;
          player.standingStairs = standingStairs;
        } else if (type.equals(Player.class.getTypeName()) && gameObject.getConnectionID() != getClientConnectionID().intValue()) {
          otherPlayersObjectID = i;
          for (GameObject otherPlayer : gameObjects) {
            ((Player) otherPlayer).standingStairs = standingStairs;
            ((Player) otherPlayer).fallingStairs = fallingStairs;
            otherPlayers.put(otherPlayer.getConnectionID(), otherPlayer);
          }
        } else if (type.equals(FallingStair.class.getTypeName())) {
          fallingStairsObjectID = i;
          fallingStairs.clear();
          fallingStairs.addAll(gameObjects);
          for (GameObject stair : fallingStairs) {
            stairMap.put(((FallingStair) stair).getStairID(), stair);
          }
        } else if (type.equals(StandingStair.class.getTypeName())) {
          standingStairsObjectID = i;
          standingStairs.clear();
          standingStairs.addAll(gameObjects);
          for (GameObject stair : standingStairs) {
            stairMap.put(((StandingStair) stair).getStairID(), stair);
          }
        }
      }
    }
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
      content += "Player " + index + ": " + dateFormat.format(player.score) + "\n";
    }

    if (player != null)
      content += "\nMy score: " + dateFormat.format(player.score);
    else
      content += "\nMy score: " + dateFormat.format(0);

    return content;
  }

  public PVector getTextPosition() {
    return Constants.SCORE_POSITION;
  }

  private void registerConstants() {
    System.out.println("Registering constants.");
    MainEngine.registerConstants(Constants.CLIENT_RESOLUTION, Constants.SERVER_RESOLUTION, Constants.SMOOTH_FACTOR, Constants.BACKGROUND_RGB, serverMode);
  }

  @Override
  public void setup() {
    registerEventTypes();

    if (!serverMode) {
      AtomicInteger clientConnectionID = getClientConnectionID();
      synchronized (clientConnectionID) {
        try {
          while (clientConnectionID.intValue() == -1)
            clientConnectionID.wait();
        } catch (InterruptedException ex) {
        }
      }
      System.out.println("Connection ID is " + getClientConnectionID() + ".");
      initializePlayer();
    } else {
      spawnStandingStairs();
    }
  }

  public void registerServerOrClient() {
    if (serverMode) {
      System.out.println("Registering Server.");
      gameServer = engine.registerServer(Constants.SERVER_PORT, this);
    } else {
      System.out.println("Registering Client.");
      gameClient = engine.registerClient(Constants.SERVER_ADDRESS, Constants.SERVER_PORT, this);
    }
  }

  private void registerEventTypes() {
    engine.getEventManager().registerEventType(Constants.EVENT_TYPES.PLAYER_DEATH.toString(), EventManager.EventPriorities.HIGH);
    engine.getEventManager().registerEventType(Constants.EVENT_TYPES.PLAYER_COLLISION.toString(), EventManager.EventPriorities.MED);
    engine.getEventManager().registerEventType(Constants.EVENT_TYPES.STAIR_SPAWN.toString(), EventManager.EventPriorities.LOW);
    engine.getEventManager().registerEventType(Constants.EVENT_TYPES.PLAYER_SPAWN.toString(), EventManager.EventPriorities.HIGH);
  }

  private void registerTextRenderers() {
    System.out.println("Registering text renderers.");
    engine.registerTextRenderer(this);
  }

  private void spawnStandingStairs() {
    for (int i = 0; i < Constants.STANDING_STAIR_COUNT; i++) {
      PVector stairColor = new PVector((int) engine.random(0, 255), (int) engine.random(0, 255), (int) engine.random(0, 255));
      PVector stairInitPosition = new PVector(engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.x - Constants.STANDING_STAIR_SIZE.x - Constants.STAIR_PADDING_X), engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.y - Constants.STANDING_STAIR_SIZE.x - Constants.STAIR_PADDING_X));
      String eventType = Constants.EVENT_TYPES.STAIR_SPAWN.toString();
      List<Object> eventParameters = new ArrayList<>();
      eventParameters.add(stairCount++);
      eventParameters.add(false);
      eventParameters.add(stairColor);
      eventParameters.add(stairInitPosition);
      Event event = new Event(eventType, eventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), getClientConnectionID().intValue(), engine.gameTimelineInMillis.getTime(), true);
      engine.getEventManager().raiseEvent(event, true);
    }
  }

  public void manage() {
    if (serverMode) {
      if (engine.frameCount % Constants.FALLING_STAIR_SPAWN_INTERVAL == 0) {
        spawnFallingStair();
      }
    }
    removeStairs();
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
    PVector stairColor = new PVector((int) engine.random(0, 255), (int) engine.random(0, 255), (int) engine.random(0, 255));
    PVector stairInitPosition = new PVector(engine.random(Constants.STAIR_PADDING_X, Constants.CLIENT_RESOLUTION.x - Constants.FALLING_STAIR_SIZE.x - Constants.STAIR_PADDING_X), Constants.FALLING_STAIR_START_Y);
    PVector stairVelocity = new PVector(0, engine.random(Constants.FALLING_STAIR_MIN_VEL_Y, Constants.FALLING_STAIR_MAX_VEL_Y));

    String eventType = Constants.EVENT_TYPES.STAIR_SPAWN.toString();
    List<Object> eventParameters = new ArrayList<>();
    eventParameters.add(stairCount++);
    eventParameters.add(true);
    eventParameters.add(stairColor);
    eventParameters.add(stairInitPosition);
    eventParameters.add(stairVelocity);
    if (engine.random(0, 1) > Constants.DEATH_STAIR_PROBABILITY)
      eventParameters.add(true);
    else
      eventParameters.add(false);
    Event event = new Event(eventType, eventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), getClientConnectionID().intValue(), engine.gameTimelineInMillis.getTime(), false);

    engine.getEventManager().raiseEvent(event, true);
  }

  private void initializePlayer() {
    System.out.println("Initializing player.");
    playerSpawnPoint = new SpawnPoint(new PVector(engine.random(Constants.PLAYER_PADDING_X, Constants.CLIENT_RESOLUTION.x - Constants.PLAYER_PADDING_X), Constants.PLAYER_SPAWN_Y));
    PVector playerColor = new PVector((int) engine.random(0, 255), (int) engine.random(0, 255), (int) engine.random(0, 255));

    String eventType = Constants.EVENT_TYPES.PLAYER_SPAWN.toString();
    List<Object> eventParameters = new ArrayList<>();

    eventParameters.add(playerSpawnPoint);
    eventParameters.add(playerColor);
    Event event = new Event(eventType, eventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), getClientConnectionID().intValue(), engine.gameTimelineInMillis.getTime(), true);

    engine.getEventManager().raiseEvent(event, true);
  }

  @Override
  public EventHandler getEventHandler() {
    return eventHandler;
  }

}