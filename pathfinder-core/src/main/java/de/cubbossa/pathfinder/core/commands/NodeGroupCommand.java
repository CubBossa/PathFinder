package de.cubbossa.pathfinder.core.commands;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroupHandler;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.StringUtils;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

/**
 * A command to manage NodeGroups.
 */
public class NodeGroupCommand extends Command {

  /**
   * A command to manage NodeGroups.
   *
   * @param offset The argument index offset. Increase if this command is also a child of another
   *               command with non-static arguments.
   */
  public NodeGroupCommand(int offset) {
    super("nodegroup");
    withGeneratedHelp();

    withRequirement(sender -> sender.hasPermission(PathPlugin.PERM_CMD_NG_LIST)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_CREATE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_DELETE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_ADD)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_REMOVE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_LIST)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_NAME)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_PERM)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_NAVIGABLE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVERABLE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVER_DIST)
    );

    then(CustomArgs.literal("list")
        .withPermission(PathPlugin.PERM_CMD_NG_LIST)
        .executes((sender, objects) -> {
          listGroups(sender, 1);
        })
        .then(CustomArgs.integer("page", 1)
            .displayAsOptional()
            .executes((sender, objects) -> {
              listGroups(sender, (int) objects[offset]);
            })));

    then(CustomArgs.literal("create")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_NG_CREATE)
        .then(new StringArgument("name")
            .executes((sender, args) -> {
              createGroup(sender,
                  new NamespacedKey(PathPlugin.getInstance(), args[offset].toString()));
            })));

    then(CustomArgs.literal("delete")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_NG_DELETE)
        .then(CustomArgs.nodeGroupArgument("group")
            .executes((sender, objects) -> {
              deleteGroup(sender, (NodeGroup) objects[offset]);
            })));

    then(CustomArgs.literal("search-terms")
        .withGeneratedHelp()
        .withRequirement(sender ->
            sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_ADD)
                || sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_REMOVE)
                || sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_LIST)
        )

        .then(CustomArgs.literal("add")
            .withGeneratedHelp()
            .withPermission(PathPlugin.PERM_CMD_NG_ST_ADD)
            .then(CustomArgs.nodeGroupArgument("group")
                .then(CustomArgs.suggestCommaSeparatedList("search-terms")
                    .executes((sender, objects) -> {
                      searchTermsAdd(sender, (NodeGroup) objects[offset],
                          (String) objects[offset + 1]);
                    }))))
        .then(CustomArgs.literal("remove")
            .withGeneratedHelp()
            .withPermission(PathPlugin.PERM_CMD_NG_ST_REMOVE)
            .then(CustomArgs.nodeGroupArgument("group")
                .then(CustomArgs.suggestCommaSeparatedList("search-terms")
                    .executes((sender, objects) -> {
                      searchTermsRemove(sender, (NodeGroup) objects[offset],
                          (String) objects[offset + 1]);
                    }))))
        .then(CustomArgs.literal("list")
            .withGeneratedHelp()
            .withPermission(PathPlugin.PERM_CMD_NG_ST_LIST)
            .then(CustomArgs.nodeGroupArgument("group")
                .executes((sender, objects) -> {
                  searchTermsList(sender, (NodeGroup) objects[offset]);
                }))));
    then(CustomArgs.literal("edit")
        .withGeneratedHelp()
        .withRequirement(sender -> sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_NAME)
            || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_PERM)
            || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_NAVIGABLE)
            || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVERABLE)
            || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVER_DIST)
        )
        .then(CustomArgs.nodeGroupArgument("group")
            .withGeneratedHelp()
            .then(CustomArgs.literal("name")
                .withGeneratedHelp()
                .withPermission(PathPlugin.PERM_CMD_NG_SET_NAME)
                .then(CustomArgs.miniMessageArgument("name",
                        i -> Lists.newArrayList(((NodeGroup) i.previousArgs()[0]).getNameFormat()))
                    .executes((sender, objects) -> {
                      renameGroup(sender, (NodeGroup) objects[offset],
                          (String) objects[offset + 1]);
                    })
                ))
            .then(CustomArgs.literal("permission")
                .withGeneratedHelp()
                .withPermission(PathPlugin.PERM_CMD_NG_SET_PERM)
                .then(new GreedyStringArgument("permission")
                    .executes((sender, objects) -> {
                      setGroupPermission(sender, (NodeGroup) objects[offset],
                          (String) objects[offset + 1]);
                    })
                ))
            .then(CustomArgs.literal("navigable")
                .withGeneratedHelp()
                .withPermission(PathPlugin.PERM_CMD_NG_SET_NAVIGABLE)
                .then(new BooleanArgument("value")
                    .executes((sender, objects) -> {
                      setGroupNavigable(sender, (NodeGroup) objects[offset],
                          (Boolean) objects[offset + 1]);
                    })
                ))
            .then(CustomArgs.literal("discoverable")
                .withGeneratedHelp()
                .withPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVERABLE)
                .then(new BooleanArgument("value")
                    .executes((sender, objects) -> {
                      setGroupDiscoverable(sender, (NodeGroup) objects[offset],
                          (Boolean) objects[offset + 1]);
                    })))
            .then(CustomArgs.literal("find-distance")
                .withGeneratedHelp()
                .withPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVER_DIST)
                .then(new FloatArgument("value", 0.01f)
                    .executes((sender, objects) -> {
                      setGroupDiscoverDist(sender, (NodeGroup) objects[offset],
                          (Float) objects[offset + 1]);
                    })))));
  }

  private void searchTermsList(CommandSender sender, NodeGroup group) {
    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_NG_TERMS_LIST.format(TagResolver.builder()
            .resolver(Placeholder.component("name", group.getDisplayName()))
            .resolver(Placeholder.component("values", toList(group.getSearchTermStrings())))
            .build()), sender);
  }

  private void searchTermsAdd(CommandSender sender, NodeGroup group, String commaSeparatedList) {
    Collection<String> toAdd = Arrays.stream(commaSeparatedList.split(","))
        .map(String::trim)
        .map(String::toLowerCase)
        .toList();
    group.addSearchTermStrings(toAdd);
    Bukkit.getPluginManager().callEvent(new NodeGroupSearchTermsChangedEvent(
        group, NodeGroupSearchTermsChangedEvent.Action.ADD, toAdd
    ));

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_NG_TERMS_ADD.format(TagResolver.builder()
            .resolver(Placeholder.component("name", group.getDisplayName()))
            .resolver(Placeholder.component("values", toList(toAdd)))
            .build()), sender);
  }

  private void searchTermsRemove(CommandSender sender, NodeGroup group, String commaSeparatedList) {
    Collection<String> toRemove = Arrays.stream(commaSeparatedList.split(","))
        .map(String::trim)
        .map(String::toLowerCase)
        .toList();
    group.removeSearchTermStrings(toRemove);

    Bukkit.getPluginManager().callEvent(new NodeGroupSearchTermsChangedEvent(
        group, NodeGroupSearchTermsChangedEvent.Action.REMOVE, toRemove
    ));

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_NG_TERMS_REMOVE.format(TagResolver.builder()
            .resolver(Placeholder.component("name", group.getDisplayName()))
            .resolver(Placeholder.component("values", toList(toRemove)))
            .build()), sender);
  }

  private Component toList(Collection<String> tags) {
    return Component.join(JoinConfiguration.separator(Component.text(",", NamedTextColor.GRAY)),
        tags.stream()
            .map(Component::text).collect(Collectors.toList()));
  }

  private void renameGroup(CommandSender sender, NodeGroup group, String newName) {

    Component oldName = group.getDisplayName();
    NodeGroupHandler.getInstance().setNodeGroupName(group, newName);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_NG_SET_NAME.format(TagResolver.builder()
            .tag("key", Messages.formatKey(group.getKey()))
            .resolver(Placeholder.component("name", oldName))
            .resolver(Placeholder.component("new-name", group.getDisplayName()))
            .resolver(Placeholder.component("value", Component.text(group.getNameFormat())))
            .build()), sender);
  }

  private void setGroupPermission(CommandSender sender, NodeGroup group, String permission) {

    permission = permission.equalsIgnoreCase("null") || permission.equalsIgnoreCase("none") ? null :
        permission;

    @Nullable
    String oldValue = group.getPermission();
    NodeGroupHandler.getInstance().setNodeGroupPermission(group, permission);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_NG_SET_PERM.format(TagResolver.builder()
            .tag("key", Messages.formatKey(group.getKey()))
            .resolver(Placeholder.component("name", group.getDisplayName()))
            .resolver(Placeholder.component("old-value", Messages.formatPermission(oldValue)))
            .resolver(
                Placeholder.component("value", Messages.formatPermission(group.getPermission())))
            .build()), sender);
  }

  private void setGroupNavigable(CommandSender sender, NodeGroup group, boolean value) {

    boolean oldValue = group.isNavigable();
    NodeGroupHandler.getInstance().setNodeGroupNavigable(group, value);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_NG_SET_NAVIGABLE.format(TagResolver.builder()
            .tag("key", Messages.formatKey(group.getKey()))
            .resolver(Placeholder.component("name", group.getDisplayName()))
            .resolver(Placeholder.component("old-value", Messages.formatBool(oldValue)))
            .resolver(Placeholder.component("value", Messages.formatBool(group.isNavigable())))
            .build()), sender);
  }

  private void setGroupDiscoverable(CommandSender sender, NodeGroup group, boolean value) {

    boolean oldValue = group.isDiscoverable();
    NodeGroupHandler.getInstance().setNodeGroupDiscoverable(group, value);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_NG_SET_DISCOVERABLE.format(TagResolver.builder()
            .tag("key", Messages.formatKey(group.getKey()))
            .resolver(Placeholder.component("name", group.getDisplayName()))
            .resolver(Placeholder.component("old-value",
                Messages.formatBool(oldValue).asComponent(sender)))
            .resolver(Placeholder.component("value",
                Messages.formatBool(group.isDiscoverable()).asComponent(sender)))
            .build()), sender);
  }

  private void setGroupDiscoverDist(CommandSender sender, NodeGroup group, float value) {

    float oldValue = group.getFindDistance();
    NodeGroupHandler.getInstance().setNodeGroupFindDistance(group, value);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_NG_SET_FIND_DIST.format(TagResolver.builder()
            .tag("key", Messages.formatKey(group.getKey()))
            .resolver(Placeholder.component("name", group.getDisplayName()))
            .resolver(Placeholder.component("old-value", Component.text(oldValue)))
            .resolver(Placeholder.component("value", Component.text(group.getFindDistance())))
            .build()), sender);
  }

  private void deleteGroup(CommandSender sender, NodeGroup group) {

    NodeGroupHandler.getInstance().deleteNodeGroup(group);
    TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(
        TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), sender);
  }

  private void createGroup(CommandSender sender, NamespacedKey key) {

    try {
      NodeGroup group = NodeGroupHandler.getInstance().createNodeGroup(key);
      TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(
          TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), sender);
    } catch (IllegalArgumentException e) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS
              .format(TagResolver.resolver("name", Tag.inserting(Component.text(key.toString())))),
          sender);
    } catch (Exception e) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE_FAIL, sender);
      e.printStackTrace();
    }
  }

  /**
   * Lists NodeGroups as list section.
   *
   * @param page first page is 1, not 0!
   */
  private void listGroups(CommandSender sender, int page) {

    CommandUtils.printList(
        sender,
        page,
        10,
        new ArrayList<>(NodeGroupHandler.getInstance().getNodeGroups()),
        group -> {
          TagResolver r = TagResolver.builder()
              .resolver(Placeholder.component("key", Component.text(group.getKey().toString())))
              .resolver(Placeholder.component("name", group.getDisplayName()))
              .resolver(Placeholder.component("size", Component.text(group.size())))
              .resolver(Placeholder.component("findable", Component.text(group.isDiscoverable())))
              .build();
          TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_LINE.format(r), sender);
        },
        Messages.CMD_NG_LIST_HEADER,
        Messages.CMD_NG_LIST_FOOTER);
  }
}
