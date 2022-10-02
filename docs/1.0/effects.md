## What is NBO and how does it work

NBO files are made to serialize and deserialize objects which stand in relation to each other.
It is pretty similar to JSON and NBT and stands, almost like NBT, for "Named Binary Objects".

```
<include "some_other_file.nbo">
<with Effect as de.cubbossa.effects.EffectPlayer>
<with Particle as de.cubbossa.effects.ParticlePlayer>
<with Sound as de.cubbossa.effects.SoundPlayer>

sound := Sound{
  sound: "minecraft:entity.firework_rocket.blast",
  volume: 1f,
  pitch: 1f
}

effect := Effect{
    dalay_0: [...]
    delay_20: [
      &sound, # <---- reference to previously declared sound object
      Sound{sound: "minecraft:entity.firework_rocket.blast", volume: 0.5f, pitch: 1.4f}
    ]
}
```

The structure of the file divides into includes, imports and objects. You can imagine it to be a table of objects, each object with its corresponding key. If an object can be made out of other objects, like in the example above, you can reference the object and insert it by using the '&' symbol and the key of the previously declared object.
Keep in mind, that the file will be read from top to bottom, you can therefore never reference an object that has not yet been declared or included from another file.

Includes are references to other files, which are loaded first and whose results are then stored in the 'table' mentioned earlier. This means, you can reference any object that you declared in an included file. This also accounts for imports. You can move all your imports to an imports.nbo file and start every other file with `<includes "imports.nbo">`.

Imports are formatted like in this example: `<with SomeShortName as some.pretty.long.package.and.class.name>`.
It can be understood as an alias. As you can see in the example above, Sound Objects are declared with `Sound{attributes...}`. To make sure, that the applications can interpret the sound as an actual code object, you have to tell it which Sound is meant. This has to be done with an import.

## Which objects are possible?

In context of this plugin, the following imports are valid, where the alias after "with" can be named however you like:
```
<with ActionBar as de.cubbossa.pathfinder.serializedeffects.effects.ActionBarPlayer>
<with Cooldown as de.cubbossa.pathfinder.serializedeffects.effects.Cooldown>
<with Effect as de.cubbossa.pathfinder.serializedeffects.effects.EffectPlayer>
<with Message as de.cubbossa.pathfinder.serializedeffects.effects.MessagePlayer>
<with ParticleLine as de.cubbossa.pathfinder.serializedeffects.effects.ParticleLinePlayer>
<with Particle as de.cubbossa.pathfinder.serializedeffects.effects.ParticlePlayer>
<with ResetActionBar as de.cubbossa.pathfinder.serializedeffects.effects.ResetActionBar>
<with ResetTitle as de.cubbossa.pathfinder.serializedeffects.effects.ResetTitle>
<with Sound as de.cubbossa.pathfinder.serializedeffects.effects.SoundPlayer>
<with Title as de.cubbossa.pathfinder.serializedeffects.effects.TitlePlayer>
<with WorldEffect as de.cubbossa.pathfinder.serializedeffects.effects.WorldEffectPlayer>
```

Each of these objects plays an effect to a player. For example, the Message object will send the player a certain message. Which message to send has to be defined as parameter, like so: `Message{text: '<green>You have been notified about something.</green>'}`

Here are all types of effect players with their parameters explained:

<table>
<tr> <td>Type</td> <td>Example</td> <td>Description</td></tr>
<tr> <td>ActionBar</td> <td>

```
ActionBarPlayer {
  text: '<red>Some <msg:example></red>'
}
```
</td> <td>Plays an action bar with the provided text. All minimessage tags are possible, as well as the message tags from the language files.</td></tr>
<tr> <td>Cooldown</td> <td>

```
Coolown {
  ticks: 10
}
```
</td> <td>Sets a cooldown for the parent effect, so that the next call on the effect will only appear after x ticks have passed.</td>
</tr>
<tr> <td>Effect</td> <td>

```
Effect {
  delay_0: [ &soundEffect, &cooldown, ActionBar {text: 'example'}]
  delay_10: &otherSoundEffect, &particleEffect
}
```
</td> <td>Combines multiple effects with delay if required. Name each attribute 'delay_' + ticks to wait and then add all the required effects to the list.</td>
</tr>
</table>


## And now?

The names of some view effects are reserved for the plugins use. For example, the effect 'discover' will always be called when a player discoveres a discoverable location ingame. You can now change everything so that the discovering has the effect that appeals most to you.
