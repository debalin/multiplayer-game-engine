package com.debalin.engine;

import processing.core.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MainEngine extends PApplet {

  public List<List<GameObject>> gameObjectsCluster;
  public List<KeypressUser> keypressUsers;
  public static Controller controller;
  public GameServer gameServer;
  public GameClient gameClient;

  public static PVector clientResolution;
  public static PVector serverResolution;
  public static PVector backgroundRGB;
  public static int smoothFactor;

  public List<Boolean> updateOrNotArray;

  public static boolean serverMode;

  public MainEngine() {
    gameObjectsCluster = new ArrayList<>();
    keypressUsers = new LinkedList<>();
    updateOrNotArray = new ArrayList<>();
  }

  public int registerGameObject(GameObject gameObject, int gameObjectListID, boolean update) {
    if (gameObjectListID == -1) {
      gameObjectListID = gameObjectsCluster.size();
      gameObjectsCluster.add(new LinkedList<>());
      updateOrNotArray.add(update);
    }
    gameObjectsCluster.get(gameObjectListID).add(gameObject);
    return gameObjectListID;
  }

  public void registerKeypressUser(KeypressUser keypressUser) {
    keypressUsers.add(keypressUser);
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

    if (!serverMode)
      drawShapes();
  }

  public void updatePositions() {
    int count = 0;
    for (List<GameObject> gameObjects : gameObjectsCluster) {
      if (updateOrNotArray.get(count)) {
        Iterator<GameObject> i = gameObjects.iterator();
        while (i.hasNext()) {
          GameObject gameObject = i.next();
          if (!gameObject.isVisible()) {
            i.remove();
          } else {
            gameObject.updatePosition();
          }
        }
      }
      count++;
    }
  }

  public int registerGameObjects(List<GameObject> gameObjects, int gameObjectListID, boolean update) {
    if (gameObjectListID == -1) {
      gameObjectListID = gameObjectsCluster.size();
      gameObjectsCluster.add(new LinkedList<>());
      updateOrNotArray.add(update);
    }
    gameObjectsCluster.set(gameObjectListID, gameObjects);
    return gameObjectListID;
  }

  public void drawShapes() {
    gameObjectsCluster.forEach(gameObjects -> gameObjects.forEach(GameObject::drawShape));
  }

  public void keyPressed() {
    for (KeypressUser keypressUser : keypressUsers) {
      keypressUser.handleKeypress(key, true);
    }
  }

  public void keyReleased() {
    for (KeypressUser keypressUser : keypressUsers) {
      keypressUser.handleKeypress(key, false);
    }
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

}
