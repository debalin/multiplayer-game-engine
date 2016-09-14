package com.debalin.engine;

import java.util.Queue;

public abstract class Controller {

  public MainEngine engine;

  public Integer clientCount = 0;

  public void setEngine(MainEngine engine) {
    this.engine = engine;
    initialize();
  }

  public abstract void initialize();

  public abstract void manage();

  public int incrementClientCount() {
    synchronized (clientCount) {
      return ++clientCount;
    }
  }

  public int getClientCount() {
    synchronized (clientCount) {
      return clientCount;
    }
  }

  public Queue<GameObject> sendDataFromServer() {
    return null;
  }

  public Queue<GameObject> sendDataFromClient() {
    return null;
  }

  public void getDataFromServer(Queue<GameObject> dataFromServer) {}

  public void getDataFromClient(Queue<GameObject> dataFromClient, int connectionID) {}

}
