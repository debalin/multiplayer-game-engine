package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

public class StandingStair extends NonMovingRectangle {

  public StandingStair(MainEngine engine, PVector stairColor, PVector stairInitPosition) {
    super(stairColor, stairInitPosition, Constants.STANDING_STAIR_SIZE, engine);
    visible = true;
  }

  public void updatePosition() {};

}
