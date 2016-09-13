package com.debalin.engine;

import processing.core.*;

import java.util.ArrayList;
import java.util.Iterator;

public class MainEngine extends PApplet {

  public ArrayList<GameObject> gameObjects;
  public ArrayList<KeypressUser> keypressUsers;
  public static Controller controller;
  public GameServer gameServer;
  public GameClient gameClient;

  public static PVector resolution;
  public static PVector backgroundRGB;
  public static int smoothFactor;

  public MainEngine() {
    gameObjects = new ArrayList<>();
    keypressUsers = new ArrayList<>();
  }

  public void registerGameObject(GameObject gameObject) {
    this.gameObjects.add(gameObject);
  }

  public void registerKeypressUser(KeypressUser keypressUser) {
    keypressUsers.add(keypressUser);
  }

  public static void registerConstants(PVector inputResolution, int inputSmoothFactor, PVector inputBackgroundRGB) {
    resolution = inputResolution.copy();
    smoothFactor = inputSmoothFactor;
    backgroundRGB = inputBackgroundRGB.copy();
  }

  public static void startEngine(Controller inputController) {
    controller = inputController;
    PApplet.main(new String[] { "com.debalin.engine.MainEngine" });
  }

  public void settings() {
    size((int)resolution.x, (int)resolution.y, P2D);
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
    drawShapes();
  }

  public void updatePositions() {
    synchronized (gameObjects) {
      Iterator<GameObject> i = gameObjects.iterator();
      while (i.hasNext()) {
        GameObject gameObject = i.next();
        if (!gameObject.isVisible()) {
          i.remove();
        }
        else {
          gameObject.updatePosition();
        }
      }
    }
  }

  public void registerGameObjects(ArrayList<GameObject> gameObjects) {
    this.gameObjects = gameObjects;
  }

  public void drawShapes() {
    gameObjects.forEach(GameObject::drawShape);
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
