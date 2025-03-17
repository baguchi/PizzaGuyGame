package baguchi.pizza_guy_move.client;

import baguchi.pizza_guy_move.PizzaGuyGame;
import baguchi.pizza_guy_move.api.ShadowHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords;

@EventBusSubscriber(modid = PizzaGuyGame.MODID, value = Dist.CLIENT)
public class ClientEvents<E extends Entity, S extends EntityRenderState> {

    @SubscribeEvent
    public static void renderEvent(RenderLivingEvent.Post<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>> event) {
        MultiBufferSource buffer = event.getMultiBufferSource();
        LivingEntityRenderState entity = event.getRenderState();
        LivingEntityRenderer<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>> renderer = event.getRenderer();
        PoseStack posestack = event.getPoseStack();
        int light = event.getPackedLight();
        float partialtick = event.getPartialTick();


        ShadowHandler shadow = entity.getRenderData(ClientRegistry.SHADOW);

        if (shadow != null && shadow.getPercentBoost() >= 0.8F) {

            double shadowX = (shadow.getPrevShadow().x + (shadow.getShadow().x - shadow.getPrevShadow().x) * partialtick);
            double shadowY = (shadow.getPrevShadow().y + (shadow.getShadow().y - shadow.getPrevShadow().y) * partialtick);
            double shadowZ = (shadow.getPrevShadow().z + (shadow.getShadow().z - shadow.getPrevShadow().z) * partialtick);
            double shadowX2 = (shadow.getPrevShadow2().x + (shadow.getShadow2().x - shadow.getPrevShadow2().x) * partialtick);
            double shadowY2 = (shadow.getPrevShadow2().y + (shadow.getShadow2().y - shadow.getPrevShadow2().y) * partialtick);
            double shadowZ2 = (shadow.getPrevShadow2().z + (shadow.getShadow2().z - shadow.getPrevShadow2().z) * partialtick);
            double ownerInX = entity.x;
            double ownerInY = entity.y;
            double ownerInZ = entity.z;
            double deltaX = shadowX - ownerInX;
            double deltaY = shadowY - ownerInY;
            double deltaZ = shadowZ - ownerInZ;
            double deltaX2 = shadowX2 - shadowX;
            double deltaY2 = shadowY2 - shadowY;
            double deltaZ2 = shadowZ2 - shadowZ;
            posestack.pushPose();

            posestack.translate(deltaX, deltaY, deltaZ);
            setupRender(entity, renderer, posestack, buffer, light);
            posestack.popPose();
            posestack.pushPose();

            posestack.translate(deltaX2, deltaY2, deltaZ2);
            setupRender(entity, renderer, posestack, buffer, light);

            posestack.popPose();
        }
    }

    private static void setupRender(LivingEntityRenderState entity, LivingEntityRenderer<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>> renderer, PoseStack posestack, MultiBufferSource buffer, int light) {
        if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.bedOrientation;
            if (direction != null) {
                float f = entity.eyeHeight - 0.1F;
                posestack.translate((float) (-direction.getStepX()) * f, 0.0F, (float) (-direction.getStepZ()) * f);
            }
        }

        float f1 = entity.scale;
        posestack.scale(f1, f1, f1);
        setupRotations(renderer, entity, posestack, entity.bodyRot, f1);
        posestack.scale(-1.0F, -1.0F, 1.0F);
        //renderer.scale(entity, posestack);
        posestack.translate(0.0F, -1.501F, 0.0F);
        renderer.getModel().setupAnim(entity);
        RenderType rendertype = RenderType.entityTranslucent(renderer.getTextureLocation(entity));
        if (rendertype != null) {
            VertexConsumer vertexconsumer = buffer.getBuffer(rendertype);
            int i = getOverlayCoords(entity, 0.0F);
            int j = 654311423;
            int k = ARGB.multiply(j, -1);
            renderer.getModel().renderToBuffer(posestack, vertexconsumer, light, i, k);
        }
    }

    private static void setupRotations(LivingEntityRenderer<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>> renderer, LivingEntityRenderState entity, PoseStack poseStack, float bodyRot, float scale) {

        if (!entity.hasPose(Pose.SLEEPING)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
        }

        /*if (entity.deathTime > 0.0F) {
            float f = (entity.deathTime - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            poseStack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees()));
        } else*/
        if (entity.isAutoSpinAttack) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - entity.xRot));
            poseStack.mulPose(Axis.YP.rotationDegrees(entity.ageInTicks * -75.0F));
        } else /*if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.bedOrientation;
            float f1 = direction != null ? sleepDirectionToRotation(direction) : bodyRot;
            poseStack.mulPose(Axis.YP.rotationDegrees(f1));
            poseStack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees()));
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        } else*/ if (entity.isUpsideDown) {
            poseStack.translate(0.0F, (entity.boundingBoxHeight + 0.1F) / scale, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    protected static float getBob(LivingEntityRenderState p_115305_, float p_115306_) {
        return (float) p_115305_.ageInTicks;
    }

}