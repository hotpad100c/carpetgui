package ml.mypals.carpetgui.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EventStream<T> {
   protected final Function<List<T>, T> sinkFactory;
   protected final List<T> subscribers = new ArrayList<>();
   protected T sink;

   public EventStream(Function<List<T>, T> sinkFactory) {
      this.sinkFactory = sinkFactory;
      this.regenerateSink();
   }

   public T sink() {
      return this.sink;
   }

   public Subscription subscribe(T subscriber) {
      this.addSubscriber(subscriber);
      return new Subscription(subscriber);
   }

   public void addSubscriber(T subscriber) {
      this.subscribers.add(subscriber);
      this.regenerateSink();
   }

   public void removeSubscriber(T subscriber) {
      this.subscribers.remove(subscriber);
      this.regenerateSink();
   }

   protected void regenerateSink() {
      this.sink = (T)this.sinkFactory.apply(this.subscribers);
   }

   public class Subscription {
      protected final T subscriber;

      public Subscription(T subscriber) {
         this.subscriber = subscriber;
      }

      public void cancel() {
         EventStream.this.removeSubscriber(this.subscriber);
      }
   }
}
