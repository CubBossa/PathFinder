package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A command to manage all roadmap instances.
 */
public class RoadMapCommand extends Command {

  /**
   * A command to manage all roadmap instances.
   */
  public RoadMapCommand() {
    super("roadmap");
    withAliases("rm");
    withGeneratedHelp();

    withRequirement(sender ->
        sender.hasPermission(PathPlugin.PERM_CMD_RM_INFO)
            || sender.hasPermission(PathPlugin.PERM_CMD_RM_CREATE)
            || sender.hasPermission(PathPlugin.PERM_CMD_RM_DELETE)
            || sender.hasPermission(PathPlugin.PERM_CMD_RM_EDITMODE)
            || sender.hasPermission(PathPlugin.PERM_CMD_RM_FORCEFIND)
            || sender.hasPermission(PathPlugin.PERM_CMD_RM_FORCEFORGET)
            || sender.hasPermission(PathPlugin.PERM_CMD_RM_SET_VIS)
            || sender.hasPermission(PathPlugin.PERM_CMD_RM_SET_NAME)
            || sender.hasPermission(PathPlugin.PERM_CMD_RM_SET_CURVE)
    );

    then(CustomArgs.literal("info")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_RM_INFO)
        .then(CustomArgs.roadMapArgument("roadmap")
            .executes((commandSender, args) -> {
              onInfo(commandSender, (RoadMap) args[0]);
            })));

    then(CustomArgs.literal("create")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_RM_CREATE)
        .then(new StringArgument("key")
            .executes((player, args) -> {
              onCreate(player, new NamespacedKey(PathPlugin.getInstance(), (String) args[0]));
            })));

    then(CustomArgs.literal("delete")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_RM_DELETE)
        .then(CustomArgs.roadMapArgument("roadmap")
            .executes((commandSender, args) -> {
              onDelete(commandSender, (RoadMap) args[0]);
            })));

    then(CustomArgs.literal("editmode")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_RM_EDITMODE)
        .executesPlayer((player, args) -> {
          if (RoadMapHandler.getInstance().getRoadMaps().size() != 1) {
            TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_PROVIDE_RM, player);
            return;
          }
          onEdit(player, RoadMapHandler.getInstance().getRoadMaps().values().iterator().next());
        })
        .then(CustomArgs.roadMapArgument("roadmap")
            .executesPlayer((player, objects) -> {
              onEdit(player, (RoadMap) objects[0]);
            })));

    then(CustomArgs.literal("list")
        .withPermission(PathPlugin.PERM_CMD_RM_LIST)
        .executes((commandSender, args) -> {
          onList(commandSender, 1);
        })
        .then(CustomArgs.integer("page", 1)
            .displayAsOptional()
            .executes((commandSender, args) -> {
              onList(commandSender, (Integer) args[0]);
            })));

    then(CustomArgs.literal("forcefind")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_RM_FORCEFIND)
        .then(CustomArgs.roadMapArgument("roadmap")
            .then(CustomArgs.player("player")
                .withGeneratedHelp()
                .then(CustomArgs.discoverableArgument("discovering")
                    .withGeneratedHelp()
                    .executes((commandSender, args) -> {
                      onForceFind(commandSender, (Player) args[1], (Discoverable) args[2]);
                    })))));
    then(CustomArgs.literal("forceforget")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_RM_FORCEFORGET)
        .then(CustomArgs.roadMapArgument("roadmap")
            .withGeneratedHelp()
            .then(CustomArgs.player("player")
                .withGeneratedHelp()
                .then(CustomArgs.discoverableArgument("discovering")
                    .executes((commandSender, args) -> {
                      onForceForget(commandSender, (Player) args[1], (Discoverable) args[2]);
                    })))));

    then(CustomArgs.literal("edit")
        .withGeneratedHelp()
        .withRequirement(sender ->
            sender.hasPermission(PathPlugin.PERM_CMD_RM_SET_VIS)
                || sender.hasPermission(PathPlugin.PERM_CMD_RM_SET_NAME)
                || sender.hasPermission(PathPlugin.PERM_CMD_RM_SET_CURVE)
        )
        .then(CustomArgs.roadMapArgument("roadmap")
            .withGeneratedHelp()
            .then(CustomArgs.literal("visualizer")
                .withGeneratedHelp()
                .withPermission(PathPlugin.PERM_CMD_RM_SET_VIS)
                .then(CustomArgs.pathVisualizerArgument("visualizer")
                    .executes((commandSender, args) -> {
                      onStyle(commandSender, (RoadMap) args[0], (PathVisualizer<?, ?>) args[1]);
                    })))

            .then(CustomArgs.literal("name")
                .withGeneratedHelp()
                .withPermission(PathPlugin.PERM_CMD_RM_SET_NAME)
                .then(CustomArgs.miniMessageArgument("name")
                    .executes((commandSender, args) -> {
                      onRename(commandSender, (RoadMap) args[0], (String) args[1]);
                    })))
            .then(CustomArgs.literal("curve-length")
                .withGeneratedHelp()
                .withPermission(PathPlugin.PERM_CMD_RM_SET_CURVE)
                .then(new DoubleArgument("curvelength", 0)
                    .executes((commandSender, args) -> {
                      onChangeTangentStrength(commandSender, (RoadMap) args[0], (Double) args[1]);
                    })))));
  }

  private void onInfo(CommandSender sender, RoadMap roadMap) {

    FormattedMessage message = Messages.CMD_RM_INFO.format(TagResolver.builder()
        .tag("key", Messages.formatKey(roadMap.getKey()))
        .resolver(Placeholder.component("name", roadMap.getDisplayName()))
        .resolver(Placeholder.component("name-format", Component.text(roadMap.getNameFormat())))
        .resolver(Placeholder.component("nodes",
            Messages.formatNodeSelection(sender, roadMap.getNodes())))
        .resolver(Placeholder.component("groups", Messages.formatNodeGroups(sender,
            NodeGroupHandler.getInstance().getNodeGroups(roadMap))))
        .resolver(Placeholder.unparsed("curve-length", roadMap.getDefaultCurveLength() + ""))
        .resolver(Placeholder.component("path-visualizer",
            roadMap.getVisualizer() == null ? Messages.GEN_NULL.asComponent() :
                roadMap.getVisualizer().getDisplayName()))
        .build());

    TranslationHandler.getInstance().sendMessage(message, sender);
  }


  private void onCreate(CommandSender sender, NamespacedKey key) {

    try {
      RoadMap roadMap = RoadMapHandler.getInstance()
          .createRoadMap(PathPlugin.getInstance(), key.getKey(), true, true);
      TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_SUCCESS
          .format(TagResolver.resolver("name", Tag.inserting(roadMap.getDisplayName()))), sender);

    } catch (IllegalArgumentException e) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_DUPLICATE_KEY, sender);
    } catch (Exception e) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_FAIL, sender);
      e.printStackTrace();
    }
  }

  private void onDelete(CommandSender sender, RoadMap roadMap) {

    RoadMapHandler.getInstance().deleteRoadMap(roadMap);
    TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_DELETE.format(
        TagResolver.resolver("roadmap", Tag.inserting(roadMap.getDisplayName()))), sender);
  }

  private void onEdit(Player player, RoadMap roadMap) {

    TagResolver r = TagResolver.resolver("roadmap", Tag.inserting(roadMap.getDisplayName()));
    boolean wasEditing;
    try {
      wasEditing = RoadMapHandler.getInstance().getRoadMapEditor(roadMap.getKey())
          .toggleEditMode(player.getUniqueId());
    } catch (IllegalStateException e) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_NO_IMPL, player);
      e.printStackTrace();
      return;
    }
    if (wasEditing) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_ACTIVATED.format(r), player);
    } else {
      TranslationHandler.getInstance()
          .sendMessage(Messages.CMD_RM_EM_DEACTIVATED.format(r), player);
    }
  }

  /**
   * Lists all roadmaps.
   *
   * @param page first page is 1, not 0!
   */
  private void onList(CommandSender sender, Integer page) {

    CommandUtils.printList(
        sender,
        page,
        10,
        new ArrayList<>(RoadMapHandler.getInstance().getRoadMaps().values()),
        roadMap -> {
          TagResolver r = TagResolver.builder()
              .tag("key", Messages.formatKey(roadMap.getKey()))
              .resolver(Placeholder.component("name", roadMap.getDisplayName()))
              .resolver(Placeholder.unparsed("curve-length", roadMap.getDefaultCurveLength() + ""))
              .resolver(Placeholder.component("path-visualizer",
                  roadMap.getVisualizer() == null ? Messages.GEN_NULL.asComponent() :
                      roadMap.getVisualizer().getDisplayName()))
              .build();

          TranslationHandler.getInstance()
              .sendMessage(Messages.CMD_RM_LIST_ENTRY.format(r), sender);
        },
        Messages.CMD_RM_LIST_HEADER,
        Messages.CMD_RM_LIST_FOOTER);
  }

  private void onForceFind(CommandSender sender, Player target, Discoverable discoverable) {

    DiscoverHandler.getInstance().discover(target.getUniqueId(), discoverable, LocalDateTime.now());

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_RM_FORCE_FIND.format(TagResolver.builder()
            .resolver(Placeholder.component("name",
                PathPlugin.getInstance().getAudiences().player(target)
                    .getOrDefault(Identity.DISPLAY_NAME, Component.text(target.getName()))))
            .tag("discovery", Tag.inserting(discoverable.getDisplayName())).build()), sender);
  }

  private void onForceForget(CommandSender sender, Player target, Discoverable discoverable) {

    DiscoverHandler.getInstance().forget(target.getUniqueId(), discoverable);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_RM_FORCE_FORGET.format(TagResolver.builder()
            .resolver(Placeholder.component("name",
                PathPlugin.getInstance().getAudiences().player(target)
                    .getOrDefault(Identity.DISPLAY_NAME, Component.text(target.getName()))))
            .tag("discovery", Tag.inserting(discoverable.getDisplayName())).build()), sender);
  }

  private void onStyle(CommandSender sender, RoadMap roadMap, PathVisualizer<?, ?> visualizer)
      throws WrapperCommandSyntaxException {
    PathVisualizer<?, ?> old = roadMap.getVisualizer();

    if (!RoadMapHandler.getInstance().setRoadMapVisualizer(roadMap, visualizer)) {
      return;
    }

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_RM_SET_VISUALIZER.format(TagResolver.builder()
            .tag("key", Messages.formatKey(roadMap.getKey()))
            .resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
            .resolver(Placeholder.component("old-value",
                old == null ? Messages.GEN_NULL.asComponent(sender) : old.getDisplayName()))
            .resolver(Placeholder.component("value", roadMap.getVisualizer().getDisplayName()))
            .build()), sender);
  }

  private void onRename(CommandSender sender, RoadMap roadMap, String nameNew)
      throws WrapperCommandSyntaxException {
    Component old = roadMap.getDisplayName();

    if (!RoadMapHandler.getInstance().setRoadMapName(roadMap, nameNew)) {
      return;
    }

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_RM_SET_NAME.format(TagResolver.builder()
            .tag("key", Messages.formatKey(roadMap.getKey()))
            .resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
            .resolver(Placeholder.component("old-value", old))
            .resolver(Placeholder.unparsed("name-format", roadMap.getNameFormat()))
            .resolver(Placeholder.component("value", roadMap.getDisplayName()))
            .build()), sender);
  }

  private void onChangeTangentStrength(CommandSender sender, RoadMap roadMap, double strength)
      throws WrapperCommandSyntaxException {
    double old = roadMap.getDefaultCurveLength();

    if (!RoadMapHandler.getInstance().setRoadMapCurveLength(roadMap, strength)) {
      return;
    }

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_RM_SET_CURVED.format(TagResolver.builder()
            .tag("key", Messages.formatKey(roadMap.getKey()))
            .resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
            .resolver(Placeholder.component("old-value", Component.text(old)))
            .resolver(
                Placeholder.component("value", Component.text(roadMap.getDefaultCurveLength())))
            .build()), sender);
  }
}