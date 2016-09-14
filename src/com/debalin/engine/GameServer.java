package com.debalin.engine;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameServer implements Runnable {

  private ServerSocket serverSocket;
  private int localServerPort;
  private Controller controller;
  private List<Socket> serverConnections;

  public enum NetworkTag {
    OBJECT, START_TAG, END_TAG
  }

  public GameServer(int localServerPort, Controller controller) {
    this.localServerPort = localServerPort;
    this.controller = controller;
    this.serverConnections = new ArrayList<>();

    try {
      serverSocket = new ServerSocket(this.localServerPort);
      System.out.println("Server started at IP " + serverSocket.getLocalSocketAddress() + ".");
    }
    catch (IOException e) {
      System.out.println("Some error in GameServer.");
      e.printStackTrace();
    }
  }

  public void run() {
    while (true) {
      System.out.println("Looking for connections.");
      Socket serverConnection = acceptConnections();
      if (serverConnection != null) {
        serverConnections.add(serverConnection);
        new Thread(() -> maintainClientReadConnection(serverConnection, serverConnections.size() - 1)).start();
        new Thread(() -> maintainClientWriteConnection(serverConnection, serverConnections.size() - 1)).start();
      }
    }
  }

  private void maintainClientReadConnection(Socket serverConnection, int connectionID) {
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(serverConnection.getInputStream());
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
        controller.getDataFromClient(gameObjects, connectionID);
      }
      gameObjects = null;
    }
  }

  private void maintainClientWriteConnection(Socket serverConnection, int connectionID) {
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(serverConnection.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    while (true) {
      try {
        Queue<GameObject> dataToSend = controller.sendDataFromServer();
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

  private Socket acceptConnections() {
    Socket serverConnection = null;
    try {
      serverConnection = serverSocket.accept();
      controller.incrementClientCount();
      System.out.println("Server accepted connection to " + serverConnection.getRemoteSocketAddress() + ".");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return serverConnection;
  }

}
