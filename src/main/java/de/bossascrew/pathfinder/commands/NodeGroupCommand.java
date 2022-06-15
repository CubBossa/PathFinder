package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.NodeGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.cubbossa.translations.TranslationHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandAlias("nodegroup|ng|findablegroup")
public class NodeGroupCommand extends BaseCommand {

    @Subcommand("list")
    @Syntax("[<page>]")
    @CommandPermission("pathfinder.command.nodegroup.list")
    public void onList(Player player, @Optional Integer pageInput) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        TagResolver resolver = TagResolver.builder()
                .tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
                .tag("page", Tag.inserting(Component.text(pageInput)))
                .build();

        TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_HEADER.format(resolver), player);

        for (NodeGroup group : new ArrayList<>(roadMap.getGroups().values()).subList(pageInput * 10, (pageInput + 1) * 10)) {

            TagResolver r = TagResolver.builder()
                    .tag("id", Tag.inserting(Component.text(group.getGroupId())))
                    .tag("name", Tag.inserting(group.getDisplayName()))
                    .tag("size", Tag.inserting(Component.text(group.size())))
                    .tag("findable", Tag.inserting(Component.text(group.isFindable())))
                    .build();
            TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_LINE.format(resolver, r), player);
        }
        TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_FOOTER.format(resolver), player);
    }

    @Subcommand("create")
    @Syntax("<name>")
    @CommandPermission("pathfinder.command.nodegroup.create")
    public void onCreate(Player player, String name) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        NodeGroup group = roadMap.createNodeGroup(name, true);
        TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), player);
    }

    @Subcommand("delete")
    @Syntax("<group>")
    @CommandPermission("pathfinder.command.nodegroup.delete")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
    public void onDelete(Player player, NodeGroup group) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        roadMap.removeNodeGroup(group);
        TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), player);
    }

    @Subcommand("set name")
    @Syntax("<group> <new name>")
    @CommandPermission("pathfinder.command.nodegroup.rename")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
    public void onRename(Player player, NodeGroup group, @Single String newName) {
        group.setNameFormat(newName);

        TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_NAME.format(TagResolver.builder()
                .tag("name", Tag.inserting(group.getDisplayName()))
                .tag("value", Tag.inserting(PathPlugin.getInstance().getMiniMessage().deserialize(newName)))
                .build()), player);
    }

    @Subcommand("set findable")
    @Syntax("<group> <findable>")
    @CommandPermission("pathfinder.command.nodegroup.setfindable")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION + " true|false")
    public void onSetFindable(Player player, NodeGroup group, boolean findable) {
        group.setFindable(findable);

        TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_FINDABLE.format(TagResolver.builder()
                .tag("name", Tag.inserting(group.getDisplayName()))
                .tag("value", Tag.inserting(Component.text(findable)))
                .build()), player);
    }
}
