package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.TextArgument;
import java.util.Map;
import javax.script.ScriptEngine;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class ScriptLineParticleVisualizerType
    extends BezierVisualizerType<ScriptLineParticleVisualizer> {

  @Getter
  private final ScriptEngine javaScriptEngine;

  public ScriptLineParticleVisualizerType(NamespacedKey key) {
    super(key);
    javaScriptEngine = new NashornScriptEngineFactory().getScriptEngine("JavaScript");
  }

  @Override
  public ScriptLineParticleVisualizer create(NamespacedKey key, String nameFormat) {
    return new ScriptLineParticleVisualizer(key, nameFormat);
  }

  @Override
  public Message getInfoMessage(ScriptLineParticleVisualizer element) {
    return Messages.CMD_ADV_VIS_INFO_PARTICLES.format(TagResolver.builder()
        .resolver(Formatter.number("point-distance", element.getPointDistance()))
        .resolver(Placeholder.parsed("particle", element.getParticleFunction()))
        .resolver(Placeholder.parsed("particle-data", element.getParticleDataFunction()))
        .resolver(Placeholder.parsed("speed", element.getSpeedFunction()))
        .resolver(Placeholder.parsed("amount", element.getAmountFunction()))
        .resolver(Placeholder.parsed("offset-x", element.getParticleOffsetXFunction()))
        .resolver(Placeholder.parsed("offset-y", element.getParticleOffsetYFunction()))
        .resolver(Placeholder.parsed("offset-z", element.getParticleOffsetZFunction()))
        .resolver(Placeholder.parsed("path-x", element.getPathOffsetXFunction()))
        .resolver(Placeholder.parsed("path-y", element.getPathOffsetYFunction()))
        .resolver(Placeholder.parsed("path-z", element.getPathOffsetZFunction()))
        .build());
  }

  @Override
  public String getCommandName() {
    return "scriptline";
  }

  @Override
  public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex,
                                        int argumentOffset) {
    return super.appendEditCommand(tree, visualizerIndex, argumentOffset)
        .then(CustomArgs.literal("particle")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "particle-steps",
                            true, vis::getParticleFunction, vis::setParticleFunction);
                  }
                })))
        .then(CustomArgs.literal("particle-data")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "particle-data", true,
                            vis::getParticleDataFunction, vis::setParticleDataFunction);
                  }
                })))
        .then(CustomArgs.literal("particle-steps")
            .then(CustomArgs.integer("amount", 1)
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (Integer) objects[1], "particle-steps",
                            true, vis::getSchedulerSteps, vis::setSchedulerSteps);
                  }
                })))
        .then(CustomArgs.literal("amount")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "amount", true,
                            vis::getAmountFunction, vis::setAmountFunction);
                  }
                })))
        .then(CustomArgs.literal("speed")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "speed", true,
                            vis::getSpeedFunction, vis::setSpeedFunction);
                  }
                })))
        .then(CustomArgs.literal("offset-x")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "offset-x", true,
                            vis::getParticleOffsetXFunction, vis::setParticleOffsetXFunction);
                  }
                })))
        .then(CustomArgs.literal("offset-y")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "offset-y", true,
                            vis::getParticleOffsetYFunction, vis::setParticleOffsetYFunction);
                  }
                })))
        .then(CustomArgs.literal("offset-z")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "offset-z", true,
                            vis::getParticleOffsetZFunction, vis::setParticleOffsetZFunction);
                  }
                })))
        .then(CustomArgs.literal("path-x")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "path-x", true,
                            vis::getPathOffsetXFunction, vis::setPathOffsetXFunction);
                  }
                })))
        .then(CustomArgs.literal("path-y")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "path-y", true,
                            vis::getPathOffsetYFunction, vis::setPathOffsetYFunction);
                  }
                })))
        .then(CustomArgs.literal("path-z")
            .then(new TextArgument("java-script")
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (String) objects[1], "path-z", true,
                            vis::getPathOffsetZFunction, vis::setPathOffsetZFunction);
                  }
                })));
  }


  @Override
  public Map<String, Object> serialize(ScriptLineParticleVisualizer visualizer) {
    super.serialize(visualizer);
    return new LinkedHashMapBuilder<String, Object>()
        .put("particle-steps", visualizer.getSchedulerSteps())
        .put("interval", visualizer.getInterval())
        .put("particle", visualizer.getParticleFunction())
        .put("particle-data", visualizer.getParticleDataFunction())
        .put("speed", visualizer.getSpeedFunction())
        .put("amount", visualizer.getAmountFunction())
        .put("offset-x", visualizer.getParticleOffsetXFunction())
        .put("offset-y", visualizer.getParticleOffsetZFunction())
        .put("offset-z", visualizer.getParticleOffsetZFunction())
        .put("path-x", visualizer.getPathOffsetXFunction())
        .put("path-y", visualizer.getPathOffsetYFunction())
        .put("path-z", visualizer.getPathOffsetZFunction())
        .put("sample-rate", visualizer.getBezierSamplingRate())
        .put("point-distance", visualizer.getPointDistance())
        .build();
  }

  @Override
  public void deserialize(ScriptLineParticleVisualizer visualizer, Map<String, Object> values) {
    super.deserialize(visualizer, values);
    if (values.containsKey("particle-steps")) {
      visualizer.setSchedulerSteps((Integer) values.get("particle-steps"));
    }
    if (values.containsKey("interval")) {
      visualizer.setInterval((Integer) values.get("interval"));
    }
    if (values.containsKey("particle")) {
      visualizer.setParticleFunction((String) values.get("particle"));
    }
    if (values.containsKey("particle-data")) {
      visualizer.setParticleDataFunction((String) values.get("particle-data"));
    }
    if (values.containsKey("speed")) {
      visualizer.setSpeedFunction((String) values.get("speed"));
    }
    if (values.containsKey("amount")) {
      visualizer.setAmountFunction((String) values.get("amount"));
    }
    if (values.containsKey("offset-x")) {
      visualizer.setParticleOffsetXFunction((String) values.get("offset-x"));
    }
    if (values.containsKey("offset-y")) {
      visualizer.setParticleOffsetYFunction((String) values.get("offset-y"));
    }
    if (values.containsKey("offset-z")) {
      visualizer.setParticleOffsetZFunction((String) values.get("offset-z"));
    }
    if (values.containsKey("path-x")) {
      visualizer.setPathOffsetXFunction((String) values.get("path-x"));
    }
    if (values.containsKey("path-y")) {
      visualizer.setPathOffsetYFunction((String) values.get("path-y"));
    }
    if (values.containsKey("path-z")) {
      visualizer.setPathOffsetZFunction((String) values.get("path-z"));
    }
    if (values.containsKey("sample-rate")) {
      visualizer.setBezierSamplingRate((Integer) values.get("sample-rate"));
    }
    if (values.containsKey("point-distance")) {
      visualizer.setPointDistance(((Double) values.get("point-distance")).floatValue());
    }
  }
}
