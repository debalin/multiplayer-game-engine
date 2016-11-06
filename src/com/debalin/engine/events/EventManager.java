package com.debalin.engine.events;

import com.debalin.engine.MainEngine;
import com.debalin.engine.timeline.Timeline;
import com.debalin.engine.util.EngineConstants;

import java.util.*;

public class EventManager implements Runnable {

  Map<String, EventPriorities> eventTypePriorities;
  Set<Timeline> timelines;
  MainEngine engine;
  Map<Timeline, PriorityQueue<OrderedEvent>> eventQueues;

  private class OrderedEvent {
    public Event event;
    public long score;

    public OrderedEvent(Event event, long score) {
      this.event = event;
      this.score = score;
    }
  }

  private class EventComparator implements Comparator<OrderedEvent> {
    public int compare(OrderedEvent event1, OrderedEvent event2) {
      if (event1.score < event2.score) {
        return -1;
      }
      else if (event1.score > event2.score) {
        return 1;
      }
      else {
        return 0;
      }
    }
  }

  public enum EventPriorities {
    HIGH, MED, LOW
  }

  public EventManager(MainEngine engine) {
    eventTypePriorities = new HashMap<>();
    timelines = new HashSet<>();
    eventQueues = new HashMap<>();
    this.engine = engine;
    getDefaultEventTypes();
  }

  private void getDefaultEventTypes() {
    for (EngineConstants.DEFAULT_EVENT_TYPES defaultEventType : EngineConstants.DEFAULT_EVENT_TYPES.values()) {
      eventTypePriorities.put(defaultEventType.toString(), EventPriorities.HIGH);
    }
  }

  public void registerEventType(String eventType, EventPriorities defaultEventPriority) {
    eventTypePriorities.put(eventType, defaultEventPriority);
  }

  public void registerTimeline(Timeline timeline) {
    timelines.add(timeline);
    eventQueues.put(timeline, new PriorityQueue<>(new EventComparator()));
  }

  public void raiseEvent(Event event, Timeline timeline) {
    if (timeline == null || !timelines.contains(timeline)) {
      System.out.println("Timeline not registered, please do it first.");
      return;
    }
    String eventType = event.getEventType();
    EventPriorities eventDefaultPriority = eventTypePriorities.get(eventType);

    if (eventDefaultPriority == null) {
      System.out.println("Event type does not exist, will default to low.");
      eventDefaultPriority = EventPriorities.LOW;
    }

    synchronized (eventQueues) {
      switch (eventDefaultPriority) {
        case HIGH:
          eventQueues.get(timeline).add(new OrderedEvent(event, timeline.getTime() - 100));
          break;
        case MED:
          eventQueues.get(timeline).add(new OrderedEvent(event, timeline.getTime() - 50));
          break;
        case LOW:
          eventQueues.get(timeline).add(new OrderedEvent(event, timeline.getTime()));
          break;
      }
      eventQueues.notify();
    }
  }

  public void handleEvents() {
    while (true) {
      try {
        synchronized (eventQueues) {
          Iterator it = eventQueues.entrySet().iterator();
          while (it.hasNext()) {
            Map.Entry<Timeline, PriorityQueue<OrderedEvent>> pair = (Map.Entry) it.next();
            Queue<OrderedEvent> timelineQueue = pair.getValue();
            while (!timelineQueue.isEmpty()) {
              OrderedEvent orderedEvent = timelineQueue.poll();
              MainEngine.controller.getEventHandler().onEvent(orderedEvent.event);
            }
          }
          eventQueues.wait();
        }
      }
      catch (InterruptedException ex) {}
    }
  }

  public void run() {
    System.out.println("Starting event handling.");
    handleEvents();
  }

}
