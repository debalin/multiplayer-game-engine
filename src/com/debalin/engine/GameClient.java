package com.debalin.engine;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

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
    DataInputStream in;

    try {
      in = new DataInputStream(clientConnection.getInputStream());
      System.out.println(in.readUTF());
      controller.getDataFromServer(null);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void maintainServerWriteConnection() {
    DataOutputStream out;

    try {
      ArrayList<Serializable> dataToSend = controller.sendDataFromClient();
      out = new DataOutputStream(clientConnection.getOutputStream());
      out.writeUTF("Writing stuff from client to server (" + clientConnection.getRemoteSocketAddress() + ").");
    }
    catch (IOException e) {
      e.printStackTrace();
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
