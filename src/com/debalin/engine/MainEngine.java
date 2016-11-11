package com.debalin.engine;

import com.debalin.engine.events.Event;
import com.debalin.engine.events.EventManager;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.network.GameClient;
import com.debalin.engine.network.GameServer;
import com.debalin.engine.timeline.Timeline;
import com.debalin.engine.util.EngineConstants;
import com.debalin.engine.util.TextRenderer;
import processing.core.*;

import java.io.*;
import java.util.*;

public class MainEngine extends PApplet {

  public List<Queue<GameObject>> gameObjectsCluster, gameObjectsClusterBackup;
  public List<Queue<GameObject>> gameObjectsClusterSnapshot;
  ByteArrayOutputStream snapshot;

  public List<TextRenderer> textRenderers;
  public static Controller controller;
  public GameServer gameServer;
  public GameClient gameClient;
  public List<Boolean> updateOrNotArray;
  public static boolean serverMode;

  public static PVector clientResolution;
  public static PVector serverResolution;
  public static PVector backgroundRGB;
  public static int smoothFactor;

  public Timeline realTimelineInMillis, gameTimelineInMillis, totalTimelineInFrames, gameTimelineInFrames;
  private EventManager eventManager;
  public enum ReplaySpeed {
    SLOW, NORMAL, FAST
  }
  public ReplaySpeed replaySpeed;

  public float targetAlpha = 0, signToggle;
  PFont recFont, instructionsFont;

  public MainEngine() {
    gameObjectsCluster = new ArrayList<>();
    gameObjectsClusterBackup = new ArrayList<>();
    gameObjectsClusterSnapshot = new ArrayList<>();
    updateOrNotArray = new ArrayList<>();
    textRenderers = new ArrayList<>();
    eventManager = new EventManager(this);
    replaySpeed = ReplaySpeed.NORMAL;
  }

  public void takeSnapshot() {
    try {
      snapshot = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(snapshot);

      synchronized (gameObjectsCluster) {
        objectOutputStream.writeObject(gameObjectsCluster);
        objectOutputStream.flush();
        objectOutputStream.close();
      }
    } catch (IOException e) {
    }

    System.out.println("Snapshot taken and length is " + snapshot.toByteArray().length + ".");
  }

  public int registerGameObject(GameObject gameObject, int gameObjectListID, boolean update) {
    synchronized (gameObjectsCluster) {
      if (gameObjectListID == -1) {
        gameObjectListID = gameObjectsCluster.size();
        gameObjectsCluster.add(new LinkedList<>());
        updateOrNotArray.add(update);
      }
      gameObjectsCluster.get(gameObjectListID).add(gameObject);
    }

    return gameObjectListID;
  }

  public void removeGameObjects(int gameObjectListID) {
    synchronized (gameObjectsCluster) {
      gameObjectsCluster.get(gameObjectListID).clear();
    }
  }

  public void registerTextRenderer(TextRenderer textRenderer) {
    textRenderers.add(textRenderer);
  }

  public static void registerConstants(PVector inputClientResolution, PVector inputServerResolution, int inputSmoothFactor, PVector inputBackgroundRGB, boolean serverModeInput) {
    clientResolution = inputClientResolution.copy();
    serverResolution = inputServerResolution.copy();
    smoothFactor = inputSmoothFactor;
    backgroundRGB = inputBackgroundRGB.copy();

    serverMode = serverModeInput;
  }

  public static void startEngine(Controller inputController) {
    if (controller != null) {
      System.out.println("Controller already set, won't be starting engine again.");
      return;
    }

    controller = inputController;
    PApplet.main(new String[]{"com.debalin.engine.MainEngine"});
  }

  public void settings() {
    if (!serverMode)
      size((int) clientResolution.x, (int) clientResolution.y, P2D);
    else
      size((int) serverResolution.x, (int) serverResolution.y, P2D);

    smooth(smoothFactor);
  }

  public void setup() {
    frameRate(40);
    if (!MainEngine.serverMode) {
      recFont = createFont("Verdana", 25);
      instructionsFont = createFont("Verdana", 16);
    }
    controller.setEngine(this);
    controller.registerServerOrClient();
    if (MainEngine.serverMode)
      realTimelineInMillis = new Timeline(null, 1000000, Timeline.TimelineIterationTypes.REAL, this);
    startServers();
    startTimelines();
    controller.setup();
  }

  private void startTimelines() {
    if (!MainEngine.serverMode) {
      while (realTimelineInMillis == null) {
        try {
          Thread.sleep(1);
        } catch (Exception ex) {
        }
      }
    }
    gameTimelineInMillis = new Timeline(realTimelineInMillis, 1000000, Timeline.TimelineIterationTypes.REAL, this);
    totalTimelineInFrames = new Timeline(null, 1, Timeline.TimelineIterationTypes.LOOP, this);
    gameTimelineInFrames = new Timeline(totalTimelineInFrames, 1, Timeline.TimelineIterationTypes.LOOP, this);

    Queue<GameObject> timelines = new LinkedList<>();
    timelines.add(gameTimelineInMillis);
    timelines.add(gameTimelineInFrames);

    eventManager.registerTimeline(gameTimelineInMillis, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString());

    registerGameObjects(timelines, -1, true);
  }

  private void startServers() {
    if (gameServer != null)
      (new Thread(gameServer)).start();
    if (gameClient != null)
      (new Thread(gameClient)).start();
  }

  public void draw() {
    background(backgroundRGB.x, backgroundRGB.y, backgroundRGB.z);
    controller.manage();
    updatePositions();

    if (!serverMode) {
      drawText();
      drawShapes();
      eventManager.handleEvents();
    }
  }

  private void drawText() {

    if (eventManager.recording) {
      pushMatrix();
      textFont(recFont);
      noStroke();
      fill(255, 0, 0);
      text("REC", clientResolution.x - 110, 90);
      fill(255, 0, 0, targetAlpha);
      ellipse(clientResolution.x - 125, 82, 20, 20);
      popMatrix();

      if (targetAlpha <= 0.0)
        signToggle = 2;
      else if (targetAlpha >= 255.0)
        signToggle = -2;
      targetAlpha += signToggle;
    } else if (eventManager.playingRecording) {
      pushMatrix();
      noStroke();
      fill(255, 0, 0, targetAlpha);
      triangle(clientResolution.x - 80, 82, clientResolution.x - 100, 72, clientResolution.x - 100, 92);
      popMatrix();

      if (targetAlpha <= 0.0)
        signToggle = 2;
      else if (targetAlpha >= 255.0)
        signToggle = -2;
      targetAlpha += signToggle;
    }

    pushMatrix();
    textFont(instructionsFont);
    noStroke();
    fill(255, 255, 255);
    text("R: RECORD", clientResolution.x - 140, clientResolution.y - 90);
    text("H: HALT", clientResolution.x - 140, clientResolution.y - 70);
    text("N: NORMAL PLAY", clientResolution.x - 140, clientResolution.y - 50);
    popMatrix();

    for (TextRenderer textRenderer : textRenderers) {
      String content = textRenderer.getTextContent();
      PVector position = textRenderer.getTextPosition();

      pushMatrix();
      fill(255, 255, 255);
      text(content, position.x, position.y);
      popMatrix();
    }
  }

  private void updatePositions() {
    int count = 0;
    synchronized (gameObjectsCluster) {
      for (Queue<GameObject> gameObjects : gameObjectsCluster) {
        if (updateOrNotArray.get(count)) {
          Iterator<GameObject> i = gameObjects.iterator();
          while (i.hasNext()) {
            GameObject gameObject = i.next();
            if (!gameObject.isVisible()) {
              i.remove();
            } else {
              if (!eventManager.playingRecording)
                gameObject.update(1f);
              else {
                switch (replaySpeed) {
                  case SLOW:
                    gameObject.update(0.5f);
                    break;
                  case NORMAL:
                    gameObject.update(1f);
                    break;
                  case FAST:
                    gameObject.update(2f);
                    break;
                }
              }
            }
          }
        }
        count++;
      }
    }
  }

  public int registerGameObjects(Queue<GameObject> gameObjects, int gameObjectListID, boolean update) {
    synchronized (gameObjectsCluster) {
      if (gameObjectListID == -1) {
        gameObjectListID = gameObjectsCluster.size();
        gameObjectsCluster.add(new LinkedList<>());
        updateOrNotArray.add(update);
      }
      gameObjectsCluster.set(gameObjectListID, gameObjects);
    }

    return gameObjectListID;
  }

  private void drawShapes() {
    synchronized (gameObjectsCluster) {
      gameObjectsCluster.forEach(gameObjects -> gameObjects.forEach(GameObject::draw));
    }
  }

  public void keyPressed() {
    if (serverMode)
      return;
    switch (key) {
      default:
        List<Object> eventParameters = new ArrayList<>();
        eventParameters.add(new Integer(key));
        eventParameters.add(new Boolean(true));

        Event event = new Event(EngineConstants.DEFAULT_EVENT_TYPES.USER_INPUT.toString(), eventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), controller.getClientConnectionID().intValue(), gameTimelineInMillis.getTime(), false);
        eventManager.raiseEvent(event, true);
    }
  }

  public void keyReleased() {
    if (serverMode)
      return;
    switch (key) {
      case 'R':
      case 'r':
        System.out.println("Starting recording.");
        takeSnapshot();
        eventManager.setRecording(true);
        break;
      case 'H':
      case 'h':
        System.out.println("Stopping recording.");
        eventManager.setRecording(false);
        break;
      case 'N':
      case 'n':
        System.out.println("Playing recording in normal speed.");
        replaySpeed = ReplaySpeed.NORMAL;
        playRecordedGameObjects(1f);
        break;
      default:
        List<Object> eventParameters = new ArrayList<>();
        eventParameters.add(new Integer(key));
        eventParameters.add(new Boolean(false));

        Event event = new Event(EngineConstants.DEFAULT_EVENT_TYPES.USER_INPUT.toString(), eventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), controller.getClientConnectionID().intValue(), gameTimelineInMillis.getTime(), false);
        eventManager.raiseEvent(event, true);
    }
  }

  private void playRecordedGameObjects(float frameTicSize) {
    try {
      ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(snapshot.toByteArray()));
      gameObjectsClusterSnapshot = (List<Queue<GameObject>>) objectInputStream.readObject();
    } catch (IOException ex) {
    } catch (ClassNotFoundException ex) {
    }

    for (Queue<GameObject> gameObjects : gameObjectsClusterSnapshot) {
      for (GameObject gameObject : gameObjects) {
        gameObject.engine = this;
      }
    }

    synchronized (gameObjectsCluster) {
      gameObjectsClusterBackup.clear();
      gameObjectsClusterBackup.addAll(gameObjectsCluster);
      gameObjectsCluster.clear();
      gameObjectsCluster.addAll(gameObjectsClusterSnapshot);
    }

    controller.mirrorGameObjects(gameObjectsCluster);
    eventManager.playRecordedEvents(frameTicSize);
  }

  public void stopPlayingRecordedGameObjects() {
    synchronized (gameObjectsCluster) {
      gameObjectsCluster.clear();
      gameObjectsClusterSnapshot.clear();
      gameObjectsCluster.addAll(gameObjectsClusterBackup);
    }

    controller.mirrorGameObjects(gameObjectsCluster);
  }

  public GameClient registerClient(String remoteServerAddress, int remoteServerPort, Controller controller) {
    if (gameClient == null)
      gameClient = new GameClient(remoteServerAddress, remoteServerPort, controller, this);

    return gameClient;
  }

  public GameServer registerServer(int localServerPort, Controller controller) {
    if (gameServer == null)
      gameServer = new GameServer(localServerPort, controller, this);

    return gameServer;
  }

  public EventManager getEventManager() {
    return eventManager;
  }

}
