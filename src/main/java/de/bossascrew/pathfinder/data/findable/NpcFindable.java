package de.bossascrew.pathfinder.data.findable;

import net.citizensnpcs.api.npc.NPC;

public interface NpcFindable extends Findable {

    /**
     * @return Die ID des NPCs
     */
    int getNpcId();

    /**
     * @return Der Citizens NPC, der zum Beispiel die Position des
     */
    NPC getNpc();

}
