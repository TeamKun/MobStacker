package me.jet315.stacker.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.world.item.ItemTypes;
import me.jet315.stacker.MobStacker;
import me.jet315.stacker.manager.StackEntity;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

/**
 * Created by Jet on 24/01/2018.
 */
public class OnEntityFeed implements Listener {
    private final Map<EntityType, Material> feedItems = ImmutableMap.<EntityType, Material>builder()
            .put(EntityType.SHEEP, Material.WHEAT)
            .put(EntityType.COW, Material.WHEAT)
            .put(EntityType.MUSHROOM_COW, Material.WHEAT)
            .put(EntityType.CHICKEN, Material.WHEAT_SEEDS)
            .put(EntityType.PIG, Material.CARROT)
            .put(EntityType.TURTLE, Material.SEAGRASS)
            .build();

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityFeed(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();

        if (!(entity instanceof LivingEntity)) {
            return; // Not a living entity.
        }

        PlayerInventory inv = e.getPlayer().getInventory();
        ItemStack itemStack = inv.getItem(e.getHand());

        if (feedItems.get(entity.getType()) != itemStack.getType())
            return;

        StackEntity stack = MobStacker.getInstance().getStackEntity();

        int amount = stack.parseAmount(entity);
        if (amount < 2)
            return;

        e.setCancelled(true);

        int amountConsumed;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            amountConsumed = amount;
        } else {
            int amountLeft = Math.max(0, itemStack.getAmount() - amount);
            amountConsumed = itemStack.getAmount() - amountLeft;
            itemStack.setAmount(amountLeft);
            inv.setItem(e.getHand(), itemStack);
        }

        entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), amountConsumed, 1, 1, 1);

        LivingEntity dupEntity = stack.duplicate((LivingEntity) entity);
        if (dupEntity instanceof Ageable)
            ((Ageable) dupEntity).setBaby();
        stack.setAmount(dupEntity, amountConsumed / 2);
    }

}
