//package de.cubbossa.pathfinder.command.util;
//
//import de.cubbossa.pathfinder.command.Command;
//import de.cubbossa.pathfinder.command.CommandArgument;
//import de.cubbossa.pathfinder.command.CustomLiteralArgument;
//import dev.jorel.commandapi.CommandTree;
//import dev.jorel.commandapi.arguments.BukkitNodeSelectionArgument;
//import dev.jorel.commandapi.arguments.LiteralArgument;
//import dev.jorel.commandapi.arguments.MultiLiteralArgument;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Queue;
//import java.util.SortedMap;
//import java.util.Stack;
//import java.util.TreeMap;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.ComponentLike;
//import net.kyori.adventure.text.event.ClickEvent;
//import net.kyori.adventure.text.format.NamedTextColor;
//import net.kyori.adventure.text.format.TextColor;
//import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
//
//public class CommandHelpGenerator {
//
//  private static final TextColor[] ARG_COLORS = {
//      NamedTextColor.AQUA,
//      NamedTextColor.YELLOW,
//      NamedTextColor.GREEN,
//      NamedTextColor.LIGHT_PURPLE
//  };
//  private static PlainTextComponentSerializer TO_PLAIN = PlainTextComponentSerializer.builder()
//      .build();
//
//  public List<ComponentLike> format(BukkitNodeSelectionArgument<?> tree) {
//    return format(tree, -1);
//  }
//
//  public List<ComponentLike> format(BukkitNodeSelectionArgument<?> tree, int depth) {
//
//    SortedMap<String, ComponentLike> components = new TreeMap<>();
//    List<BukkitNodeSelectionArgument<?>> leaves = findLeaves(tree);
//
//    for (BukkitNodeSelectionArgument<?> leaf : leaves) {
//      Stack<BukkitNodeSelectionArgument<?>> stack = new Stack<>();
//      stack.push(leaf);
//      BukkitNodeSelectionArgument<?> el = leaf;
//      while (el.getParent() != null) {
//        stack.push(el.getParent());
//        el = el.getParent();
//      }
//
//      Component cmd =
//              Component.text("/" + ((CommandTree) stack.pop()).getName(), NamedTextColor.GRAY);
//
//      int index = 0;
//
//      while (!stack.isEmpty()) {
//        BukkitNodeSelectionArgument<?> arg = stack.pop();
//        cmd = cmd
//            .append(Component.text(" "))
//            .append(formatArgument(arg, index));
//        if (arg instanceof ArgumentTree t
//            && !(t.getArgument() instanceof LiteralArgument
//            || t.getArgument() instanceof MultiLiteralArgument)) {
//          index = (index + 1) % ARG_COLORS.length;
//        }
//      }
//
//      String wiki = getWiki(leaf);
//      if (wiki != null) {
//        cmd = cmd.append(Component.text(" (i)", NamedTextColor.GRAY))
//                .hoverEvent(Component.text("Open WIKI"))
//                .clickEvent(ClickEvent.openUrl(wiki));
//      }
//
//      components.put(TO_PLAIN.serialize(cmd), cmd);
//    }
//    return new ArrayList<>(components.values());
//  }
//
//  private String getShortDesc(BukkitNodeSelectionArgument<?> tree) {
//    BukkitNodeSelectionArgument<?> current = tree;
//    while (current != null) {
//      if (current instanceof CommandArgument<?, ?> arg && arg.getDescription() != null) {
//        return arg.getDescription();
//      } else if (current instanceof CustomLiteralArgument arg && arg.getDescription() != null) {
//        return arg.getDescription();
//      } else if (current instanceof CommandTree cmd && cmd.getShortDescription() != null) {
//        return cmd.getShortDescription();
//      }
//      current = current.getParent();
//    }
//    return null;
//  }
//
//  private String getWiki(BukkitNodeSelectionArgument<?> tree) {
//    BukkitNodeSelectionArgument<?> current = tree;
//    while (current != null) {
//      if (current instanceof CommandArgument<?, ?> arg && arg.getWiki() != null) {
//        return arg.getWiki();
//      } else if (current instanceof CustomLiteralArgument arg && arg.getWiki() != null) {
//        return arg.getWiki();
//      }
//      current = current.getParent();
//    }
//    return null;
//  }
//
//  private boolean executable(BukkitNodeSelectionArgument<?> tree) {
//    BukkitNodeSelectionArgument<?> current = tree;
//    while (current != null) {
//      if (!(current instanceof LiteralArgument || current instanceof Command)) {
//        return false;
//      }
//      current = current.getgetParent();
//    }
//    return true;
//  }
//
//  private Component formatArgument(BukkitNodeSelectionArgument<?> tree, int index) {
//    Component c = null;
//
//    if (tree instanceof ArgumentTree arg) {
//      BukkitNodeSelectionArgument<?> o = arg.getArgument();
//      String s = o.getNodeName();
//      if (o instanceof LiteralArgument l) {
//        c = Component.text(l.getLiteral());
//      } else if (o instanceof MultiLiteralArgument ml) {
//        c = Component.text(String.join("|", ml.getLiterals()));
//      } else {
//        c = Component.text("‹" + s + "›");
//        if (tree instanceof CommandArgument<?, ?> cmdArg && cmdArg.isOptional()) {
//          c = Component.text('[')
//                  .append(c)
//                  .append(Component.text(']'));
//        }
//        c = c.color(ARG_COLORS[index % 4]);
//      }
//    }
//    if (c == null) {
//      throw new IllegalStateException("Invalid argument, cannot format.");
//    }
//
//    String shortDesc = getShortDesc(tree);
//    if (shortDesc != null) {
//      c = c.hoverEvent(Component.empty()
//          .append(c)
//          .appendNewline()
//          .append(Component.text(shortDesc, NamedTextColor.WHITE))
//          .appendNewline().appendNewline()
//          .append(Component.text("Click to execute command"))
//      );
//    }
//    return c;
//  }
//
//  private List<BukkitNodeSelectionArgument<?>> findLeaves(BukkitNodeSelectionArgument<?> tree) {
//    if (tree.getArguments().size() == 0) {
//      return List.of(tree);
//    }
//
//    Queue<BukkitNodeSelectionArgument<?>> toProcess = new LinkedList<>();
//    List<BukkitNodeSelectionArgument<?>> childless = new ArrayList<>();
//    toProcess.add(tree);
//
//    while (!toProcess.isEmpty()) {
//      BukkitNodeSelectionArgument<?> first = toProcess.poll();
//      if (first.getArguments().size() == 0) {
//        // no children
//        childless.add(first);
//      } else {
//        // has children
//        toProcess.addAll(first.getArguments());
//      }
//    }
//    return childless;
//  }
//}
