package de.cubbossa.pathfinder.listener;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.*;
import de.cubbossa.pathapi.group.DiscoverProgressModifier;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.PathFinderConf;
import de.cubbossa.tinytranslations.util.FormattableBuilder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class BukkitEffects {

  @Setter
  @Getter
  private MiniMessage miniMessage;
  private final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

  public BukkitEffects(EventDispatcher<Player> dispatcher, PathFinderConf.EffectsConf config) {

    miniMessage = MiniMessage.miniMessage();

    dispatcher.listen(PathStartEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathStart, FormattableBuilder.builder()
          .insertObject("player", e.getPath().getTargetViewer())
          .toResolver()
      );
    });

    dispatcher.listen(PathTargetReachedEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathTargetReach, FormattableBuilder.builder()
          .insertObject("player", e.getPath().getTargetViewer())
          .toResolver()
      );
    });

    dispatcher.listen(PathCancelledEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathCancel, FormattableBuilder.builder()
          .insertObject("player", e.getPath().getTargetViewer())
          .toResolver()
      );
    });

    dispatcher.listen(PathStoppedEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathStop, FormattableBuilder.builder()
          .insertObject("player", e.getPath().getTargetViewer())
          .toResolver()
      );
    });

    dispatcher.listen(PlayerDiscoverLocationEvent.class, e -> {
      runCommands(e.getPlayer(), config.onDiscover, FormattableBuilder.builder()
          .insertObject("player", e.getPlayer())
          .insertObject("discoverable", e.getPlayer().getDisplayName())
          .insertObject("group", e.getGroup())
          .toResolver()
      );
    });

    dispatcher.listen(PlayerDiscoverProgressEvent.class, e -> {

      DiscoverableModifier discoverableModifier = e.getFoundGroup().<DiscoverableModifier>getModifier(DiscoverableModifier.KEY)
          .orElseThrow();
      DiscoverProgressModifier discoverProgressModifier = e.getProgressObserverGroup().<DiscoverProgressModifier>getModifier(DiscoverProgressModifier.KEY)
          .orElseThrow();

      runCommands(e.getPlayer(), config.onDiscoverProgress, //"player", "discoverable", "group", "name", "percent", "ratio", "count-found", "count-all"
          Placeholder.component("player", e.getPlayer().getDisplayName()),
          Placeholder.component("discoverable", discoverableModifier.getDisplayName()),
          Placeholder.parsed("group", e.getProgressObserverGroup().getKey().toString()),
          Placeholder.component("name", discoverProgressModifier.getDisplayName()),
          Formatter.number("percentage", discoverProgressModifier.calculateProgress(e.getPlayer().getUniqueId()).join() * 100)
      );
    });

    dispatcher.listen(PlayerForgetLocationEvent.class, e -> {
      runCommands(e.getPlayer(), config.onForget,
          Placeholder.component("player", e.getPlayer().getDisplayName()),
          Placeholder.component("discoverable", e.getModifier().getDisplayName()),
          Placeholder.parsed("group", e.getGroup().getKey().toString())
      );
    });
  }

  private String prepareCmd(String cmd, PathPlayer<Player> player, TagResolver... msgResolvers) {
    return serializer.serialize(PathFinderProvider.get().getTranslations().translate(cmd, Locale.ENGLISH, msgResolvers));
  }

  private void runCommands(PathPlayer<Player> player, List<String> commands, TagResolver... msgResolvers) {
    if (commands == null) {
      return;
    }
    commands.stream()
        .map(s -> this.prepareCmd(s, player, msgResolvers))
        .peek(System.out::println)
        .forEach(r -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), r));
  }
}
