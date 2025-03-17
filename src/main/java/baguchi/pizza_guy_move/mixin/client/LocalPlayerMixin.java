package baguchi.pizza_guy_move.mixin.client;

import baguchi.pizza_guy_move.api.IShadow;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements IShadow {
    @Shadow
    public ClientInput input;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Shadow
    protected abstract boolean hasEnoughFoodToStartSprinting();

    @Shadow
    public abstract boolean isUnderWater();

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSprinting()Z", ordinal = 1))
    public boolean aiStep(boolean original) {
        if (original && getShadowHandler() != null) {
            boolean flag6 = !this.input.hasForwardImpulse() || !this.hasEnoughFoodToStartSprinting();
            boolean flag7 = !flag6 && (!this.isInWater() || !this.isUnderWater()) && getShadowHandler().percentBoost > 0.4F;
            if (flag7) {

                return false;
            }
        }
        return original;
    }
}
