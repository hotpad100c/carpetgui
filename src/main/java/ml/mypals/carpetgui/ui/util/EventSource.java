package ml.mypals.carpetgui.ui.util;

import java.util.Objects;

public class EventSource<T> {
   private final EventStream<T> stream;

   protected EventSource(EventStream<T> stream) {
      this.stream = stream;
   }

   public EventSource<T>.Subscription subscribe(T subscriber) {
      this.stream.addSubscriber(subscriber);
      return new Subscription(subscriber);
   }

   public class Subscription {
      protected final T subscriber;

      public Subscription(T subscriber) {
         Objects.requireNonNull(EventSource.this);
         super();
         this.subscriber = subscriber;
      }

      public void cancel() {
         EventSource.this.stream.removeSubscriber(this.subscriber);
      }
   }
}
