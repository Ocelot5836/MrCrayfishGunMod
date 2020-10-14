package com.mrcrayfish.guns.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrcrayfish.guns.client.RenderTypes;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.object.Bullet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class BulletRenderer
{
    private final List<Bullet> bullets = new ArrayList<>();

    public void addBullet(Bullet bullet)
    {
        this.bullets.add(bullet);
    }

    @SubscribeEvent
    public void onTickBullets(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().world != null && event.phase == TickEvent.Phase.END)
        {
            this.bullets.forEach(bullet -> bullet.tick(Minecraft.getInstance().world));
            this.bullets.removeIf(Bullet::isFinished);
        }
    }

    @SubscribeEvent
    public void onRenderBullets(RenderWorldLastEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null)
            return;

        boolean old = ((PlayerEntity) entity).inventory.currentItem == 0;
        if (old)
        {
            for (Bullet bullet : this.bullets)
            {
                this.renderBulletOld(bullet, event.getMatrixStack(), event.getPartialTicks());
            }
        }
        else
        {
            IRenderTypeBuffer.Impl buffer = mc.getRenderTypeBuffers().getBufferSource();
            MatrixStack matrixStack = event.getMatrixStack();
            float partialTicks = event.getPartialTicks();
            Vector3d view = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

            matrixStack.push();
            matrixStack.translate(-view.getX(), -view.getY(), -view.getZ());

            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (Bullet bullet : this.bullets)
            {
                if (bullet.isFinished() || bullet.getProjectile() == null)
                    continue;
                this.renderBullet(bullet, pos, buffer, matrixStack, partialTicks);
            }

            for (Bullet bullet : this.bullets)
            {
                if (bullet.isFinished() || bullet.getProjectile() == null || bullet.getProjectile().getShooterId() == entity.getEntityId())
                    continue;
                this.renderTrail(bullet, buffer, matrixStack, partialTicks);
            }

            matrixStack.pop();

            buffer.finish();
        }
    }

    private void renderBullet(Bullet bullet, BlockPos.Mutable pos, IRenderTypeBuffer buffer, MatrixStack matrixStack, float partialTicks)
    {
        matrixStack.push();

        double bulletX = bullet.getRenderX(partialTicks);
        double bulletY = bullet.getRenderY(partialTicks);
        double bulletZ = bullet.getRenderZ(partialTicks);
        matrixStack.translate(bulletX, bulletY, bulletZ);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(bullet.getRotationYaw()));
        matrixStack.rotate(Vector3f.XN.rotationDegrees(bullet.getRotationPitch() - 90));

        matrixStack.rotate(Vector3f.YP.rotationDegrees((bullet.getProjectile().ticksExisted + partialTicks) * 50));
        matrixStack.scale(0.275F, 0.275F, 0.275F);

        RenderUtil.renderModel(bullet.getProjectile().getItem(), ItemCameraTransforms.TransformType.NONE, matrixStack, buffer, WorldRenderer.getCombinedLight(bullet.getProjectile().world, pos.setPos(bulletX, bulletY, bulletZ)), OverlayTexture.NO_OVERLAY);

        matrixStack.pop();
    }

    private void renderTrail(Bullet bullet, IRenderTypeBuffer buffer, MatrixStack matrixStack, float partialTicks)
    {
        matrixStack.push();

        matrixStack.translate(bullet.getRenderX(partialTicks), bullet.getRenderY(partialTicks), bullet.getRenderZ(partialTicks));
        matrixStack.rotate(Vector3f.YP.rotationDegrees(bullet.getRotationYaw()));
        matrixStack.rotate(Vector3f.XN.rotationDegrees(bullet.getRotationPitch() - 90));

        Vector3d motionVec = new Vector3d(bullet.getMotionX(), bullet.getMotionY(), bullet.getMotionZ());
        float trailLength = (float) ((motionVec.length() / 3.0F) * bullet.getTrailLengthMultiplier());
        float red = (float) ((bullet.getTrailColor() >> 16) & 255) / 255.0F;
        float green = (float) ((bullet.getTrailColor() >> 8) & 255) / 255.0F;
        float blue = (float) (bullet.getTrailColor() & 255) / 255.0F;
        float alpha = 0.3F;

        IVertexBuilder builder = buffer.getBuffer(RenderTypes.getBulletTrail());
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        builder.pos(matrix4f, 0, 0, -0.035F).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix4f, 0, 0, 0.035F).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix4f, 0, -trailLength, 0.035F).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix4f, 0, -trailLength, -0.035F).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix4f, -0.035F, 0, 0).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix4f, 0.035F, 0, 0).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix4f, 0.035F, -trailLength, 0).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix4f, -0.035F, -trailLength, 0).color(red, green, blue, alpha).endVertex();

        matrixStack.pop();
    }

    private void renderBulletOld(Bullet bullet, MatrixStack matrixStack, float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null || bullet.isFinished() || bullet.getProjectile() == null)
            return;

        matrixStack.push();

        Vector3d view = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
        double bulletX = bullet.getPosX() + bullet.getMotionX() * partialTicks;
        double bulletY = bullet.getPosY() + bullet.getMotionY() * partialTicks;
        double bulletZ = bullet.getPosZ() + bullet.getMotionZ() * partialTicks;
        matrixStack.translate(bulletX - view.getX(), bulletY - view.getY(), bulletZ - view.getZ());

        matrixStack.rotate(Vector3f.YP.rotationDegrees(bullet.getRotationYaw()));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(-bullet.getRotationPitch() + 90));

        Vector3d motionVec = new Vector3d(bullet.getMotionX(), bullet.getMotionY(), bullet.getMotionZ());
        float trailLength = (float) ((motionVec.length() / 3.0F) * bullet.getTrailLengthMultiplier());
        float red = (float) (bullet.getTrailColor() >> 16 & 255) / 255.0F;
        float green = (float) (bullet.getTrailColor() >> 8 & 255) / 255.0F;
        float blue = (float) (bullet.getTrailColor() & 255) / 255.0F;
        float alpha = 0.3F;

        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        IRenderTypeBuffer.Impl renderTypeBuffer = mc.getRenderTypeBuffers().getBufferSource();

        if (bullet.getProjectile().getShooterId() != entity.getEntityId())
        {
            RenderType bulletType = RenderTypes.getBulletTrail();
            IVertexBuilder builder = renderTypeBuffer.getBuffer(bulletType);
            builder.pos(matrix4f, 0, 0, -0.035F).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, 0, 0, 0.035F).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, 0, -trailLength, 0.035F).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, 0, -trailLength, -0.035F).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, -0.035F, 0, 0).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, 0.035F, 0, 0).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, 0.035F, -trailLength, 0).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, -0.035F, -trailLength, 0).color(red, green, blue, alpha).endVertex();
            Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(bulletType);
        }

        // No point rendering item if empty, so return
        if (bullet.getProjectile().getItem().isEmpty())
        {
            matrixStack.pop();
            return;
        }

        matrixStack.rotate(Vector3f.YP.rotationDegrees((bullet.getProjectile().ticksExisted + partialTicks) * (float) 50));
        matrixStack.scale(0.275F, 0.275F, 0.275F);

        int combinedLight = WorldRenderer.getCombinedLight(entity.world, new BlockPos(entity.getPositionVec()));
        ItemStack stack = bullet.getProjectile().getItem();
        RenderType renderType = RenderTypeLookup.func_239219_a_(stack, false);
        RenderUtil.renderModel(stack, ItemCameraTransforms.TransformType.NONE, matrixStack, renderTypeBuffer, combinedLight, OverlayTexture.NO_OVERLAY);
        Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(renderType);

        matrixStack.pop();
    }
}