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

    int connectionID = -1;
    while (true) {
      while (true) {
        try {
          GameObject gameObject = (GameObject) in.readObject();
          if (gameObject.tag == GameServer.NetworkTag.START_TAG) {
            connectionID = gameObject.connectionID;
            gameObjects = new ConcurrentLinkedQueue<>();
          } else if (gameObject.tag == GameServer.NetworkTag.OBJECT) {
            gameObjects.add(gameObject);
          } else if (gameObject.tag == GameServer.NetworkTag.END_TAG) {
            break;
          }
        }
        catch (IOException e) {
          if (e.getMessage().equals(EngineConstants.readErrorMessage)) {
            System.out.println("Connection lost with server, will stop client read thread.");
            return;
          }
        }
        catch (Exception e) {
          System.out.println("Connection lost with server, will stop client read thread.");
          return;
        }
      }
      if (gameObjects != null && gameObjects.size() > 0) {
        controller.getDataFromServer(gameObjects, connectionID);
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
        GameObject startObject = new NetworkStartTag(-1);
        out.writeObject(startObject);

        for (GameObject object : dataToSend)
          out.writeObject(object);

        GameObject endObject = new NetworkEndTag();
        out.writeObject(endObject);
        out.reset();
      } catch (IOException e) {
        if (e.getMessage().equals(EngineConstants.writeErrorMessage)) {
          System.out.println("Connection lost with server, will stop client write thread.");
          return;
        }
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
