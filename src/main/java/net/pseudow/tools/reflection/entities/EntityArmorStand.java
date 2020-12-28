package net.pseudow.tools.reflection.entities;

import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Location;

public class EntityArmorStand extends EntityLiving {
    private final Object entityArmorStand;

    /**
     * Create easily an armor stand using nms. This class will help you
     * to implement multi-versioning.
     *
     * @author Pseudow
     *
     * @param location - The armor stand location
     */
    public EntityArmorStand(Location location) {
        super(Reflection.getNMSClass("EntityArmorStand"), location);
        this.entityArmorStand = Reflection.getNMSClass("EntityArmorStand").cast(getEntityLiving());
    }

    public void setBasePlate(boolean option) {
        Reflection.invokeMethod(entityArmorStand, "setBasePlate", option);
    }

    public void setSmall(boolean option) {
        Reflection.invokeMethod(entityArmorStand, "setSmall", option);
    }

    public void setGravity(boolean option) {
        Reflection.invokeMethod(entityArmorStand, "setGravity", option);
    }

    public void setSize(double a, double b) {
        Reflection.invokeMethod(entityArmorStand, "setSize", a, b);
    }

    public void setArms(boolean option) {
        Reflection.invokeMethod(entityArmorStand, "setArms", option);
    }
}
