package com.debalin.characters;

import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.events.KeypressUser;
import com.debalin.engine.MainEngine;
import com.debalin.util.Collision;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.util.Queue;
import java.util.Random;

public class Player extends BaseRectangle implements KeypressUser {

  private transient boolean LEFT, RIGHT, JUMP;
  private transient Queue<GameObject> fallingStairs;
  private transient Queue<GameObject> standingStairs;
  private transient BaseRectangle collidedStair;
  private transient States state;
  private boolean VISIBLE;
  private Random colorRandom;

  private enum States {
    ON_GROUND, ON_STAIR, ON_AIR
  }

  public boolean isVisible() {
    return VISIBLE;
  }

  public Player(MainEngine engine, Queue<GameObject> fallingStairs, Queue<GameObject> standingStairs) {
    super(Constants.PLAYER_COLOR, Constants.PLAYER_INIT_POS, Constants.PLAYER_SIZE, Constants.PLAYER_INIT_VEL, Constants.PLAYER_INIT_ACC, engine);
    colorRandom = new Random();
    color = (new PVector(colorRandom.nextInt(255), colorRandom.nextInt(255), colorRandom.nextInt(255))).copy();
    this.fallingStairs = fallingStairs;
    this.standingStairs = standingStairs;
    LEFT = RIGHT = JUMP = false;
    state = States.ON_GROUND;
    VISIBLE = true;
  }

  public synchronized void updatePosition() {
    switch (state) {
      case ON_GROUND:
        checkBounds();
        break;
      case ON_AIR:
        checkStairCollision();
        checkBounds();
        break;
      case ON_STAIR:
        isStillOnStair();
        break;
    }

    moveAndJump();

    velocity.add(acceleration);
    position.add(velocity);
  }

  private void isStillOnStair() {
    boolean check;

    check = (!collidedStair.isVisible());
    check = check || (position.x < collidedStair.getPosition().x - size.x || position.x > collidedStair.getPosition().x + collidedStair.getSize().x);
    check = check || (position.y >= collidedStair.getPosition().y + size.y + 5);

    if (check) {
      acceleration.y = Constants.PLAYER_MAX_ACC.y;
      changeState(States.ON_AIR);
    }
  }

  private void moveAndJump() {
    if (LEFT)
      velocity.x = -Constants.PLAYER_MAX_VEL.x;
    else if (RIGHT)
      velocity.x = Constants.PLAYER_MAX_VEL.x;
    else
      velocity.x = 0f;

    if (JUMP) {
      changeState(States.ON_AIR);
      velocity.y = -Constants.PLAYER_MAX_VEL.y;
      acceleration.y = Constants.PLAYER_MAX_ACC.y;
      JUMP = false;
    }
  }

  private void changeState(States state) {
    this.state = state;
  }

  private void checkStairCollision() {
    fallingStairs.stream().filter(stair -> Collision.hasCollidedRectangles(this, (BaseRectangle) stair)).forEach(stair -> {
      position.y = (stair).getPosition().y - size.y - 3;
      LEFT = false;
      RIGHT = false;
      velocity.y = Constants.FALLING_STAIR_INIT_VEL.y;
      acceleration.y = Constants.PLAYER_INIT_ACC.y;
      changeState(States.ON_STAIR);
      collidedStair = (BaseRectangle) stair;
    });

    standingStairs.stream().filter(stair -> Collision.hasCollidedRectangles(this, (BaseRectangle) stair)).forEach(stair -> {
      position.y = (stair).getPosition().y - size.y - 3;
      LEFT = false;
      RIGHT = false;
      velocity.y = 0;
      acceleration.y = Constants.PLAYER_INIT_ACC.y;
      changeState(States.ON_STAIR);
      collidedStair = (BaseRectangle) stair;
    });
  }

  private void checkBounds() {
    if (position.y > Constants.CLIENT_RESOLUTION.y - size.y) {
      velocity.y = Constants.PLAYER_INIT_VEL.y;
      acceleration.y = Constants.PLAYER_INIT_ACC.y;
      position.y = Constants.CLIENT_RESOLUTION.y - size.y;
      state = States.ON_GROUND;
    }
    if (position.x >= Constants.CLIENT_RESOLUTION.x - size.x) {
      RIGHT = false;
      position.x = Constants.CLIENT_RESOLUTION.x - size.x;
    }
    if (position.x <= 0) {
      LEFT = false;
      position.x = 0;
    }
  }

  public void handleKeypress(int key, boolean set) {
    switch(key) {
      case 'A':
      case 'a':
        LEFT = set;
        break;
      case 'D':
      case 'd':
        RIGHT = set;
        break;
      case 32:
        if (state == States.ON_GROUND || state == States.ON_STAIR)
          JUMP = true;
    }
  }

}
