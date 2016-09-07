package com.debalin.engine;

import processing.core.*;

import java.util.ArrayList;

public class MainEngine extends PApplet {

  public ArrayList<GameObject> gameObjects;
  public ArrayList<KeypressUser> keypressUsers;
  public static Controller controller;

  public static PVector resolution;
  public static PVector backgroundRGB;
  public static int smoothFactor;

  public MainEngine() {
    gameObjects = new ArrayList<>();
    keypressUsers = new ArrayList<>();
  }

  public void registerGameObject(GameObject gameObject) {
    gameObjects.add(gameObject);
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
  }

  public void draw() {
    background(backgroundRGB.x, backgroundRGB.y, backgroundRGB.z);
    controller.manage();
    updatePositions();
    drawShapes();
  }

  public void updatePositions() {
    gameObjects.forEach(GameObject::updatePosition);
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

}
