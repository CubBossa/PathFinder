package de.cubbossa.pathfinder.visualizer;import de.cubbossa.pathfinder.misc.NamespacedKey;import de.cubbossa.pathfinder.AbstractPathFinder;import de.cubbossa.pathfinder.command.Arguments;import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;import de.cubbossa.pathfinder.messages.Messages;import de.cubbossa.pathfinder.util.BukkitUtils;import de.cubbossa.pathfinder.visualizer.impl.BezierVisualizerType;import de.cubbossa.translations.Message;import dev.jorel.commandapi.arguments.Argument;import dev.jorel.commandapi.arguments.TextArgument;import java.util.LinkedHashMap;import java.util.Map;import javax.script.ScriptEngine;import lombok.Getter;import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;import org.pf4j.Extension;@Getter@Extension(points = VisualizerType.class)public class ScriptLineParticleVisualizerType    extends BezierVisualizerType<ScriptLineParticleVisualizer>    implements VisualizerTypeMessageExtension<ScriptLineParticleVisualizer> {  private final ScriptEngine javaScriptEngine;  public ScriptLineParticleVisualizerType() {    super(AbstractPathFinder.pathfinder("scriptline"));    javaScriptEngine = new NashornScriptEngineFactory().getScriptEngine("JavaScript");  }  @Override  public ScriptLineParticleVisualizer createVisualizerInstance(NamespacedKey key) {    return new ScriptLineParticleVisualizer(key, javaScriptEngine);  }  @Override  public Message getInfoMessage(ScriptLineParticleVisualizer element) {    return Messages.CMD_ADV_VIS_INFO_PARTICLES.formatted(        Messages.formatter().number("point-distance", element.getPointDistance()),        Placeholder.parsed("particle", element.getParticleFunction()),        Placeholder.parsed("particle-data", element.getParticleDataFunction()),        Placeholder.parsed("speed", element.getSpeedFunction()),        Placeholder.parsed("amount", element.getAmountFunction()),        Placeholder.parsed("offset-x", element.getParticleOffsetXFunction()),        Placeholder.parsed("offset-y", element.getParticleOffsetYFunction()),        Placeholder.parsed("offset-z", element.getParticleOffsetZFunction()),        Placeholder.parsed("path-x", element.getPathOffsetXFunction()),        Placeholder.parsed("path-y", element.getPathOffsetYFunction()),        Placeholder.parsed("path-z", element.getPathOffsetZFunction())    );  }  @Override  public String getCommandName() {    return "scriptline";  }  @Override  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex,                                       int argumentOffset) {    return super.appendEditCommand(tree, visualizerIndex, argumentOffset)        .then(Arguments.literal("particle")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "particle-steps",                        vis::getParticleFunction, vis::setParticleFunction);                  }                })))        .then(Arguments.literal("particle-data")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "particle-data",                        vis::getParticleDataFunction, vis::setParticleDataFunction);                  }                })))        .then(Arguments.literal("particle-steps")            .then(Arguments.integer("amount", 1)                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (Integer) args.getUnchecked(1), "particle-steps",                        vis::getSchedulerSteps, vis::setSchedulerSteps);                  }                })))        .then(Arguments.literal("amount")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "amount",                        vis::getAmountFunction, vis::setAmountFunction);                  }                })))        .then(Arguments.literal("speed")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "speed",                        vis::getSpeedFunction, vis::setSpeedFunction);                  }                })))        .then(Arguments.literal("offset-x")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "offset-x",                        vis::getParticleOffsetXFunction, vis::setParticleOffsetXFunction);                  }                })))        .then(Arguments.literal("offset-y")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "offset-y",                        vis::getParticleOffsetYFunction, vis::setParticleOffsetYFunction);                  }                })))        .then(Arguments.literal("offset-z")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "offset-z",                        vis::getParticleOffsetZFunction, vis::setParticleOffsetZFunction);                  }                })))        .then(Arguments.literal("path-x")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "path-x",                        vis::getPathOffsetXFunction, vis::setPathOffsetXFunction);                  }                })))        .then(Arguments.literal("path-y")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "path-y",                        vis::getPathOffsetYFunction, vis::setPathOffsetYFunction);                  }                })))        .then(Arguments.literal("path-z")            .then(new TextArgument("java-script")                .executes((commandSender, args) -> {                  if (args.get(0) instanceof ScriptLineParticleVisualizer vis) {                    setProperty(BukkitUtils.wrap(commandSender), vis, (String) args.getUnchecked(1), "path-z",                        vis::getPathOffsetZFunction, vis::setPathOffsetZFunction);                  }                })));  }  @Override  public Map<String, Object> serialize(ScriptLineParticleVisualizer visualizer) {    super.serialize(visualizer);    Map<String, Object> map = new LinkedHashMap<>();    map.put("particle-steps", visualizer.getSchedulerSteps());    map.put("interval", visualizer.getInterval());    map.put("particle", visualizer.getParticleFunction());    map.put("particle-data", visualizer.getParticleDataFunction());    map.put("speed", visualizer.getSpeedFunction());    map.put("amount", visualizer.getAmountFunction());    map.put("offset-x", visualizer.getParticleOffsetXFunction());    map.put("offset-y", visualizer.getParticleOffsetZFunction());    map.put("offset-z", visualizer.getParticleOffsetZFunction());    map.put("path-x", visualizer.getPathOffsetXFunction());    map.put("path-y", visualizer.getPathOffsetYFunction());    map.put("path-z", visualizer.getPathOffsetZFunction());    map.put("sample-rate", visualizer.getBezierSamplingRate());    map.put("point-distance", visualizer.getPointDistance());    return map;  }  @Override  public void deserialize(ScriptLineParticleVisualizer visualizer, Map<String, Object> values) {    super.deserialize(visualizer, values);    if (values.containsKey("particle-steps")) {      visualizer.setSchedulerSteps((Integer) values.get("particle-steps"));    }    if (values.containsKey("interval")) {      visualizer.setInterval((Integer) values.get("interval"));    }    if (values.containsKey("particle")) {      visualizer.setParticleFunction((String) values.get("particle"));    }    if (values.containsKey("particle-data")) {      visualizer.setParticleDataFunction((String) values.get("particle-data"));    }    if (values.containsKey("speed")) {      visualizer.setSpeedFunction((String) values.get("speed"));    }    if (values.containsKey("amount")) {      visualizer.setAmountFunction((String) values.get("amount"));    }    if (values.containsKey("offset-x")) {      visualizer.setParticleOffsetXFunction((String) values.get("offset-x"));    }    if (values.containsKey("offset-y")) {      visualizer.setParticleOffsetYFunction((String) values.get("offset-y"));    }    if (values.containsKey("offset-z")) {      visualizer.setParticleOffsetZFunction((String) values.get("offset-z"));    }    if (values.containsKey("path-x")) {      visualizer.setPathOffsetXFunction((String) values.get("path-x"));    }    if (values.containsKey("path-y")) {      visualizer.setPathOffsetYFunction((String) values.get("path-y"));    }    if (values.containsKey("path-z")) {      visualizer.setPathOffsetZFunction((String) values.get("path-z"));    }    if (values.containsKey("sample-rate")) {      visualizer.setBezierSamplingRate((Integer) values.get("sample-rate"));    }    if (values.containsKey("point-distance")) {      visualizer.setPointDistance(((Double) values.get("point-distance")).floatValue());    }  }}