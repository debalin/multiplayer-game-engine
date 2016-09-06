package com.debalin.engine;

import processing.core.*;
import com.debalin.util.*;

import java.util.ArrayList;

public class MainEngine extends PApplet {

  public ArrayList<GameObject> gameObjects;

  public MainEngine() {
    gameObjects = new ArrayList<>();
  }

  public void registerGameObject(GameObject gameObject) {
    this.gameObjects.add(gameObject);
  }

  public void start() {
    PApplet.main(new String[] { "com.debalin.engine.MainEngine" });
  }

  public void settings() {
    size((int)Constants.RESOLUTION.x, (int)Constants.RESOLUTION.y, P2D);
    smooth(Constants.SMOOTH_FACTOR);
  }

  public void setup() {
    noStroke();
  }

  public void draw() {
    background(Constants.BACKGROUND_RGB.x, Constants.BACKGROUND_RGB.y, Constants.BACKGROUND_RGB.z);
  }

}
