package com.debalin.characters;

import com.debalin.engine.events.Event;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.MainEngine;
import com.debalin.engine.util.EngineConstants;
import com.debalin.util.Collision;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Player extends MovingRectangle {

  public boolean LEFT, RIGHT, JUMP;
  public Queue<GameObject> fallingStairs;
  public List<GameObject> standingStairs;
  public GameObject collidedStair;
  private States state;
  public float score = 100;
  private PVector averagePosition = new PVector(0, 0);

  private SpawnPoint spawnPoint;

  public enum States {
    ON_GROUND, ON_STAIR, ON_AIR
  }

  public boolean isVisible() {
    return visible;
  }

  public Player(MainEngine engine, SpawnPoint spawnPoint, Queue<GameObject> fallingStairs, List<GameObject> standingStairs, PVector color) {
    super(Constants.PLAYER_COLOR, spawnPoint.getPosition(), Constants.PLAYER_SIZE, Constants.PLAYER_INIT_VEL, Constants.PLAYER_MAX_ACC, engine);
    this.color = color;
    this.fallingStairs = fallingStairs;
    this.standingStairs = standingStairs;
    LEFT = RIGHT = JUMP = false;
    state = States.ON_AIR;
    visible = true;

    this.spawnPoint = spawnPoint;
  }

  public synchronized void update(float frameTicSize) {
    switch (state) {
      case ON_GROUND:
        checkBounds();
        break;
      case ON_AIR:
        checkStairCollision();
        checkBounds();
        break;
      case ON_STAIR:
        checkDeath();
        isStillOnStair();
        break;
    }

    moveAndJump();

    velocity.add(PVector.mult(acceleration, frameTicSize));
    position.add(PVector.mult(velocity, frameTicSize));

    averagePosition.add(position).div(2);
    if (engine.frameCount % Constants.SCORE_INCREMENT_INTERVAL == 0) {
      score *= ((Constants.CLIENT_RESOLUTION.y - averagePosition.y) / Constants.CLIENT_RESOLUTION.y);
      score = (score < 0) ? 0 : score;
    }
  }

  private void checkDeath() {
    if (collidedStair.getClass().getTypeName().equals(FallingStair.class.getTypeName())) {
      if (((FallingStair) collidedStair).isDeathStair) {
        String eventType = Constants.EVENT_TYPES.PLAYER_DEATH.toString();
        List<Object> eventParameters = new ArrayList<>();
        eventParameters.add(new Long(((FallingStair) collidedStair).getStairID()));
        eventParameters.add(getConnectionID());
        Event event = new Event(eventType, eventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), engine.controller.getClientConnectionID().intValue(), engine.gameTimelineInMillis.getTime(), true);
        engine.getEventManager().raiseEvent(event, false);
        System.out.println("Player is dead.");
      }
    }
  }

  public void regenerate() {
    System.out.println("Player is regenerating.");
    position = spawnPoint.getPosition().copy();
    acceleration.y = Constants.PLAYER_MAX_ACC.y;
    changeState(States.ON_AIR);
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

  public void changeState(States state) {
    this.state = state;
  }

  private void checkStairCollision() {
    fallingStairs.stream().filter(stair -> Collision.hasCollidedRectangles(this, stair)).forEach(stair -> {
      String eventType = Constants.EVENT_TYPES.PLAYER_COLLISION.toString();
      List<Object> eventParameters = new ArrayList<>();
      eventParameters.add(((FallingStair) stair).getStairID());
      eventParameters.add(true);
      eventParameters.add(getConnectionID());
      Event event = new Event(eventType, eventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), engine.controller.getClientConnectionID().intValue(), engine.gameTimelineInMillis.getTime(), true);
      engine.getEventManager().raiseEvent(event, false);
      return;
    });

    standingStairs.stream().filter(stair -> Collision.hasCollidedRectangles(this, stair)).forEach(stair -> {
      String eventType = Constants.EVENT_TYPES.PLAYER_COLLISION.toString();
      List<Object> eventParameters = new ArrayList<>();
      eventParameters.add(((StandingStair) stair).getStairID());
      eventParameters.add(false);
      eventParameters.add(getConnectionID());
      Event event = new Event(eventType, eventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), engine.controller.getClientConnectionID().intValue(), engine.gameTimelineInMillis.getTime(), true);
      engine.getEventManager().raiseEvent(event, false);
      return;
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
    switch (key) {
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
        break;
    }
  }

}
