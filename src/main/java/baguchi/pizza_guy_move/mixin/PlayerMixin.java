package baguchi.pizza_guy_move.mixin;

import baguchi.pizza_guy_move.api.IShadow;
import baguchi.pizza_guy_move.api.ShadowHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IShadow {

    @Unique
    public ShadowHandler pizzaGuyGame$shadowHandler = new ShadowHandler();

    @Shadow
    @Final
    private Abilities abilities;

    @Shadow
    public abstract Abilities getAbilities();

    protected PlayerMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    public void aiStep(CallbackInfo callbackInfo) {
        if (pizzaGuyGame$shadowHandler != null) {
            if (!this.level().isClientSide) {
                pizzaGuyGame$removeDashBoost(this);
            }
            if (this.level().isClientSide) {
                pizzaGuyGame$shadowHandler.tick(this);
            }

            if (pizzaGuyGame$shadowHandler.percentBoost > ShadowHandler.PRE_DASH) {
                pizzaGuyGame$pushEntitiesWhenDashed(this);
            }
            pizzaGuyGame$tryAddDashBooster(this);
        }
    }


    // climing stuff like peppino
    @Override
    public boolean onClimbable() {
        return super.onClimbable() || this.pizzaGuyGame$shadowHandler.percentBoost > ShadowHandler.PRE_DASH && this.horizontalCollision;
    }

    private Vec3 handleOnClimbable(Vec3 p_21298_) {
        if (this.onClimbable()) {
            this.resetFallDistance();
            float f = 0.15F;
            double d0 = Mth.clamp(p_21298_.x, (double) -0.15F, (double) 0.15F);
            double d1 = Mth.clamp(p_21298_.z, (double) -0.15F, (double) 0.15F);
            double d2 = Math.max(p_21298_.y, (double) -0.15F);
            if (d2 < 0.0D && !this.getBlockStateOn().isScaffolding(this) && this.isSuppressingSlidingDownLadder()) {
                d2 = 0.0D;
            }

            p_21298_ = new Vec3(d0, d2, d1);
        }

        return p_21298_;
    }

    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    public void travel(Vec3 travelVector, CallbackInfo ci) {
        if (!this.getAbilities().flying) {
            if (pizzaGuyGame$shadowHandler != null) {
                if (this.isControlledByLocalInstance()) {
                    FluidState fluidstate = this.level().getFluidState(this.blockPosition());
                    if ((this.isInWater() && this.isUnderWater() || this.isInLava() || this.isInFluidType(fluidstate)) && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate)) {
                        //this.travelInFluid(travelVector, fluidstate);
                    } else if (this.isFallFlying()) {
                        //this.travelFallFlying();
                    } else {
                        pizzaGuyGame$travelInAir(travelVector);
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Unique
    private void pizzaGuyGame$travelInAir(Vec3 travelVector) {
        BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
        float f = this.onGround() ? this.level().getBlockState(blockpos).getFriction(this.level(), blockpos, this) : 1.0F;
        float f1 = f * 0.91F;
        Vec3 vec3 = this.handleRelativeFrictionAndCalculateMovement(travelVector, f);
        double d0 = vec3.y;
        MobEffectInstance mobeffectinstance = this.getEffect(MobEffects.LEVITATION);
        if (mobeffectinstance != null) {
            d0 += (0.05 * (double) (mobeffectinstance.getAmplifier() + 1) - vec3.y) * 0.2;
        } else if (!this.level().isClientSide || this.level().hasChunkAt(blockpos)) {
            d0 -= this.getEffectiveGravity();
        } else if (this.getY() > (double) this.level().getMinY()) {
            d0 = -0.1;
        } else {
            d0 = 0.0;
        }

        if (this.shouldDiscardFriction() || vec3.horizontalDistance() > 0.1F && !this.onGround()) {
            this.setDeltaMovement(vec3.x, d0, vec3.z);
        } else {
            float f2 = this instanceof FlyingAnimal ? f1 : 0.98F;
            this.setDeltaMovement(vec3.x * (double) f1, d0 * (double) f2, vec3.z * (double) f1);
        }
    }

    private float getFrictionInfluencedSpeed(float p_21331_) {
        if (!this.abilities.flying && !this.onGround() && this.pizzaGuyGame$shadowHandler.percentBoost > 0.5F) {
            return this.getSpeed() * (0.21600002F / 0.98F);
        } else if (this.abilities.flying) {
            return this.getFlyingSpeed();
        }
        return this.onGround() ? this.getSpeed() * (0.21600002F / (p_21331_ * p_21331_ * p_21331_)) : this.getFlyingSpeed();
    }

    @Unique
    public Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 p_21075_, float p_21076_) {
        this.moveRelative(this.getFrictionInfluencedSpeed(p_21076_), p_21075_);
        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 vec3 = this.getDeltaMovement();
        if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.getInBlockState().is(Blocks.POWDER_SNOW))) {
            vec3 = new Vec3(vec3.x, 0.2D + 0.2F * pizzaGuyGame$shadowHandler.percentBoost, vec3.z);
        }

        return vec3;
    }


    /*
     * Dashing Attack Stuff
     */

    @Unique
    public AABB pizzaGuyGame$getAttackBoundingBox() {
        Vec3 vec3d = this.getViewVector(1.0F);
        Vec3 vec3 = new Vec3(this.getX() - (double) (this.getBbWidth() * 0.85D), this.getY(), this.getZ() - (double) (this.getBbWidth() * 0.85D));
        Vec3 vec31 = new Vec3(this.getX() + (double) (this.getBbWidth() * 0.85D), this.getY() + this.getBbHeight(), this.getZ() + (double) (this.getBbWidth() * 0.85D));
        return new AABB(vec3, vec31).move(vec3d.x * 1.5D, vec3d.y * 1.5D, vec3d.z * 1.5D);
    }

    @Unique
    protected void pizzaGuyGame$pushEntitiesWhenDashed(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            List<LivingEntity> list = entity.level().getEntities(EntityTypeTest.forClass(LivingEntity.class), pizzaGuyGame$getAttackBoundingBox(), EntitySelector.pushableBy(entity));
            if (!list.isEmpty()) {
                for (int l = 0; l < list.size(); ++l) {
                    LivingEntity entity2 = list.get(l);
                    if (entity != entity2 && !entity.isAlliedTo(entity2)) {
                        entity2.knockback(2.0D * pizzaGuyGame$shadowHandler.percentBoost, entity.getX() - entity2.getX(), entity.getZ() - entity2.getZ());
                        entity2.hurt(entity.damageSources().mobAttack(entity2), Mth.floor(10.0F * pizzaGuyGame$shadowHandler.percentBoost));
                    }
                }
            }
        }
    }

    @Unique
    protected void pizzaGuyGame$removeDashBoost(LivingEntity entity) {
        AttributeInstance attributeinstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeinstance != null) {
            if (attributeinstance.getModifier(ShadowHandler.RUN_ID) != null) {
                attributeinstance.removeModifier(ShadowHandler.RUN_ID);
            }

        }
    }

    @Unique
    protected void pizzaGuyGame$tryAddDashBooster(LivingEntity entity) {
        if ((entity.isSprinting()) && entity.getPose() == Pose.STANDING) {
            if (pizzaGuyGame$shadowHandler.percentBoost <= 1) {
                pizzaGuyGame$shadowHandler.percentBoost += 0.01F;
            } else if (pizzaGuyGame$shadowHandler.percentBoost <= 2) {
                pizzaGuyGame$shadowHandler.percentBoost += 0.005F;
            } else {
                pizzaGuyGame$shadowHandler.percentBoost = 2;
            }

        } else if (!this.horizontalCollision) {
            if (pizzaGuyGame$shadowHandler.percentBoost >= 0) {
                pizzaGuyGame$shadowHandler.percentBoost -= 0.1F;
            } else {
                pizzaGuyGame$shadowHandler.percentBoost = 0;
            }
        } else {
            if (!this.onGround() && this.verticalCollision) {
                pizzaGuyGame$shadowHandler.percentBoost = 0;
            } else {
                if (pizzaGuyGame$shadowHandler.percentBoost <= 1) {
                    pizzaGuyGame$shadowHandler.percentBoost += 0.01F;
                }
            }
        }

        if (pizzaGuyGame$shadowHandler.percentBoost > ShadowHandler.PRE_DASH) {
            entity.walkAnimation.setSpeed(pizzaGuyGame$shadowHandler.percentBoost + 1.0F);
        }

        if (pizzaGuyGame$shadowHandler.percentBoost > 0) {
            if (!entity.level().isClientSide) {
                AttributeInstance attributeinstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                if (attributeinstance == null) {
                    return;
                }

                float f = 0.75F * pizzaGuyGame$shadowHandler.percentBoost;
                attributeinstance.addTransientModifier(new AttributeModifier(ShadowHandler.RUN_ID, f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    @Override
    public ShadowHandler getShadowHandler() {
        return pizzaGuyGame$shadowHandler;
    }

    @Override
    public void setPos(double p_20210_, double p_20211_, double p_20212_) {
        super.setPos(p_20210_, p_20211_, p_20212_);
        //when shadow is too far or nothing reset
        if (this.getShadowHandler() != null) {
            if (this.pizzaGuyGame$shadowHandler.shadow == null || this.pizzaGuyGame$shadowHandler.shadow2 == null || this.pizzaGuyGame$shadowHandler.shadow.distanceTo(this.position()) >= 30) {
                this.pizzaGuyGame$shadowHandler.shadow = new Vec3(p_20210_, p_20211_, p_20212_);
                this.pizzaGuyGame$shadowHandler.shadow2 = new Vec3(p_20210_, p_20211_, p_20212_);
            }
        }
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return super.canStandOnFluid(fluidState) || this.getShadowHandler() != null && this.getShadowHandler().getPercentBoost() > ShadowHandler.PRE_DASH;
    }
}