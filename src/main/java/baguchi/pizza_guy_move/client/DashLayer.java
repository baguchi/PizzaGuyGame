package baguchi.pizza_guy_move.client;

import baguchi.pizza_guy_move.PizzaGuyGame;
import baguchi.pizza_guy_move.api.ShadowHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class DashLayer extends RenderLayer<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(PizzaGuyGame.MODID, "textures/entity/dash.png");
    private final DashEffectModel model;

    public DashLayer(EntityRendererProvider.Context context, RenderLayerParent<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> renderer) {
        super(renderer);
        this.model = new DashEffectModel(context.bakeLayer(DashEffectModel.LAYER_LOCATION));
    }

    public void render(PoseStack p_312704_, MultiBufferSource p_312359_, int p_312773_, LivingEntityRenderState p_362521_, float p_312146_, float p_312128_) {
        boolean climb = p_362521_.getRenderDataOrDefault(ClientRegistry.CLIMB, false);
        ShadowHandler shadow = p_362521_.getRenderDataOrDefault(ClientRegistry.SHADOW, null);
        if (shadow != null && shadow.percentBoost > 0.8F && !climb) {
            VertexConsumer vertexconsumer = p_312359_.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(p_362521_.ageInTicks) % 1.0F, 0.0F));
            this.model.setupAnim(p_362521_);
            this.model.renderToBuffer(p_312704_, vertexconsumer, p_312773_, OverlayTexture.NO_OVERLAY);
        }
    }

    private float xOffset(float tickCount) {
        return tickCount * 0.05F;
    }
}
