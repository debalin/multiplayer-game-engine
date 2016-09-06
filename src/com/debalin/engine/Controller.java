package com.debalin.engine;

public abstract class Controller {

  public MainEngine engine;

  public void setEngine(MainEngine engine) {
    this.engine = engine;
    initialize();
  }

  public abstract void initialize();

}
