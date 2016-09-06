package com.debalin.util;

import processing.core.PVector;

public class Constants {

  public static final PVector RESOLUTION = new PVector(800, 800);
  public static final int SMOOTH_FACTOR = 4;
  public static final PVector BACKGROUND_RGB = new PVector(60, 60, 60);

  public static final PVector PLAYER_COLOR = new PVector(123, 120, 45);
  public static final PVector PLAYER_SIZE = new PVector(50, 50);
  public static final float PLAYER_START_X = 50;
  public static final PVector PLAYER_INIT_POS = new PVector(Constants.PLAYER_START_X, Constants.RESOLUTION.y - Constants.PLAYER_SIZE.y);

}
