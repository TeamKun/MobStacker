package me.jet315.stacker.manager;

import me.jet315.stacker.MobStacker;
import me.jet315.stacker.util.Config;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.material.Colorable;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

/**
 * Created by Jet on 24/01/2018.
 */
public class StackEntity {


    private Config mobStackerConfig;

    public StackEntity(Config mobStackerConfig) {
        this.mobStackerConfig = mobStackerConfig;
    }

    /**
     * The stacked mob custom name format.
     */
    public static int INVALID_STACK = -1;


    public LivingEntity duplicate(LivingEntity livingEntity) {
        LivingEntity dupEntity = (LivingEntity) livingEntity.getWorld().spawnEntity(livingEntity.getLocation(), livingEntity.getType());
        if (livingEntity instanceof Sheep && dupEntity instanceof Sheep)
            ((Sheep) dupEntity).setColor(((Sheep) livingEntity).getColor());
        return dupEntity;
    }

    /*
     * Methods used to Stack or Unstack mobs
     */
    public boolean attemptUnstackOne(LivingEntity livingEntity) {

        int mobsAmount = parseAmount(livingEntity);

        // Kill this mob
        livingEntity.setHealth(0);

        if (mobsAmount <= 1) {
            // The stack is down to one mob; don't recreate it
            return false;
        }


        // Recreate the stack with one less mob
        mobsAmount--;

        LivingEntity dupEntity = duplicate(livingEntity);
        setAmount(dupEntity, mobsAmount);
        dupEntity.setCustomNameVisible(true);

        return true;
    }

    public boolean unstackAll(LivingEntity livingEntity) {
        setAmount(livingEntity, 1);
        //Hide name from users
        livingEntity.setCustomNameVisible(false);
        livingEntity.setHealth(0);
        MobStacker.getInstance().getEntityStacker().getValidEntity().remove(livingEntity);
        return true;
    }


    public boolean stack(LivingEntity target, LivingEntity stackee) {
        if (target.getType() != stackee.getType()) {
            return false; // The entities must be of the same type.
        }
        if (target instanceof Ageable && stackee instanceof Ageable) {
            if (((Ageable) target).isAdult() != ((Ageable) stackee).isAdult())
                return false; // The entities must be of the same age.
            if (((Ageable) target).canBreed() != ((Ageable) stackee).canBreed())
                return false; // The entities must be of the breedable or not.
        }
        if (target instanceof Pig && stackee instanceof Pig) {
            if (!((Pig) target).hasSaddle() && !((Pig) stackee).hasSaddle())
                return false; // The entities must not have saddle.
        }
        if (target instanceof Colorable && stackee instanceof Colorable) {
            if (((Colorable) target).getColor() != ((Colorable) stackee).getColor())
                return false; // The entities must be of the same color.
        }

        int alreadyStacked = parseAmount(target);
        int stackedMobsAlready = 1;

        // Check if the stackee is already a stack
        if (isStacked(stackee)) {
            stackedMobsAlready = parseAmount(stackee);
        }
        if (stackedMobsAlready >= MobStacker.getInstance().getMobStackerConfig().maxAllowedInStack || alreadyStacked >= MobStacker.getInstance().getMobStackerConfig().maxAllowedInStack)
            return false;
        stackee.remove();
        MobStacker.getInstance().getEntityStacker().getValidEntity().remove(stackee);
        if (alreadyStacked == INVALID_STACK) {
            // The target is NOT a stack
            target.setCustomNameVisible(true);
            setAmount(target, stackedMobsAlready + 1);
        } else {
            // The target is already a stack
            setAmount(target, alreadyStacked + stackedMobsAlready);
        }
        return true;
    }

    /*
     * "Helper" methods
     */
    public int parseAmount(String displayName) {
        if (displayName == null) {
            return INVALID_STACK; // No display name, therefor not a stack.
        }


        String colourStrip = ChatColor.stripColor(displayName);
        String str = colourStrip.replaceAll("[^-?0-9]+", " ");

        try {
            return Integer.parseInt(str.replaceAll(" ", ""));
        } catch (NumberFormatException e) {
            return INVALID_STACK;
        }
    }

    public int parseAmount(Entity target) {
        if (!target.hasMetadata("stack")) {
            return parseAmount(target.getCustomName()); // No metadata, therefor not a stack.
        }

        return target.getMetadata("stack").stream().findFirst().map(e -> {
            if (e.value() instanceof Integer)
                return (Integer) e.value();
            return INVALID_STACK;
        }).orElse(INVALID_STACK);
    }

    private boolean isStacked(LivingEntity entity) {
        return parseAmount(entity) != INVALID_STACK;
    }

    public void setAmount(Entity target, int amount) {
        String newDisplayName = mobStackerConfig.stackMobsDispalyName.replace("%number%", String.valueOf(amount));
        target.setCustomName(newDisplayName.replace("%type%", StringUtils.capitalize(target.getType().name().toLowerCase())));
        target.setMetadata("stack", new FixedMetadataValue(MobStacker.getInstance(), amount));
    }


}
