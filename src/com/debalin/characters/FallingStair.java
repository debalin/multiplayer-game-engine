package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

public class FallingStair extends BaseRectangle {

  private boolean VISIBLE;

  public FallingStair(MainEngine engine, PVector stairColor, PVector stairInitPosition) {
    super(stairColor, stairInitPosition, Constants.FALLING_STAIR_SIZE, Constants.FALLING_STAIR_INIT_VEL, Constants.FALLING_STAIR_INIT_ACC, engine);
    VISIBLE = true;
  }

  public boolean isVisible() {
    return VISIBLE;
  }

  public void updatePosition() {
    position.add(velocity);
    checkBounds();
  }

  private void checkBounds() {
    if (position.y > Constants.CLIENT_RESOLUTION.y)
      VISIBLE = false;
  }

}
