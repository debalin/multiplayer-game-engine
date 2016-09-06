package com.debalin.engine;

import com.debalin.Controller;
import processing.core.*;

import java.util.ArrayList;

public class MainEngine extends PApplet {

  public static ArrayList<GameObject> gameObjects;
  public static Controller controller;

  public static PVector resolution;
  public static PVector backgroundRGB;
  public static int smoothFactor;

  static {
    gameObjects = new ArrayList<>();
  }

  public static void registerGameObject(GameObject gameObject) {
    gameObjects.add(gameObject);
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
    controller.setEngine(this);
  }

  public void setup() {
    noStroke();
  }

  public void draw() {
    background(backgroundRGB.x, backgroundRGB.y, backgroundRGB.z);
    updatePositions();
    drawShapes();
  }

  public void updatePositions() {
    gameObjects.forEach(GameObject::updatePosition);
  }

  public void drawShapes() {
    gameObjects.forEach(GameObject::drawShape);
  }

}
