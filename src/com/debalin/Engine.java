package com.debalin;

import processing.core.*;
import java.util.*;

public class Engine extends PApplet {

  public static final PVector RESOLUTION = new PVector(800, 800);
  private static final int SMOOTH_FACTOR = 4;
  private static final PVector BACKGROUND_RGB = new PVector(60, 60, 60);

  public static void main(String args[]) {
    PApplet.main(new String[] { "com.debalin.Engine" });
  }

  public void settings() {
    size((int)RESOLUTION.x, (int)RESOLUTION.y, P2D);
    smooth(SMOOTH_FACTOR);
  }

  public void setup() {
    noStroke();
  }

  public void draw() {
    background(BACKGROUND_RGB.x, BACKGROUND_RGB.y, BACKGROUND_RGB.z);
  }

}
