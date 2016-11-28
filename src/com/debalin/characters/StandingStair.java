package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

public class StandingStair extends NonMovingRectangle {

  private long stairID;

  public StandingStair(MainEngine engine, PVector stairColor, PVector stairInitPosition, long stairID) {
    super(stairColor, stairInitPosition, Constants.STANDING_STAIR_SIZE, engine);
    setVisible(true);
    this.stairID = stairID;
  }

  public long getStairID() {
    return stairID;
  }

  public void moveUp(float y) {
    this.position.add(0, -y);
  }

  public void moveDown(float y) {
    this.position.add(0, y);
  }

  public void moveLeft(float x) {
    this.position.add(-x, 0);
  }

  public void moveRight(float x) {
    this.position.add(x, 0);
  }
}
