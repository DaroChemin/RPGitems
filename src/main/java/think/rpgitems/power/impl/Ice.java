package think.rpgitems.power.impl;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import think.rpgitems.I18n;
import think.rpgitems.RPGItems;
import think.rpgitems.power.*;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static think.rpgitems.power.Utils.getNearbyEntities;

/**
 * Power ice.
 * <p>
 * The ice power will fire an ice block on right click
 * which will then create a box of ice on impact,
 * the ice will slowly remove itself.
 * </p>
 */
@Meta(defaultTrigger = "RIGHT_CLICK", withSelectors = true, generalInterface = PowerPlain.class, implClass = Ice.Impl.class)
public class Ice extends BasePower {

    @Override
    public String getName() {
        return "ice";
    }

    @Override
    public String displayText() {
        return I18n.formatDefault("power.ice", 0);
    }

    public static class Impl implements PowerRightClick<Ice>, PowerLeftClick<Ice>, PowerSprint<Ice>, PowerSneak<Ice>, PowerPlain<Ice>, PowerBowShoot<Ice> {

        @Override
        public PowerResult<Void> rightClick(Ice power, final Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(power, player, stack);
        }

        @Override
        public PowerResult<Void> fire(Ice power, Player player, ItemStack stack) {
            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0f, 0.1f);

            // launch an ice block
            final FallingBlock block = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 1.8, 0), Material.PACKED_ICE.createBlockData());
            block.setVelocity(player.getLocation().getDirection().multiply(2d));
            block.setDropItem(false);

            BukkitRunnable run = new BukkitRunnable() {
                public void run() {
                    boolean hit = false;
                    World world = block.getWorld();

                    List<Entity> entities = getNearbyEntities(power, block.getLocation(), player, 1);
                    for (Entity e : entities) {
                        if (e != player && e != block) {
                            hit = true;
                            break;
                        }
                    }

                    if (block.isDead() || hit) {
                        Location landingLoc = block.getLocation();
                        boolean hitBlock = block.isDead();
                        // remove entity and (potential) placed block.
                        block.remove();
                        if (hitBlock) {
                            if (landingLoc.getBlock().getType().equals(Material.PACKED_ICE)) {
                                landingLoc.getBlock().setType(Material.AIR);
                            }
                        }
                        cancel();
                        final HashMap<Location, BlockData> changedBlocks = new HashMap<>();
                        for (int x = -1; x < 2; x++) {
                            for (int y = -1; y < 3; y++) {
                                for (int z = -1; z < 2; z++) {
                                    Location loc = landingLoc.clone().add(x, y, z);
                                    Block b = world.getBlockAt(loc);
                                    if (!b.getType().isSolid() &&
                                                !(b.getType() == Material.PLAYER_HEAD || b.getType() == Material.PLAYER_WALL_HEAD)) {
                                        changedBlocks.put(b.getLocation(), b.getBlockData());
                                        b.setType(Material.PACKED_ICE);
                                    }
                                }
                            }
                        }

                        // ice block remove timer
                        (new BukkitRunnable() {
                            Random random = new Random();

                            @Override
                            public void run() {
                                for (int i = 0; i < 4; i++) {
                                    if (changedBlocks.isEmpty()) {
                                        cancel();
                                        return;
                                    }
                                    int index = random.nextInt(changedBlocks.size());
                                    BlockData data = changedBlocks.values().toArray(new BlockData[0])[index];
                                    Location position = changedBlocks.keySet().toArray(new Location[0])[index];
                                    changedBlocks.remove(position);
                                    Block c = position.getBlock();
                                    position.getWorld().playEffect(position, Effect.STEP_SOUND, c.getType());
                                    c.setBlockData(data);
                                }

                            }
                        }).runTaskTimer(RPGItems.plugin, 4 * 20 + new Random().nextInt(40), 3);
                    }

                }
            };
            run.runTaskTimer(RPGItems.plugin, 0, 1);
            return PowerResult.ok();
        }

        @Override
        public Class<? extends Ice> getPowerClass() {
            return Ice.class;
        }

        @Override
        public PowerResult<Void> leftClick(Ice power, Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(power,player, stack);
        }

        @Override
        public PowerResult<Float> bowShoot(Ice power, Player player, ItemStack stack, EntityShootBowEvent event) {
            return fire(power,player, stack).with(event.getForce());
        }

        @Override
        public PowerResult<Void> sneak(Ice power, Player player, ItemStack stack, PlayerToggleSneakEvent event) {
            return fire(power,player, stack);
        }

        @Override
        public PowerResult<Void> sprint(Ice power, Player player, ItemStack stack, PlayerToggleSprintEvent event) {
            return fire(power,player, stack);
        }
    }
}
