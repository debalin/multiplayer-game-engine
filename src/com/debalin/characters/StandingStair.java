package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

public class StandingStair extends NonMovingRectangle {

  private long stairID;

  public StandingStair(MainEngine engine, PVector stairColor, PVector stairInitPosition, long stairID) {
    super(stairColor, stairInitPosition, Constants.STANDING_STAIR_SIZE, engine);
    visible = true;
    this.stairID = stairID;
  }

  public long getStairID() {
    return stairID;
  }
}
