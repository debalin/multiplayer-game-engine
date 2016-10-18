package com.debalin.engine.network;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.characters.SpawnPoint;
import com.debalin.characters.StandingStair;
import com.debalin.engine.Controller;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.game_objects.NetworkEndTag;
import com.debalin.engine.game_objects.NetworkStartTag;
import processing.core.PVector;

public class GameObjectAndStringConverter {

  public static GameObject convertStringToGameObject(String input, Controller controller) {
    GameObject gameObject = null;
    String[] variables = input.split(":");
    String[] color;
    String[] size;
    String[] position;
    String[] velocity;

    switch (variables[0]) {
      case "NetworkStartTag":
        gameObject = new NetworkStartTag(Integer.parseInt(variables[1]));
        break;
      case "NetworkEndTag":
        gameObject = new NetworkEndTag();
        break;
      case "Player":
        color = variables[3].split(",");
        size = variables[4].split(",");
        position = variables[5].split(",");

        Player player = new Player(controller.engine, new SpawnPoint(new PVector()), null, null);
        player.setScore(Float.parseFloat(variables[1]));
        player.setVisible(Boolean.parseBoolean(variables[2]));
        player.setColor(new PVector(Float.parseFloat(color[0]), Float.parseFloat(color[1]), Float.parseFloat(color[2])));
        player.setSize(new PVector(Float.parseFloat(size[0]), Float.parseFloat(size[1]), Float.parseFloat(size[2])));
        player.setPosition(new PVector(Float.parseFloat(position[0]), Float.parseFloat(position[1]), Float.parseFloat(position[2])));
        player.setConnectionID(Integer.parseInt(variables[6]));

        gameObject = player;
        break;
      case "FallingStair":
        color = variables[3].split(",");
        size = variables[4].split(",");
        position = variables[5].split(",");
        velocity = variables[6].split(",");

        FallingStair fallingStair = new FallingStair(controller.engine, new PVector(Float.parseFloat(color[0]), Float.parseFloat(color[1]), Float.parseFloat(color[2])), new PVector(Float.parseFloat(position[0]), Float.parseFloat(position[1]), Float.parseFloat(position[2])));
        fallingStair.setDeathStair(Boolean.parseBoolean(variables[1]));
        fallingStair.setVisible(Boolean.parseBoolean(variables[2]));
        fallingStair.setSize(new PVector(Float.parseFloat(size[0]), Float.parseFloat(size[1]), Float.parseFloat(size[2])));
        fallingStair.setVelocity(new PVector(Float.parseFloat(velocity[0]), Float.parseFloat(velocity[1]), Float.parseFloat(velocity[2])));

        gameObject = fallingStair;
        break;
      case "StandingStair":
        color = variables[2].split(",");
        size = variables[3].split(",");
        position = variables[4].split(",");

        StandingStair standingStair = new StandingStair(controller.engine, new PVector(Float.parseFloat(color[0]), Float.parseFloat(color[1]), Float.parseFloat(color[2])), new PVector(Float.parseFloat(position[0]), Float.parseFloat(position[1]), Float.parseFloat(position[2])));
        standingStair.setVisible(Boolean.parseBoolean(variables[1]));
        standingStair.setSize(new PVector(Float.parseFloat(size[0]), Float.parseFloat(size[1]), Float.parseFloat(size[2])));

        gameObject = standingStair;
        break;
    }

    return gameObject;
  }

  public static String convertGameObjectToString(GameObject gameObject) {
    String result = "";
    String className = gameObject.getClass().getName();

    switch (className) {
      case "com.debalin.engine.game_objects.NetworkStartTag":
        result += "NetworkStartTag:" + gameObject.connectionID + ":";
        break;
      case "com.debalin.engine.game_objects.NetworkEndTag":
        result += "NetworkEndTag:";
        break;
      case "com.debalin.characters.Player":
        Player player = (Player) gameObject;
        result += "Player:";
        result += player.getScore() + ":";
        result += player.isVisible() + ":";
        result += player.getColor().x + "," + player.getColor().y + "," + player.getColor().z + ":";
        result += player.getSize().x + "," + player.getSize().y + "," + player.getSize().z + ":";
        result += player.getPosition().x + "," + player.getPosition().y + "," + player.getPosition().z + ":";
        result += player.getConnectionID() + ":";
        break;
      case "com.debalin.characters.FallingStair":
        FallingStair fallingStair = (FallingStair) gameObject;
        result += "FallingStair:";
        result += fallingStair.isDeathStair() + ":";
        result += fallingStair.isVisible() + ":";
        result += fallingStair.getColor().x + "," + fallingStair.getColor().y + "," + fallingStair.getColor().z + ":";
        result += fallingStair.getSize().x + "," + fallingStair.getSize().y + "," + fallingStair.getSize().z + ":";
        result += fallingStair.getPosition().x + "," + fallingStair.getPosition().y + "," + fallingStair.getPosition().z + ":";
        result += fallingStair.getVelocity().x + "," + fallingStair.getVelocity().y + "," + fallingStair.getVelocity().z + ":";
        break;
      case "com.debalin.characters.StandingStair":
        StandingStair standingStair = (StandingStair) gameObject;
        result += "StandingStair:";
        result += standingStair.isVisible() + ":";
        result += standingStair.getColor().x + "," + standingStair.getColor().y + "," + standingStair.getColor().z + ":";
        result += standingStair.getSize().x + "," + standingStair.getSize().y + "," + standingStair.getSize().z + ":";
        result += standingStair.getPosition().x + "," + standingStair.getPosition().y + "," + standingStair.getPosition().z + ":";
        break;
    }

    return result;
  }

}
