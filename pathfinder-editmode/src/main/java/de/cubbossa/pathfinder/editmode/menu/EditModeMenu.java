package de.cubbossa.pathfinder.editmode.menu;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.MenuPresets;
import de.cubbossa.menuframework.inventory.context.ClickContext;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import de.cubbossa.pathapi.PathFinderConfig;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.NodeDeleteEvent;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.editmode.DefaultNodeGroupEditor;
import de.cubbossa.pathfinder.editmode.utils.ItemStackUtils;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.util.LocalizedItem;
import de.cubbossa.pathfinder.util.VectorUtils;
import de.cubbossa.tinytranslations.util.FormattableBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditModeMenu {


  private static final Material[] GROUP_ITEM_LIST = new Material[]{
      Material.WHITE_CONCRETE,
      Material.ORANGE_CONCRETE,
      Material.MAGENTA_CONCRETE,
      Material.LIGHT_BLUE_CONCRETE,
      Material.YELLOW_CONCRETE,
      Material.LIME_CONCRETE,
      Material.PINK_CONCRETE,
      Material.GRAY_CONCRETE,
      Material.LIGHT_GRAY_CONCRETE,
      Material.CYAN_CONCRETE,
      Material.PURPLE_CONCRETE,
      Material.BLUE_CONCRETE,
      Material.BROWN_CONCRETE,
      Material.GREEN_CONCRETE,
      Material.RED_CONCRETE,
      Material.BLACK_CONCRETE
  };


  public static final Action<TargetContext<Node>> RIGHT_CLICK_NODE = new Action<>();
  public static final Action<TargetContext<Node>> LEFT_CLICK_NODE = new Action<>();
  public static final Action<TargetContext<Edge>> RIGHT_CLICK_EDGE = new Action<>();
  public static final Action<TargetContext<Edge>> LEFT_CLICK_EDGE = new Action<>();

  private final Storage storage;
  private final NamespacedKey key;
  private final Collection<NamespacedKey> multiTool = new HashSet<>();
  private final Collection<NodeType<?>> types;
  private Boolean undirectedEdgesMode;
  private UUID chainEdgeStart = null;

  private final AtomicBoolean lock = new AtomicBoolean();

  public EditModeMenu(Storage storage, NamespacedKey group, Collection<NodeType<?>> types, PathFinderConfig.EditModeConfig config) {
    this.storage = storage;
    this.key = group;
    this.types = types;
    this.undirectedEdgesMode = !config.isDirectedEdgesByDefault();

    PathFinderProvider.get().getEventDispatcher().listen(NodeDeleteEvent.class, e -> {
      if (Objects.equals(chainEdgeStart, e.getNode().getNodeId())) {
        chainEdgeStart = null;
      }
    });
  }

  public BottomInventoryMenu createHotbarMenu(DefaultNodeGroupEditor editor, Player editingPlayer) {
    BottomInventoryMenu menu = new BottomInventoryMenu(0, 1, 2, 3, 4);

    menu.setDefaultClickHandler(Action.HOTBAR_DROP, c -> {
      Bukkit.getScheduler().runTaskLater(PathFinderPlugin.getInstance(),
          () -> editor.setEditMode(BukkitUtils.wrap(c.getPlayer()), false), 1L);
    });

    menu.setButton(4, Button.builder()
        .withItemStack(() -> new LocalizedItem.Builder(new ItemStack(undirectedEdgesMode ? Material.RED_DYE : Material.LIGHT_BLUE_DYE))
            .withName(Messages.E_EDGEDIR_TOOL_N.insertBool("value", !undirectedEdgesMode))
            .withLore(Messages.E_EDGEDIR_TOOL_L)
            .createItem(editingPlayer))
        .withClickHandler(c -> {
          undirectedEdgesMode = !undirectedEdgesMode;
          Player player = c.getPlayer();

          BukkitUtils.wrap(player).sendMessage(Messages.E_NODE_TOOL_DIR_TOGGLE.insertBool("value", !undirectedEdgesMode));
          c.getMenu().refresh(c.getSlot());
        }, Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK));

    menu.setButton(0, Button.builder()
        .withItemStack(() -> {
          ItemStack stack = new ItemStack(Material.FIREWORK_STAR);
          FireworkEffectMeta meta = (FireworkEffectMeta) stack.getItemMeta();
          meta.addItemFlags(ItemFlag.values());
          meta.setEffect(FireworkEffect.builder()
              // green = no current chain, orange = chain started
              .withColor(Color.fromRGB(chainEdgeStart == null ? 0x00ff00 : 0xfc8a00))
              .build());
          stack.setItemMeta(meta);

          return new LocalizedItem(stack, Messages.E_NODE_TOOL_N, Messages.E_NODE_TOOL_L).createItem(editingPlayer);
        })

        .withClickHandler(LEFT_CLICK_NODE, context -> {
          Player p = context.getPlayer();
          if (!lock.compareAndSet(false, true)) {
            BukkitUtils.wrap(p).sendMessage(Messages.GEN_TOO_FAST);
            return;
          }
          storage.deleteNodes(Collections.singleton(context.getTarget().getNodeId()))
              .thenRun(() -> p.playSound(p.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1, 1))
              .whenComplete((unused, throwable) -> lock.set(false));
        })

        .withClickHandler(Action.LEFT_CLICK_AIR, context -> {
          PathPlayer<Player> p = CommonPathFinder.getInstance().wrap(context.getPlayer());
          // cancel chain
          if (chainEdgeStart == null) {
            return;
          }
          p.sendMessage(Messages.E_NODE_CHAIN_NEW);
          chainEdgeStart = null;
          context.getMenu().refresh(context.getSlot());
        })

        .withClickHandler(RIGHT_CLICK_NODE, context -> {
          Player p = context.getPlayer();
          PathPlayer<Player> pp = CommonPathFinder.getInstance().wrap(p);
          if (chainEdgeStart == null) {
            chainEdgeStart = context.getTarget().getNodeId();
            context.getMenu().refresh(context.getSlot());
            pp.sendMessage(Messages.E_NODE_CHAIN_START);
            return;
          }
          if (chainEdgeStart.equals(context.getTarget().getNodeId())) {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
          }
          if (!lock.compareAndSet(false, true)) {
            BukkitUtils.wrap(p).sendMessage(Messages.GEN_TOO_FAST);
            return;
          }
          Collection<CompletableFuture<?>> futures = new HashSet<>();
          futures.add(storage.modifyNode(chainEdgeStart, node -> {
            node.connect(context.getTarget().getNodeId());
          }));
          if (undirectedEdgesMode) {
            futures.add(storage.modifyNode(context.getTarget().getNodeId(), node -> {
              node.connect(chainEdgeStart);
            }));
          }
          CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            chainEdgeStart = null;
            context.getMenu().refresh(context.getSlot());
            CommonPathFinder.getInstance().wrap(context.getPlayer()).sendMessage(Messages.E_NODE_CHAIN_NEW);
          }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
              throwable.printStackTrace();
            }
            lock.set(false);
          });
        })

        .withClickHandler(Action.RIGHT_CLICK_BLOCK, context -> {

          if (!lock.compareAndSet(false, true)) {
            BukkitUtils.wrap(context.getPlayer()).sendMessage(Messages.GEN_TOO_FAST);
            return;
          }

          Location view = context.getPlayer().getEyeLocation();
          Location block = context.getTarget().getLocation();
          BukkitVectorUtils.Orientation orientation = BukkitVectorUtils.getIntersection(view.toVector(), view.getDirection(), block.toVector());
          if (orientation == null) {
            lock.set(false);
            return;
          }

          Location pos = BukkitVectorUtils.toBukkit(VectorUtils.snap(BukkitVectorUtils.toInternal(orientation.location()), 2))
              .toLocation(block.getWorld()).add(orientation.direction().clone().multiply(.5f));
          if (types.size() > 1) {
            openNodeTypeMenu(context.getPlayer(), pos);
            lock.set(false);
            return;
          }

          NodeType<?> type = types.stream().findAny().orElse(null);
          if (type == null) {
            lock.set(false);
            throw new IllegalStateException("Could not find any node type to generate node.");
          }
          storage
              .createAndLoadNode(type, BukkitVectorUtils.toInternal(pos))
              .thenCompose(node -> storage.modifyNode(node.getNodeId(), n -> {
                if (chainEdgeStart != null) {
                  storage.modifyNode(chainEdgeStart, o -> {
                    o.connect(node);
                  });
                  if (undirectedEdgesMode) {
                    n.connect(chainEdgeStart);
                  }
                }
                chainEdgeStart = n.getNodeId();
                storage.modifyGroup(key, group -> group.add(node.getNodeId()));
                storage.modifyGroup(CommonPathFinder.globalGroupKey(), group -> group.add(node.getNodeId()));
              }))
              .whenComplete((ex, throwable) -> {
                if (throwable != null) {
                  throwable.printStackTrace();
                }
                lock.set(false);
              });
        })

        .withClickHandler(LEFT_CLICK_EDGE, context -> {
          if (!lock.compareAndSet(false, true)) {
            BukkitUtils.wrap(context.getPlayer()).sendMessage(Messages.GEN_TOO_FAST);
            return;
          }
          storage.modifyNode(context.getTarget().getStart(), node -> {
            node.disconnect(context.getTarget().getEnd());
          }).whenComplete((x, throwable) -> {
            if (throwable != null) {
              throwable.printStackTrace();
            }
            lock.set(false);
          });
        })
    );

    menu.setButton(3, Button.builder()
        .withItemStack(new LocalizedItem(Material.ENDER_PEARL, Messages.E_TP_TOOL_N,
            Messages.E_TP_TOOL_L).createItem(editingPlayer))
        .withClickHandler(context -> {
          if (!lock.compareAndSet(false, true)) {
            BukkitUtils.wrap(context.getPlayer()).sendMessage(Messages.GEN_TOO_FAST);
            return;
          }
          storage.loadGroup(key)
              .thenCompose(group -> storage.loadNodes(group.map(g -> (Collection<UUID>) g).orElseGet(HashSet::new)))
              .thenAccept(nodes -> {
                Player p = context.getPlayer();
                if (nodes.size() == 0) {
                  // no nodes in the current editing
                  p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                  return;
                }

                double dist = -1;
                Node nearest = null;
                Location pLoc = context.getPlayer().getLocation();
                for (Node node : nodes) {
                  double d = node.getLocation().distance(BukkitVectorUtils.toInternal(pLoc));
                  if (dist == -1 || d < dist) {
                    nearest = node;
                    dist = d;
                  }
                }

                Location newLoc = BukkitVectorUtils.toBukkit(nearest.getLocation()).setDirection(p.getLocation().getDirection());
                Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () -> {
                  p.teleport(newLoc);
                  p.playSound(newLoc, Sound.ENTITY_FOX_TELEPORT, 1, 1);
                });
              }).whenComplete((ex, throwable) -> {
                if (throwable != null) {
                  throwable.printStackTrace();
                }
                lock.set(false);
              });
        }, Action.RIGHT_CLICK_ENTITY, Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR));

    menu.setButton(1, Button.builder()
        .withItemStack(new LocalizedItem(Material.CHEST, Messages.E_GROUP_TOOL_N,
            Messages.E_GROUP_TOOL_L).createItem(editingPlayer))
        .withClickHandler(RIGHT_CLICK_NODE, context -> {
          storage.loadNode(context.getTarget().getNodeId()).thenAccept(node -> {
            node.ifPresent(value -> openGroupMenu(context.getPlayer(), value));
          });
        })
        .withClickHandler(LEFT_CLICK_NODE, context -> {
          if (!lock.compareAndSet(false, true)) {
            BukkitUtils.wrap(context.getPlayer()).sendMessage(Messages.GEN_TOO_FAST);
            return;
          }
          try {
            StorageUtil.clearGroups(context.getTarget());
            context.getPlayer().playSound(context.getPlayer().getLocation(),
                Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1, 1);
          } finally {
            lock.set(false);
          }
        }));

    menu.setButton(2, Button.builder()
        .withItemStack(new LocalizedItem(Material.ENDER_CHEST, Messages.E_MULTI_GROUP_TOOL_N,
            Messages.E_MULTI_GROUP_TOOL_L).createItem(editingPlayer))
        .withClickHandler(RIGHT_CLICK_NODE, context -> {
          if (!lock.compareAndSet(false, true)) {
            BukkitUtils.wrap(context.getPlayer()).sendMessage(Messages.GEN_TOO_FAST);
            return;
          }
          storage.loadGroupsByMod(multiTool).thenCompose(groups -> {
            return StorageUtil.addGroups(groups, context.getTarget().getNodeId());
          }).thenRun(() -> {
            context.getPlayer().playSound(context.getPlayer().getLocation(),
                Sound.BLOCK_CHEST_CLOSE, 1, 1);
          }).whenComplete((ex, throwable) -> {
            if (throwable != null) {
              throwable.printStackTrace();
            }
            lock.set(false);
          });
        })
        .withClickHandler(LEFT_CLICK_NODE, context -> {
          storage.loadGroupsByMod(multiTool).thenCompose(groups -> {
            return StorageUtil.removeGroups(groups, context.getTarget().getNodeId());
          }).thenRun(() -> {
            context.getPlayer().playSound(context.getPlayer().getLocation(),
                Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1, 1);
          }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
          });
        })
        .withClickHandler(Action.RIGHT_CLICK_AIR, context -> openMultiToolMenu(context.getPlayer()))
        .withClickHandler(Action.RIGHT_CLICK_BLOCK, context -> openMultiToolMenu(context.getPlayer()))
    );
    return menu;
  }

  private static Component render(ComponentLike component, Player player) {
    return GlobalTranslator.render(component.asComponent(), Locale.forLanguageTag(player.getLocale()));
  }

  private LocalizedItem.Builder groupItem(NodeGroup group) {
    int mod = group.getKey().hashCode();

    TagResolver resolver = FormattableBuilder.builder()
        .insertObject("group", group)
        .insertList("modifiers", group.getModifiers())
        .toResolver();

    return new LocalizedItem.Builder(new ItemStack(GROUP_ITEM_LIST[Math.floorMod(mod, 16)]))
        .withName(Messages.E_SUB_GROUP_ENTRY_N.formatted(resolver))
        .withLore(Messages.E_SUB_GROUP_ENTRY_L.formatted(resolver));
  }

  private void openGroupMenu(Player player, Node node) {

    storage.loadAllGroups().thenAccept(nodeGroups -> {
      ArrayList<NodeGroup> nodeGroupList = new ArrayList<>(nodeGroups);
      nodeGroupList.sort(Comparator.comparing(g -> g.getKey().toString()));

      ListMenu menu = new ListMenu(render(Messages.E_SUB_GROUP_TITLE, player), 4);
      menu.addPreset(MenuPresets.fillRow(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), 3)); //TODO extract icon
      menu.addPreset(MenuPresets.paginationRow(3, 0, 1, false, Action.LEFT));
      for (NodeGroup group : nodeGroupList) {
        if (group.getKey().equals(CommonPathFinder.globalGroupKey())) {
          continue;
        }

        menu.addListEntry(Button.builder()
            .withItemStack(() -> {
              ItemStack stack = groupItem(group).createItem(player);
              if (group.contains(node.getNodeId())) {
                stack = ItemStackUtils.setGlow(stack);
              }
              return stack;
            })
            .withClickHandler(Action.LEFT, groupEntryClickHandler(menu, group, node))
            .withClickHandler(Action.RIGHT, groupEntryClickHandler(menu, group, node))
        );
      }
      menu.addPreset(presetApplier -> {
        presetApplier.addItemOnTop(3 * 9 + 8,
            new LocalizedItem(Material.BARRIER, Messages.E_SUB_GROUP_RESET_N,
                Messages.E_SUB_GROUP_RESET_L).createItem(player));
        presetApplier.addClickHandlerOnTop(3 * 9 + 8, Action.LEFT, c -> {
          if (!lock.compareAndSet(false, true)) {
            BukkitUtils.wrap(c.getPlayer()).sendMessage(Messages.GEN_TOO_FAST);
            return;
          }
          StorageUtil.clearGroups(node).thenRun(() -> {
            menu.refresh(menu.getListSlots());
            c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f, 1f);
          }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
              throwable.printStackTrace();
            }
            lock.set(false);
          });
        });

        presetApplier.addItemOnTop(3 * 9 + 4,
            new LocalizedItem(Material.PAPER, Messages.E_SUB_GROUP_INFO_N,
                Messages.E_SUB_GROUP_INFO_L).createItem(player));
      });
      menu.open(player);
    });
  }

  private ContextConsumer<ClickContext> groupEntryClickHandler(ListMenu menu, NodeGroup group, Node node) {
    return c -> {
      if (!lock.compareAndSet(false, true)) {
        BukkitUtils.wrap(c.getPlayer()).sendMessage(Messages.GEN_TOO_FAST);
        return;
      }
      if (group.contains(node.getNodeId())) {
        StorageUtil.removeGroups(group, node.getNodeId()).thenRun(() -> {
          c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
          menu.refresh(menu.getListSlots());
        }).whenComplete((unused, throwable) -> {
          if (throwable != null) {
            throwable.printStackTrace();
          }
          lock.set(false);
        });
      } else {
        StorageUtil.addGroups(group, node.getNodeId()).thenRun(() -> {
          c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
          menu.refresh(menu.getListSlots());
        }).whenComplete((unused, throwable) -> {
          if (throwable != null) {
            throwable.printStackTrace();
          }
          lock.set(false);
        });
      }
    };
  }

  private void openMultiToolMenu(Player player) {
    storage.loadAllGroups().thenAccept(nodeGroups -> {

      ArrayList<NodeGroup> nodeGroupList = new ArrayList<>(nodeGroups);
      nodeGroupList.sort(Comparator.comparing(g -> g.getKey().toString()));

      ListMenu menu = new ListMenu(render(Messages.E_SUB_GROUP_TITLE, player), 4);
      menu.addPreset(MenuPresets.fillRow(new ItemStack(Material.BLACK_STAINED_GLASS_PANE),
              3)); //TODO extract icon
      menu.addPreset(MenuPresets.paginationRow(3, 0, 1, false, Action.LEFT));
      for (NodeGroup group : nodeGroupList) {
        if (group.getKey().equals(CommonPathFinder.globalGroupKey())) {
          continue;
        }

        menu.addListEntry(Button.builder()
            .withItemStack(() -> {
              ItemStack stack = groupItem(group).createItem(player);
              if (multiTool.contains(group.getKey())) {
                stack = ItemStackUtils.setGlow(stack);
              }
              return stack;
            })
            .withClickHandler(Action.LEFT, multiToolEntryClickHandler(menu, group))
            .withClickHandler(Action.RIGHT, multiToolEntryClickHandler(menu, group))
        );
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

  private ContextConsumer<ClickContext> multiToolEntryClickHandler(ListMenu menu, NodeGroup group) {
    return c -> {
      if (multiTool.contains(group.getKey())) {
        multiTool.remove(group.getKey());
        c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
        menu.refresh(menu.getListSlots());
      } else {
        multiTool.add(group.getKey());
        c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
        menu.refresh(menu.getListSlots());
      }
    };
  }

  private void openNodeTypeMenu(Player player, Location location) {

    ListMenu menu = new ListMenu(Component.text("Node-Gruppen verwalten:"), 2);
    for (NodeType<?> type : types) {

      menu.addListEntry(Button.builder()
          .withItemStack(() -> new ItemStack(Material.CYAN_CONCRETE_POWDER))
          .withClickHandler(Action.RIGHT, c -> {
            storage.createAndLoadNode(type, BukkitVectorUtils.toInternal(location));
            menu.close(player);
          }));
    }
    menu.open(player);
  }
}
