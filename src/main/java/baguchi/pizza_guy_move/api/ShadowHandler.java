package baguchi.pizza_guy_move.api;

import baguchi.pizza_guy_move.PizzaGuyGame;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class ShadowHandler {
    public static final ResourceLocation RUN_ID = ResourceLocation.fromNamespaceAndPath(PizzaGuyGame.MODID, "mach_run");
    public static final float PRE_DASH = 0.4F;
    public static final float POST_DASH = 0.8F;
    public static final float FULL_DASH = 1F;
    public Vec3 prevShadow = Vec3.ZERO;

    public Vec3 shadow = Vec3.ZERO;

    public Vec3 prevShadow2 = Vec3.ZERO;

    public Vec3 shadow2 = Vec3.ZERO;

    public Vec2 shadowRot = Vec2.ZERO;
    public Vec2 shadowRot2 = Vec2.ZERO;
    public Vec2 prevShadowRot = Vec2.ZERO;
    public Vec2 prevShadowRot2 = Vec2.ZERO;

    public float percentBoost = 0.0F;

    public void tick(LivingEntity mob) {
        double elasticity = 0.25D;
        this.prevShadow = this.shadow;
        this.prevShadow2 = this.shadow2;
        this.prevShadowRot = this.shadowRot;
        this.prevShadowRot2 = this.shadowRot2;
        this.shadowRot = new Vec2((float) (mob.getXRot() + (this.shadowRot.x - mob.getXRot()) * elasticity * 0.75D), (float) (mob.yBodyRot + (this.shadowRot.y - mob.yBodyRot) * elasticity * 0.75D));
        this.shadowRot2 = new Vec2((float) (this.shadowRot.x + (this.shadowRot2.x - this.shadowRot.x) * elasticity * 0.3499999940395355D), (float) (this.shadowRot.y + (this.shadowRot2.y - this.shadowRot.y) * elasticity * 0.3499999940395355D));
        float shadowX = (float) (this.shadow.x + (mob.getX() - this.shadow.x) * elasticity);
        float shadowY = (float) (this.shadow.y + (mob.getY() - this.shadow.y) * elasticity);
        float shadowZ = (float) (this.shadow.z + (mob.getZ() - this.shadow.z) * elasticity);
        float shadowX2 = (float) (this.shadow2.x + (this.shadow.x - this.shadow2.x) * elasticity * 0.375D);
        float shadowY2 = (float) (this.shadow2.y + (this.shadow.y - this.shadow2.y) * elasticity * 0.375D);
        float shadowZ2 = (float) (this.shadow2.z + (this.shadow.z - this.shadow2.z) * elasticity * 0.375D);
        this.shadow = new Vec3(shadowX, shadowY, shadowZ);
        this.shadow2 = new Vec3(shadowX2, shadowY2, shadowZ2);
    }

    public void setPercentBoost(float percentBoost) {
        this.percentBoost = percentBoost;
    }

    public Vec3 getShadow() {
        return shadow;
    }

    public Vec3 getShadow2() {
        return shadow2;
    }

    public float getPercentBoost() {
        return percentBoost;
    }

    public Vec3 getPrevShadow() {
        return prevShadow;
    }

    public Vec3 getPrevShadow2() {
        return prevShadow2;
    }

    public Vec2 getShadowRot() {
        return shadowRot;
    }

    public Vec2 getShadowRot2() {
        return shadowRot2;
    }

    public Vec2 getPrevShadowRot() {
        return prevShadowRot;
    }

    public Vec2 getPrevShadowRot2() {
        return prevShadowRot2;
    }
}
