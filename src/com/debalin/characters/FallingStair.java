package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

public class FallingStair extends MovingRectangle {

  protected boolean isDeathStair = false;
  protected transient boolean killedPlayer = false;

  public boolean isDeathStair() { return isDeathStair; }

  public void setKilledPlayer(boolean killedPlayer) {
    this.killedPlayer = killedPlayer;
  }
  public void setDeathStair(boolean deathStair) {
    isDeathStair = deathStair;
  }

  public FallingStair(MainEngine engine, PVector stairColor, PVector stairInitPosition) {
    super(stairColor, stairInitPosition, Constants.FALLING_STAIR_SIZE, new PVector(0, engine.random(Constants.FALLING_STAIR_MIN_VEL_Y, Constants.FALLING_STAIR_MAX_VEL_Y)), Constants.FALLING_STAIR_INIT_ACC, engine);
    visible = true;

    if (engine.random(0, 1) > Constants.DEATH_STAIR_PROBABILITY)
      isDeathStair = true;
  }

  public void update() {
    position.add(velocity);
    checkBounds();
  }

  private void checkBounds() {
    if (position.y > Constants.CLIENT_RESOLUTION.y)
      visible = false;
  }

  @Override
  public void draw() {
    engine.pushMatrix();

    if (!isDeathStair) {
      engine.noStroke();
      engine.fill(color.x, color.y, color.z);
      engine.rect(position.x, position.y, size.x, size.y);
    }
    else {
      if (killedPlayer) {
        engine.noFill();
        engine.stroke(255, 0, 0);
        engine.rect(position.x, position.y, size.x, size.y);
      }
    }

    engine.popMatrix();
  }

}
