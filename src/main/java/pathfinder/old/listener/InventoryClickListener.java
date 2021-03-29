package pathfinder.old.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import main.de.bossascrew.pathfinder.PathSystem;
import pathfinder.old.RoadMap;
import pathfinder.old.data.Message;
import pathfinder.old.inventories.InventoryNodeEditor;
import pathfinder.old.system.Node;
import pathfinder.old.visualization.EdgeCreator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.wesjd.anvilgui.AnvilGUI;

public class InventoryClickListener implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if(event.getView().getTitle().equals(InventoryNodeEditor.GUI_TITLE)) {
			
			if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
			if(!(event.getView().getPlayer() instanceof Player)) return;
			Player p = (Player) event.getView().getPlayer();
			NBTItem nbti = new NBTItem(event.getCurrentItem());
			InventoryNodeEditor inv = InventoryNodeEditor.getInv(nbti.getInteger(InventoryNodeEditor.NODE_ID_KEY));
			RoadMap rm = inv.getRoadMap();
			Node n = inv.getNode();

			event.setCancelled(true);
			
			switch (nbti.getString(InventoryNodeEditor.NODE_ACTION_KEY)) {
			case InventoryNodeEditor.NODE_ACTION_VALUE_DELETE:
				p.closeInventory();
				rm.removeWaypoint(n.id);
				break;
			case InventoryNodeEditor.NODE_ACTION_VALUE_EDGE:

				p.closeInventory();
				EdgeCreator edgeCreator = new EdgeCreator(rm, p.getUniqueId());
				edgeCreator.setStart(n);
				p.sendMessage(Message.EDGE_CREATION_STARTED);
				rm.getEdgeManager().setCreator(p.getUniqueId(), edgeCreator);
				
				break;
			case InventoryNodeEditor.NODE_ACTION_VALUE_PERMISSION:
				
				TextComponent tc = new TextComponent("\n�7===========================\nPermission suggestion: ");
				String permission = "navigator." + rm.getKey() + "" + n.value;
				tc.addExtra("�a" + permission + "\n");
				
				TextComponent accept = new TextComponent("�7[�aaccept�7] ");
				accept.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to accept").create()));
				accept.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, 
						"/ps edit " + rm.getKey() + " waypoint permission " + n.value + " " + permission));
				
				TextComponent createOwn = new TextComponent(ChatColor.GRAY + "[�9create own" + ChatColor.GRAY +"]");
				createOwn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Create own").create()));
				createOwn.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, 
						"/ps edit " + rm.getKey() + " waypoint permission " + n.value + " [permission]"));
				
				tc.addExtra(accept);
				tc.addExtra(createOwn);
				p.closeInventory();
				p.sendMessage(tc);
				break;
			case InventoryNodeEditor.NODE_ACTION_VALUE_TANGENT:
				new AnvilGUI.Builder()
			    .onComplete((player, text) -> {
			    	try {
				    	n.tangentReach = Float.parseFloat(text);
				    	n.tangentReachSetManually = true;
						return AnvilGUI.Response.close();
			    	} catch (IllegalArgumentException e) {
			    		return AnvilGUI.Response.text("Muss Zahl sein");
			    	}
			    })
			    .text("Setzte Wert...")
			    .item(new ItemStack(Material.NAME_TAG))
			    .title("Setze die Rundungsst�rke f�r diese Node")
			    .plugin(PathSystem.getInstance())
			    .open(p);
				break;
			}
		}
	}
}
