package baguchi.pizza_guy_move.mixin;

import baguchi.pizza_guy_move.api.IShadow;
import baguchi.pizza_guy_move.api.ShadowHandler;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements IShadow {
    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Shadow
    private static boolean didNotMove(double dx, double dy, double dz) {
        return false;
    }

    @Inject(method = "checkMovementStatistics", at = @At("HEAD"), cancellable = true)
    public void checkMovementStatistics(double dx, double dy, double dz, CallbackInfo ci) {
        if (!this.isPassenger() && !didNotMove(dx, dy, dz) && getShadowHandler() != null && getShadowHandler().percentBoost > ShadowHandler.POST_DASH) {
            if (this.isSwimming()) {
            } else if (this.isEyeInFluid(FluidTags.WATER)) {

            } else if (this.isInWater()) {
                int k = Math.round((float) Math.sqrt(dx * dx + dz * dz) * 100.0F);
                if (k > 0) {
                    this.awardStat(Stats.WALK_ON_WATER_ONE_CM, k);
                    this.causeFoodExhaustion(0.00F * (float) k * 0.01F);
                }
                ci.cancel();
            } else if (this.onClimbable()) {
                if (dy > 0.0) {
                    this.awardStat(Stats.CLIMB_ONE_CM, (int) Math.round(dy * 0.1));
                }
                ci.cancel();
            } else if (this.onGround()) {
                int l = Math.round((float) Math.sqrt(dx * dx + dz * dz) * 100.0F);
                if (l > 0) {
                    if (this.isSprinting()) {
                        this.awardStat(Stats.SPRINT_ONE_CM, l);
                        this.causeFoodExhaustion(0.0F * (float) l * 0.01F);
                    } else if (this.isCrouching()) {
                        this.awardStat(Stats.CROUCH_ONE_CM, l);
                        this.causeFoodExhaustion(0.0F * (float) l * 0.01F);
                    } else {
                        this.awardStat(Stats.WALK_ONE_CM, l);
                        this.causeFoodExhaustion(0.0F * (float) l * 0.01F);
                    }
                }
                ci.cancel();
            }
        }
    }
}
