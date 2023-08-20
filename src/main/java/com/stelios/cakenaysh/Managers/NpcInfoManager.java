package com.stelios.cakenaysh.Managers;

import com.mongodb.client.MongoCollection;
import com.stelios.cakenaysh.Items.CustomItems;
import com.stelios.cakenaysh.Main;
import com.stelios.cakenaysh.Npc.Traits.NpcStats;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelTrait;

import java.util.ArrayList;
import java.util.HashMap;

public class NpcInfoManager {

    Main main = Main.getPlugin(Main.class);
    MongoCollection<Document> npcInfo = main.getDatabase().getNpcInfo();


    //updates the specified npc's information document
    public void updateNpcInfo(NPC npc){

        //if the npc is not a sentinel with the npcstats trait
        if (!(npc.hasTrait(SentinelTrait.class) && npc.hasTrait(NpcStats.class))){
            return;
        }

        //get the traits
        SentinelTrait sentinelTrait = npc.getOrAddTrait(SentinelTrait.class);
        NpcStats npcStats = npc.getOrAddTrait(NpcStats.class);

        //create a new document if the npc doesn't have one
        if (npcInfo.find(new Document("uuid", npc.getUniqueId().toString())).first() == null){
            npcInfo.insertOne(new Document("uuid", npc.getUniqueId().toString())
                    .append("name", npc.getName())
                    .append("equipment", new ArrayList<String>())
                    .append("stats", new HashMap<String, Float>())
                    .append("drops", new HashMap<String, Double>()));
        }

        //get the npc's equipment
        ArrayList<String> equipment = new ArrayList<>();
        for (ItemStack item : npc.getOrAddTrait(Equipment.class).getEquipment()){

            //if the item is null, continue
            if (item == null){
                continue;
            }

            //if the item is a custom item
            if (CustomItems.getNameFromItem(item) != null) {

                //add the custom item's name to the equipment
                equipment.add(CustomItems.getNameFromItem(item));

            //if the item is a base material
            } else {
                //add the item's material to the equipment
                equipment.add(item.getType().toString());
            }
        }

        //get the npc's stats
        HashMap<String, Float> stats = new HashMap<>();
        stats.put("health", (float) sentinelTrait.health);
        stats.put("damage", (float) sentinelTrait.getDamage());
        stats.put("speed", (float) sentinelTrait.speed);
        stats.put("range", (float) sentinelTrait.range);
        stats.put("attack rate", (float) sentinelTrait.attackRate);
        stats.put("xp", npcStats.getXp());
        stats.put("crit damage", npcStats.getCritDamage());
        stats.put("crit chance", npcStats.getCritChance());
        stats.put("strength", npcStats.getStrength());
        stats.put("defense", npcStats.getDefense());
        stats.put("infernal defense", npcStats.getInfernalDefense());
        stats.put("infernal damage", npcStats.getInfernalDamage());
        stats.put("undead defense", npcStats.getUndeadDefense());
        stats.put("undead damage", npcStats.getUndeadDamage());
        stats.put("aquatic defense", npcStats.getAquaticDefense());
        stats.put("aquatic damage", npcStats.getAquaticDamage());
        stats.put("aerial defense", npcStats.getAerialDefense());
        stats.put("aerial damage", npcStats.getAerialDamage());
        stats.put("melee defense", npcStats.getMeleeDefense());
        stats.put("melee damage", npcStats.getMeleeDamage());
        stats.put("ranged defense", npcStats.getRangedDefense());
        stats.put("ranged damage", npcStats.getRangedDamage());
        stats.put("magic defense", npcStats.getMagicDefense());
        stats.put("magic damage", npcStats.getMagicDamage());

        //get the npc's drops
        ArrayList<ItemStack> dropItems = sentinelTrait.drops;
        ArrayList<Double> dropChances = sentinelTrait.dropChances;
        HashMap<String, Double> drops = new HashMap<>();

        //if there is one drop chance
        if (dropChances.size() == 1) {

            //add the same drop chance for each drop
            for (int i = 1; i < dropItems.size(); i++) {
                dropChances.add(dropChances.get(0));
            }
        }

        //loop through the npcs drops
        for (int i = 0; i < dropItems.size(); i++){

            //if the item is null, continue
            if (dropItems.get(i) == null){
                continue;
            }

            //if the item is a custom item
            if (CustomItems.getNameFromItem(dropItems.get(i)) != null) {

                //add the custom item's name and drop chance to the drops
                drops.put(CustomItems.getNameFromItem(dropItems.get(i)), dropChances.get(i));

            //if the item is a base material
            } else {
                //add the item's material and drop chance to the drops
                drops.put(dropItems.get(i).getType().toString(), dropChances.get(i));
            }
        }

        //update the npc's document
        Document update = new Document("name", npc.getName()).append("equipment", equipment).append("stats", stats).append("drops", drops);
        npcInfo.updateOne(new Document("uuid", npc.getUniqueId().toString()), new Document("$set", update));

    }

}