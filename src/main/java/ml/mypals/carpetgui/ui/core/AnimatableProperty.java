package ml.mypals.carpetgui.ui.core;

import ml.mypals.carpetgui.ui.util.Observable;

public class AnimatableProperty<T> extends Observable<T> {
   protected AnimatableProperty(T initial) {
      super(initial);
   }

   public static <T> AnimatableProperty<T> of(T initial) {
      return new AnimatableProperty<T>(initial);
   }

   public void update(float delta) {
   }
}
