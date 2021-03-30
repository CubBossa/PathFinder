package pathfinder.listener;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import pathfinder.PathPlayer;
import pathfinder.handler.PlayerHandler;

import java.util.UUID;

public class EditModeListener implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        //TODO wenn item ein Editmodeitem editmode beenden
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        //TODO armorstand interaktion
    }

    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if(event.getNewGameMode().equals(GameMode.CREATIVE)
                || event.getNewGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }

        UUID uuid = event.getPlayer().getUniqueId();
        PathPlayer player = PlayerHandler.getInstance().getPlayer(uuid);
        assert player != null;

        if(!player.isEditing()) {
            return;
        }
        event.setCancelled(true);
        //TODO sende Message: bearbeitung abgebrochen
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        //TODO wenn in editmode cancellen
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        //TODO block interaktion abfragen zum erstellen einer Node
    }

    //TODO citizens NPC interact event, um citizens als Nodes zu erstellen

}
