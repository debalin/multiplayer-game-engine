package com.debalin.util;

import processing.core.PVector;

public class Constants {

  public static final PVector CLIENT_RESOLUTION = new PVector(600, 900);
  public static final PVector SERVER_RESOLUTION = new PVector(1, 1);
  public static final int SMOOTH_FACTOR = 4;
  public static final PVector BACKGROUND_RGB = new PVector(60, 60, 60);

  public static final PVector PLAYER_COLOR = new PVector(0, 204, 0);
  public static final PVector PLAYER_SIZE = new PVector(25, 25);
  public static final float PLAYER_START_X = 50;
  public static final PVector PLAYER_INIT_POS = new PVector(Constants.PLAYER_START_X, Constants.CLIENT_RESOLUTION.y - Constants.PLAYER_SIZE.y);
  public static final PVector PLAYER_INIT_VEL = new PVector(0, 0);
  public static final PVector PLAYER_MAX_VEL = new PVector(5, 5);
  public static final PVector PLAYER_INIT_ACC = new PVector(0, 0);
  public static final PVector PLAYER_MAX_ACC = new PVector(0, 0.08f);

  public static final float FALLING_STAIR_START_Y = -20;
  public static final PVector FALLING_STAIR_SIZE = new PVector(80, 10);
  public static final PVector FALLING_STAIR_INIT_VEL = new PVector(0, 0.5f);
  public static final int FALLING_STAIR_SPAWN_INTERVAL = 320;
  public static final int STAIR_PADDING = 10;
  public static final PVector FALLING_STAIR_INIT_ACC = new PVector(0, 0);

  public static final PVector STANDING_STAIR_SIZE = new PVector(100, 10);
  public static final int STANDING_STAIR_COUNT = 3;

  public static final int SERVER_PORT = 5678;
  public static final String SERVER_ADDRESS = "localhost";

}
