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

import java.util.*;

public class MainEngine extends PApplet {

  public List<Queue<GameObject>> gameObjectsCluster;
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

  public Timeline realTimelineInMillis, gameTimelineInMillis, gameTimelineInFrames;
  private EventManager eventManager;

  public MainEngine() {
    gameObjectsCluster = new ArrayList<>();
    updateOrNotArray = new ArrayList<>();
    textRenderers = new ArrayList<>();
    eventManager = new EventManager(this);
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

  public void registerTextRenderer(TextRenderer textRenderer) { textRenderers.add(textRenderer); }

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
    PApplet.main(new String[] { "com.debalin.engine.MainEngine" });
  }

  public void settings() {
    if (!serverMode)
      size((int) clientResolution.x, (int) clientResolution.y, P2D);
    else
      size((int) serverResolution.x, (int) serverResolution.y, P2D);

    smooth(smoothFactor);
  }

  public void setup() {
    controller.setEngine(this);
    startServers();
    startTimelines();
    startEventHandling();
  }

  private void startEventHandling() {
    (new Thread(eventManager)).start();
  }

  private void startTimelines() {
    realTimelineInMillis = new Timeline(0, 1000, Timeline.TimelineIterationTypes.REAL, this);
    gameTimelineInMillis = new Timeline(realTimelineInMillis.getTime(), 1000, Timeline.TimelineIterationTypes.REAL, this);
    gameTimelineInFrames = new Timeline(frameCount, 1, Timeline.TimelineIterationTypes.LOOP, this);

    Queue<GameObject> timelines = new LinkedList<>();
    timelines.add(realTimelineInMillis);
    timelines.add(gameTimelineInMillis);
    timelines.add(gameTimelineInFrames);

    eventManager.registerTimeline(gameTimelineInMillis);

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
      drawShapes();
      drawText();
    }
  }

  private void drawText() {
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
              gameObject.update();
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
    List<Object> eventParameters = new ArrayList<>();
    eventParameters.add(new Integer(key));
    eventParameters.add(new Boolean(true));

    Event event = new Event(EngineConstants.DEFAULT_EVENT_TYPES.USER_INPUT.toString(), eventParameters);
    eventManager.raiseEvent(event, gameTimelineInMillis);
  }

  public void keyReleased() {
    if (serverMode)
      return;
    List<Object> eventParameters = new ArrayList<>();
    eventParameters.add(new Integer(key));
    eventParameters.add(new Boolean(false));

    Event event = new Event(EngineConstants.DEFAULT_EVENT_TYPES.USER_INPUT.toString(), eventParameters);
    eventManager.raiseEvent(event, gameTimelineInMillis);
  }

  public GameClient registerClient(String remoteServerAddress, int remoteServerPort, Controller controller) {
    if (gameClient == null)
      gameClient = new GameClient(remoteServerAddress, remoteServerPort, controller);

    return gameClient;
  }

  public GameServer registerServer(int localServerPort, Controller controller) {
    if (gameServer == null)
      gameServer = new GameServer(localServerPort, controller);

    return gameServer;
  }

  public EventManager getEventManager() {
    return eventManager;
  }

}
