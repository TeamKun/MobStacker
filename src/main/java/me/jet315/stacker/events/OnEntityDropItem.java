package me.jet315.stacker.events;

import me.jet315.stacker.MobStacker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Jet on 24/01/2018.
 */
public class OnEntityDropItem implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDropItem(EntityDropItemEvent e) {
        Entity entity = e.getEntity();

        if (!(entity instanceof LivingEntity)) {
            return; // Not a living entity.
        }

        if (entity.getType() != EntityType.PLAYER) {
            int amount = MobStacker.getInstance().getStackEntity().parseAmount(entity);
            if (amount > 1) {
                ItemStack item = new ItemStack(e.getItemDrop().getItemStack());
                item.setAmount(amount - 1);
                entity.getWorld().dropItem(entity.getLocation(), item);
            }
        }
    }

}
