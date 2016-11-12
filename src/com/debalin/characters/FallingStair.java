package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;
import processing.core.PVector;

public class FallingStair extends MovingRectangle {

  protected boolean isDeathStair = false;
  protected boolean killedPlayer = false;

  private long stairID;

  public void setKilledPlayer(boolean killedPlayer) {
    this.killedPlayer = killedPlayer;
  }

  public FallingStair(MainEngine engine, PVector stairColor, PVector stairInitPosition, long stairID, PVector velocity, boolean isDeathStair) {
    super(stairColor, stairInitPosition, Constants.FALLING_STAIR_SIZE, velocity, Constants.FALLING_STAIR_INIT_ACC, engine);
    setVisible(true);

    this.isDeathStair = isDeathStair;

    this.stairID = stairID;
  }

  public void update(float frameTicSize) {
    position.add(PVector.mult(velocity, frameTicSize));
    checkBounds();
  }

  private void checkBounds() {
    if (position.y > Constants.CLIENT_RESOLUTION.y)
      setVisible(false);
  }

  @Override
  public void draw() {
    engine.pushMatrix();

    if (!isDeathStair) {
      engine.noStroke();
      engine.fill(color.x, color.y, color.z);
      engine.rect(position.x, position.y, size.x, size.y);
    } else {
      if (killedPlayer) {
        engine.noFill();
        engine.stroke(255, 0, 0);
        engine.rect(position.x, position.y, size.x, size.y);
      } else {
        engine.noFill();
        engine.stroke(255, 255, 0);
        engine.rect(position.x, position.y, size.x, size.y);
      }
    }

    engine.popMatrix();
  }

  public long getStairID() {
    return stairID;
  }
}
