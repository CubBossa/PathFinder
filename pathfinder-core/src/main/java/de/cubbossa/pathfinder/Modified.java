package de.cubbossa.pathfinder;

public interface Modified {

  <C extends Modifier> boolean hasModifier(Class<C> modifierClass);
  
  void addModifier(Modifier modifier);
  
  <C extends Modifier> Modifier getModifier(Class<C> modifierClass);

  <C extends Modifier> C removeModifier(Class<C> modifierClass);

  <C extends Modifier> C removeModifier(C modifier);
  
  void clearModifiers();
}
