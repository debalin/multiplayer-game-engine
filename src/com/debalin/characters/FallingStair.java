package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

public class FallingStair extends MovingRectangle {

  protected boolean isDeathStair = false;

  public FallingStair(MainEngine engine, PVector stairColor, PVector stairInitPosition) {
    super(stairColor, stairInitPosition, Constants.FALLING_STAIR_SIZE, new PVector(0, engine.random(Constants.FALLING_STAIR_MAX_VEL_Y)), Constants.FALLING_STAIR_INIT_ACC, engine);
    visible = true;

    if (engine.random(0, 1) > Constants.DEATH_STAIR_PROBABILITY)
      isDeathStair = true;
  }

  public void updatePosition() {
    position.add(velocity);
    checkBounds();
  }

  private void checkBounds() {
    if (position.y > Constants.CLIENT_RESOLUTION.y)
      visible = false;
  }

  @Override
  public void drawShape() {
    engine.pushMatrix();
    engine.noStroke();

    engine.fill(color.x, color.y, color.z);
    engine.rect(position.x, position.y, size.x, size.y);

    engine.fill(Constants.DEATH_STAIR_CIRCLE_COLOR.x, Constants.DEATH_STAIR_CIRCLE_COLOR.y, Constants.DEATH_STAIR_CIRCLE_COLOR.z);
    if (isDeathStair) {
      engine.ellipse(position.x + size.x / 2, position.y - Constants.DEATH_STAIR_CIRCLE_OFFSET, Constants.DEATH_STAIR_CIRCLE_RADIUS, Constants.DEATH_STAIR_CIRCLE_RADIUS);
    }

    engine.popMatrix();
  }

}
