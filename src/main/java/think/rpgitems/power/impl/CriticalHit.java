package think.rpgitems.power.impl;

import static think.rpgitems.power.Utils.getAngleBetweenVectors;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.I18n;
import think.rpgitems.power.*;

@Meta(defaultTrigger = "HIT", implClass = CriticalHit.Impl.class)
public class CriticalHit extends BasePower {

  @Property public double chance = 20;

  @Property public double backstabChance = 0;

  @Property public double factor = 1.5;

  @Property public double backstabFactor = 1.5;

  @Property public boolean setBaseDamage = false;

  @Override
  public String getName() {
    return "criticalhit";
  }

  @Override
  public String displayText() {
    return (getBackstabChance() != 0 && getChance() != 0)
        ? I18n.formatDefault(
            "power.criticalhit.both",
            getChance(),
            getFactor(),
            getBackstabChance(),
            getBackstabFactor())
        : (getChance() != 0)
            ? I18n.formatDefault("power.criticalhit.critical", getChance(), getFactor())
            : I18n.formatDefault(
                "power.criticalhit.backstab", getBackstabChance(), getBackstabFactor());
  }

  public double getBackstabChance() {
    return backstabChance;
  }

  public double getChance() {
    return chance;
  }

  public double getFactor() {
    return factor;
  }

  public double getBackstabFactor() {
    return backstabFactor;
  }

  public boolean isSetBaseDamage() {
    return setBaseDamage;
  }

  public static class Impl implements PowerHit<CriticalHit> {
    @Override
    public PowerResult<Double> hit(
        CriticalHit power,
        Player player,
        ItemStack stack,
        LivingEntity entity,
        double damage,
        EntityDamageByEntityEvent event) {
      if (ThreadLocalRandom.current().nextDouble(100) < power.getChance()) {
        damage *= power.getFactor();
      }
      if (getAngleBetweenVectors(
                  ((LivingEntity) event.getEntity()).getEyeLocation().getDirection(),
                  player.getEyeLocation().getDirection())
              < 90
          && ThreadLocalRandom.current().nextDouble(100) < power.getBackstabChance()) {
        damage *= power.getBackstabFactor();
      }
      if (power.isSetBaseDamage()) {
        event.setDamage(damage);
      }
      return PowerResult.ok(damage);
    }

    @Override
    public Class<? extends CriticalHit> getPowerClass() {
      return CriticalHit.class;
    }
  }
}
