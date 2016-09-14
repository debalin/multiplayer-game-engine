package com.debalin.engine;

import java.net.*;
import java.io.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameClient implements Runnable {

  private Controller controller;
  private Socket clientConnection;
  private int remoteServerPort;
  private String remoteServerAddress;

  public GameClient(String remoteServerAddress, int remoteServerPort, Controller controller) {
    this.controller = controller;
    this.remoteServerPort = remoteServerPort;
    this.remoteServerAddress = remoteServerAddress;
  }

  public void run() {
    System.out.println("Trying to connect to server at " + remoteServerAddress + ":" + remoteServerPort + ".");
    connectToServer();
    new Thread(() -> maintainServerReadConnection()).start();
    new Thread(() -> maintainServerWriteConnection()).start();
  }

  private void maintainServerReadConnection() {
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(clientConnection.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    Queue<GameObject> gameObjects = new ConcurrentLinkedQueue<>();

    while (true) {
      while (true) {
        try {
          GameObject gameObject = (GameObject) in.readObject();
          if (gameObject.tag == GameServer.NetworkTag.START_TAG) {
            gameObjects = new ConcurrentLinkedQueue<>();
          } else if (gameObject.tag == GameServer.NetworkTag.OBJECT) {
            gameObjects.add(gameObject);
          } else if (gameObject.tag == GameServer.NetworkTag.END_TAG) {
            break;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (gameObjects != null && gameObjects.size() > 0) {
        controller.getDataFromServer(gameObjects);
      }
      gameObjects = null;
    }
  }

  private void maintainServerWriteConnection() {
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(clientConnection.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    while (true) {
      try {
        Queue<GameObject> dataToSend = controller.sendDataFromClient();
        GameObject startObject = new NetworkStartTag();
        out.writeObject(startObject);

        for (GameObject object : dataToSend)
          out.writeObject(object);

        GameObject endObject = new NetworkEndTag();
        out.writeObject(endObject);
        out.reset();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void connectToServer() {
    try {
      clientConnection = new Socket(remoteServerAddress, remoteServerPort);
      System.out.println("Client accepted connection to " + clientConnection.getRemoteSocketAddress() + ".");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
