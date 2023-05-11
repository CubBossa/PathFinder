package de.cubbossa.pathfinder.editmode.menu;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.MenuPresets;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.editmode.DefaultNodeGroupEditor;
import de.cubbossa.pathfinder.editmode.renderer.EdgeArmorStandRenderer;
import de.cubbossa.pathfinder.editmode.renderer.NodeArmorStandRenderer;
import de.cubbossa.pathfinder.editmode.utils.ItemStackUtils;
import de.cubbossa.pathfinder.nodegroup.modifier.DiscoverableModifier;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.LocalizedItem;
import de.cubbossa.pathfinder.util.VectorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EditModeMenu {

  private final PathFinder pathFinder;
  private final NamespacedKey key;
  private final Collection<NamespacedKey> multiTool = new HashSet<>();
  private final Collection<NodeType<?>> types;
  private UUID edgeStart = null;
  private Boolean undirectedEdges = false;

  public EditModeMenu(PathFinder pathFinder, NamespacedKey group,
                      Collection<NodeType<?>> types) {
    this.pathFinder = pathFinder;
    this.key = group;
    this.types = types;
  }

  public BottomInventoryMenu createHotbarMenu(DefaultNodeGroupEditor editor, Player editingPlayer) {
    BottomInventoryMenu menu = new BottomInventoryMenu(0, 1, 2, 3, 4, 5);

    menu.setDefaultClickHandler(Action.HOTBAR_DROP, c -> {
      Bukkit.getScheduler().runTaskLater(PathFinderPlugin.getInstance(),
          () -> editor.setEditMode(BukkitUtils.wrap(c.getPlayer()), false), 1L);
    });

    menu.setButton(0, Button.builder()
        .withItemStack(new LocalizedItem(Material.NETHER_STAR, Messages.E_NODE_TOOL_N,
            Messages.E_NODE_TOOL_L).createItem(editingPlayer))
        .withClickHandler(NodeArmorStandRenderer.LEFT_CLICK_NODE, context -> {
          Player p = context.getPlayer();
          pathFinder.getStorage().deleteNodesById(List.of(context.getTarget().getNodeId()))
              .thenRun(() -> p.playSound(p.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1, 1));
        })
        .withClickHandler(Action.RIGHT_CLICK_BLOCK, context -> {
          Location pos = context.getTarget().getLocation().clone().add(new Vector(0.5, 1.5, 0.5));

          if (types.size() <= 1) {
            NodeType<?> type = types.stream().findAny().orElse(null);
            if (type == null) {
              throw new IllegalStateException("Could not find any node type to generate node.");
            }
            pathFinder.getStorage().createAndLoadNode(type, VectorUtils.toInternal(pos))
                .thenAccept(node -> {
                  if (!(node instanceof Groupable groupable)) {
                    return;
                  }
                  groupable.addGroup(pathFinder.getStorage().loadGroup(key).join().orElseThrow());
                  pathFinder.getStorage().saveNode(node);
                }).exceptionally(throwable -> {
                  throwable.printStackTrace();
                  return null;
                });
          } else {
            openNodeTypeMenu(context.getPlayer(), pos);
          }
        }));


    menu.setButton(1, Button.builder()
        .withItemStack(() -> {
          ItemStack stack = new LocalizedItem(Material.STICK, Messages.E_EDGE_TOOL_N,
              Messages.E_EDGE_TOOL_L).createItem(editingPlayer);
          if (edgeStart != null) {
            ItemStackUtils.setGlow(stack);
          }
          return stack;
        })
        .withClickHandler(NodeArmorStandRenderer.RIGHT_CLICK_NODE, c -> {
          Player p = c.getPlayer();

          if (edgeStart == null) {
            edgeStart = c.getTarget().getNodeId();
            c.getMenu().refresh(c.getSlot());
            return;
          }
          if (edgeStart.equals(c.getTarget().getNodeId())) {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
          }
          Collection<CompletableFuture<?>> futures = new HashSet<>();
          futures.add(pathFinder.getStorage().modifyNode(edgeStart, node -> {
            node.connect(c.getTarget().getNodeId());
          }));
          if (undirectedEdges) {
            futures.add(pathFinder.getStorage().modifyNode(c.getTarget().getNodeId(), node -> {
              node.connect(edgeStart);
            }));
          }
          CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            edgeStart = null;
            c.getMenu().refresh(c.getSlot());
          }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
          });
        })
        .withClickHandler(Action.LEFT_CLICK_AIR, context -> {
          Player player = context.getPlayer();

          // switch mode
          if (edgeStart == null) {
            undirectedEdges = !undirectedEdges;
            BukkitUtils.wrap(player).sendMessage(Messages.E_EDGE_TOOL_DIR_TOGGLE
                .formatted(TagResolver.resolver("value",
                    Tag.inserting(Messages.formatBool(!undirectedEdges)))));
            return;
          }
          // cancel creation
          edgeStart = null;
          BukkitUtils.wrap(player).sendMessage(Messages.E_EDGE_TOOL_CANCELLED);
          context.getMenu().refresh(context.getSlot());

        })
        .withClickHandler(EdgeArmorStandRenderer.LEFT_CLICK_EDGE, context -> {
          pathFinder.getStorage().modifyNode(context.getTarget().getStart(), node -> {
            node.disconnect(context.getTarget().getEnd());
          });
        })
        .withClickHandler(NodeArmorStandRenderer.LEFT_CLICK_NODE, context -> {
          Player player = context.getPlayer();
          pathFinder.getStorage().modifyNode(context.getTarget().getNodeId(), n -> {
            n.disconnectAll();
          });
        }));


    menu.setButton(5, Button.builder()
        .withItemStack(new LocalizedItem(Material.ENDER_PEARL, Messages.E_TP_TOOL_N,
            Messages.E_TP_TOOL_L).createItem(editingPlayer))
        .withClickHandler(context -> {
          pathFinder.getStorage().loadNodes().thenAccept(nodes -> {

            double dist = -1;
            Node nearest = null;
            Location pLoc = context.getPlayer().getLocation();
            for (Node node : nodes) {
              double d = node.getLocation().distance(VectorUtils.toInternal(pLoc));
              if (dist == -1 || d < dist) {
                nearest = node;
                dist = d;
              }
            }
            if (nearest == null) {
              return;
            }
            Player p = context.getPlayer();
            Location newLoc = VectorUtils.toBukkit(nearest.getLocation())
                .setDirection(p.getLocation().getDirection());
            PathFinderProvider.get().runSynchronized(() -> {
              p.teleport(newLoc);
              p.playSound(newLoc, Sound.ENTITY_FOX_TELEPORT, 1, 1);
            });
          }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
          });
        }, Action.RIGHT_CLICK_ENTITY, Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR));

    menu.setButton(3, Button.builder()
        .withItemStack(new LocalizedItem(Material.CHEST, Messages.E_GROUP_TOOL_N,
            Messages.E_GROUP_TOOL_L).createItem(editingPlayer))
        .withClickHandler(NodeArmorStandRenderer.RIGHT_CLICK_NODE, context -> {
          pathFinder.getStorage().loadNode(context.getTarget().getNodeId()).thenAccept(node -> {
            if (node.isPresent() && node.get() instanceof Groupable groupable) {
              openGroupMenu(context.getPlayer(), groupable);
            }
          });
        })
        .withClickHandler(NodeArmorStandRenderer.LEFT_CLICK_NODE, context -> {
          pathFinder.getStorage().modifyNode(context.getTarget().getNodeId(), node -> {
            if (!(node instanceof Groupable groupable)) {
              return;
            }
            if (groupable.getGroups().isEmpty()) {
              return;
            }
            groupable.clearGroups();
            context.getPlayer().playSound(context.getPlayer().getLocation(),
                Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1, 1);
          });
        }));

    menu.setButton(4, Button.builder()
        .withItemStack(new LocalizedItem(Material.ENDER_CHEST, Messages.E_MULTI_GROUP_TOOL_N,
            Messages.E_MULTI_GROUP_TOOL_L).createItem(editingPlayer))
        .withClickHandler(NodeArmorStandRenderer.RIGHT_CLICK_NODE, context -> {
          pathFinder.getStorage().modifyNode(context.getTarget().getNodeId(), node -> {
            if (!(node instanceof Groupable groupable)) {
              return;
            }
            pathFinder.getStorage().loadGroups(multiTool).thenAccept(groups -> {
              groups.forEach(groupable::addGroup);
            });
            context.getPlayer().playSound(context.getPlayer().getLocation(),
                Sound.BLOCK_CHEST_CLOSE, 1, 1);
          });
        })
        .withClickHandler(NodeArmorStandRenderer.LEFT_CLICK_NODE, context -> {
          pathFinder.getStorage().modifyNode(context.getTarget().getNodeId(), node -> {
            if (!(node instanceof Groupable groupable)) {
              return;
            }
            multiTool.forEach(groupable::removeGroup);
            context.getPlayer().playSound(context.getPlayer().getLocation(),
                Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1, 1);
          });
        })
        .withClickHandler(Action.RIGHT_CLICK_AIR,
            context -> openMultiToolMenu(context.getPlayer())));
    return menu;
  }

  private void openGroupMenu(Player player, Groupable groupable) {

    pathFinder.getStorage().loadAllGroups().thenAccept(nodeGroups -> {
      System.out.println("Loaded groups, thread: " + Thread.currentThread().getName());

      ListMenu menu = new ListMenu(Messages.E_SUB_GROUP_TITLE.asComponent(BukkitPathFinder.getInstance().getAudiences().player(player.getUniqueId())), 4);
      menu.addPreset(MenuPresets.fillRow(new ItemStack(Material.BLACK_STAINED_GLASS_PANE),
          3)); //TODO extract icon
      for (NodeGroup group : nodeGroups) {

        TagResolver resolver = TagResolver.builder()
            .tag("key", Messages.formatKey(group.getKey()))
            .build();

        menu.addListEntry(Button.builder()
            .withItemStack(() -> {
              ItemStack stack = new LocalizedItem.Builder(new ItemStack(
                  group.hasModifier(DiscoverableModifier.class) ? Material.CHEST_MINECART
                      : Material.FURNACE_MINECART))
                  .withName(Messages.E_SUB_GROUP_ENTRY_N.formatted(resolver))
                  .withLore(Messages.E_SUB_GROUP_ENTRY_L.formatted(resolver))
                  .createItem(player);
              if (group.contains(groupable.getNodeId())) {
                stack = ItemStackUtils.setGlow(stack);
              }
              return stack;
            })
            .withClickHandler(Action.LEFT, c -> {
              System.out.println("Handle click, thread: " + Thread.currentThread().getName());
              if (!group.contains(groupable.getNodeId())) {
                groupable.addGroup(group);
                pathFinder.getStorage().saveNode(groupable).thenRun(() -> {
                  System.out.println("After save, thread: " + Thread.currentThread().getName());
                  c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                });
                menu.refresh(menu.getListSlots());
              }
            })
            .withClickHandler(Action.RIGHT, c -> {
              if (group.contains(groupable.getNodeId())) {
                groupable.removeGroup(group.getKey());
                pathFinder.getStorage().saveNode(groupable).join();

                c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
                menu.refresh(menu.getListSlots());
              }
            }));
      }
      menu.addPreset(presetApplier -> {
        presetApplier.addItemOnTop(3 * 9 + 8,
            new LocalizedItem(Material.BARRIER, Messages.E_SUB_GROUP_RESET_N,
                Messages.E_SUB_GROUP_RESET_L).createItem(player));
        presetApplier.addClickHandlerOnTop(3 * 9 + 8, Action.LEFT, c -> {
          groupable.clearGroups();
          pathFinder.getStorage().saveNode(groupable).join();
          menu.refresh(menu.getListSlots());
          c.getPlayer()
              .playSound(c.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f,
                  1f);
        });

        presetApplier.addItemOnTop(3 * 9 + 4,
            new LocalizedItem(Material.PAPER, Messages.E_SUB_GROUP_INFO_N,
                Messages.E_SUB_GROUP_INFO_L).createItem(player));
      });
      menu.open(player);
    });
  }

  private void openMultiToolMenu(Player player) {
    pathFinder.getStorage().loadAllGroups().thenAccept(nodeGroups -> {

      ListMenu menu = new ListMenu(Messages.E_SUB_GROUP_TITLE.asComponent(BukkitPathFinder.getInstance().getAudiences().player(player.getUniqueId())), 4);
      menu.addPreset(MenuPresets.fillRow(new ItemStack(Material.BLACK_STAINED_GLASS_PANE),
          3)); //TODO extract icon
      for (NodeGroup group : nodeGroups) {

        TagResolver resolver = TagResolver.builder()
            .tag("key", Messages.formatKey(group.getKey()))
            .build();

        menu.addListEntry(Button.builder()
            .withItemStack(() -> {
              ItemStack stack = new LocalizedItem.Builder(new ItemStack(
                  group.hasModifier(DiscoverableModifier.class) ? Material.CHEST_MINECART
                      : Material.FURNACE_MINECART))
                  .withName(Messages.E_SUB_GROUP_ENTRY_N.formatted(resolver))
                  .withLore(Messages.E_SUB_GROUP_ENTRY_L.formatted(resolver))
                  .createItem(player);
              if (multiTool.contains(group.getKey())) {
                stack = ItemStackUtils.setGlow(stack);
              }
              return stack;
            })
            .withClickHandler(Action.LEFT, c -> {
              if (!multiTool.contains(group.getKey())) {

                Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () -> {
                  multiTool.add(group.getKey());
                  c.getPlayer()
                      .playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                  menu.refresh(menu.getListSlots());
                });
              }
            })
            .withClickHandler(Action.RIGHT, c -> {
              if (multiTool.contains(group.getKey())) {

                Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () -> {
                  multiTool.remove(group.getKey());
                  c.getPlayer()
                      .playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
                  menu.refresh(menu.getListSlots());
                });
              }
            }));
      }
      menu.addPreset(presetApplier -> {
        presetApplier.addItemOnTop(3 * 9 + 8,
            new LocalizedItem(Material.BARRIER, Messages.E_SUB_GROUP_RESET_N,
                Messages.E_SUB_GROUP_RESET_L).createItem(player));
        presetApplier.addClickHandlerOnTop(3 * 9 + 8, Action.LEFT, c -> {
          multiTool.clear();
          menu.refresh(menu.getListSlots());
          c.getPlayer()
              .playSound(c.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f,
                  1f);
        });

        presetApplier.addItemOnTop(3 * 9 + 4,
            new LocalizedItem(Material.PAPER, Messages.E_SUB_GROUP_INFO_N,
                Messages.E_SUB_GROUP_INFO_L).createItem(player));
      });
      menu.open(player);
    });
  }

  private void openNodeTypeMenu(Player player, Location location) {

    ListMenu menu = new ListMenu(Component.text("Node-Gruppen verwalten:"), 2);
    for (NodeType<?> type : types) {

      menu.addListEntry(Button.builder()
          .withItemStack(() -> new ItemStack(Material.CYAN_CONCRETE_POWDER))
          .withClickHandler(Action.RIGHT, c -> {
            pathFinder.getStorage().createAndLoadNode(type, VectorUtils.toInternal(location));
            menu.close(player);
          }));
    }
    menu.open(player);
  }
}
