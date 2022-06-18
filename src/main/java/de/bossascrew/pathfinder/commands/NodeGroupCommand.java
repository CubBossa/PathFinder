package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.cubbossa.translations.TranslationHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@CommandAlias("nodegroup ")
public class NodeGroupCommand extends BaseCommand {

    @Subcommand("list")
    @Syntax("[<page>]")
    @CommandPermission("pathfinder.command.nodegroup.list")
    public void onList(Player player, @Optional Integer pageInput) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        TagResolver resolver = TagResolver.builder()
                .tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
                .tag("page", Tag.preProcessParsed(pageInput + ""))
                .build();

        TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_HEADER.format(resolver), player);

        for (NodeGroup group : CommandUtils.subList(new ArrayList<>(roadMap.getGroups().values()), pageInput, 10)) {

            TagResolver r = TagResolver.builder()
                    .tag("key", Tag.inserting(Component.text(group.getKey().toString())))
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
    public void onCreate(Player player, NamespacedKey key) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        if (roadMap.getNodeGroup(key) != null) {
            TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS
                    .format(TagResolver.resolver("name", Tag.inserting(Component.text(key.toString())))), player);
            return;
        }

        NodeGroup group = roadMap.createNodeGroup(key, true);
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

        TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_LIST.format(TagResolver.builder()
                .tag("name", Tag.inserting(group.getDisplayName()))
                .tag("value", Tag.inserting(Component.text(findable)))
                .build()), player);
    }

    @Subcommand("search-terms")
    public class Search extends BaseCommand {

        @Subcommand("list")
        @Syntax("<group>")
        @CommandPermission("pathfinder.command.nodegroup.searchterms.list")
        @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
        public void onTermList(Player player, NodeGroup group) {

            TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_FINDABLE.format(TagResolver.builder()
                    .tag("name", Tag.inserting(group.getDisplayName()))
                    .tag("values", Tag.inserting(toList(group.getSearchTerms())))
                    .build()), player);
        }

        @Subcommand("add")
        @Syntax("<group> {<term>,}<term>")
        @CommandPermission("pathfinder.command.nodegroup.searchterms.add")
        @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
        public void onTermAdd(Player player, NodeGroup group, String terms) {

            Collection<String> toAdd = Arrays.stream(terms.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();
            group.getSearchTerms().addAll(toAdd);

            TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_ADD.format(TagResolver.builder()
                    .tag("name", Tag.inserting(group.getDisplayName()))
                    .tag("values", Tag.inserting(toList(toAdd)))
                    .build()), player);
        }

        @Subcommand("remove")
        @Syntax("<group> {<term>,}<term>")
        @CommandPermission("pathfinder.command.nodegroup.searchterms.remove")
        @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
        public void onTermRemove(Player player, NodeGroup group, String terms) {

            Collection<String> toRemove = Arrays.stream(terms.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();
            group.getSearchTerms().removeAll(toRemove);

            TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_REMOVE.format(TagResolver.builder()
                    .tag("name", Tag.inserting(group.getDisplayName()))
                    .tag("values", Tag.inserting(toList(toRemove)))
                    .build()), player);
        }

        private Component toList(Collection<String> tags) {
            return Component.join(JoinConfiguration.separator(Component.text(",", NamedTextColor.GRAY)), tags.stream()
                    .map(Component::text).collect(Collectors.toList()));
        }
    }
}
