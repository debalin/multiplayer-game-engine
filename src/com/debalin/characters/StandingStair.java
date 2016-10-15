package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

public class StandingStair extends BaseRectangle {

  private boolean VISIBLE;

  public StandingStair(MainEngine engine, PVector stairColor, PVector stairInitPosition) {
    super(stairColor, stairInitPosition, Constants.STANDING_STAIR_SIZE, null, null, engine);
    VISIBLE = true;
  }

  public boolean isVisible() {
    return VISIBLE;
  }

  public void updatePosition() {}

}
