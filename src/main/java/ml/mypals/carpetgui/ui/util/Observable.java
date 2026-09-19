package ml.mypals.carpetgui.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Observable<T> {
   protected T value;
   protected final List<Consumer<T>> observers;

   protected Observable(T initial) {
      this.value = initial;
      this.observers = new ArrayList();
   }

   public static <T> Observable<T> of(T initial) {
      return new Observable<T>(initial);
   }

   public static void observeAll(Runnable observer, Observable<?>... observables) {
      for(Observable<?> observable : observables) {
         observable.observe((o) -> observer.run());
      }

   }

   @SafeVarargs
   public static <T> void observeAll(Consumer<T> observer, Observable<T>... observables) {
      for(Observable<T> observable : observables) {
         observable.observe(observer);
      }

   }

   public T get() {
      return this.value;
   }

   public void set(T newValue) {
      T oldValue = this.value;
      this.value = newValue;
      if (!Objects.equals(this.value, oldValue)) {
         this.notifyObservers(newValue);
      }

   }

   public void observe(Consumer<T> observer) {
      this.observers.add(observer);
   }

   protected void notifyObservers(T value) {
      for(Consumer<T> observer : this.observers) {
         observer.accept(value);
      }

   }
}
