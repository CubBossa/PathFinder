package de.cubbossa.pathfinder.api.group;

public interface Modified {

  <C extends Modifier> boolean hasModifier(Class<C> modifierClass);
  
  void addModifier(Modifier modifier);
  
  <C extends Modifier> C getModifier(Class<C> modifierClass);

  <C extends Modifier> C removeModifier(Class<C> modifierClass);

  <C extends Modifier> C removeModifier(C modifier);
  
  void clearModifiers();
}
