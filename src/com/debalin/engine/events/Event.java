package com.debalin.engine.events;

import java.util.List;

public class Event {

  private String eventType;
  private List<Object> eventParameters;

  public Event(String eventType, List<Object> eventParameters) {
    this.eventType = eventType;
    this.eventParameters = eventParameters;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public List<Object> getEventParameters() {
    return eventParameters;
  }

  public void setEventParameters(List<Object> eventParameters) {
    this.eventParameters = eventParameters;
  }
}
