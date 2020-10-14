package com.mrcrayfish.guns.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

/**
 * author: mrcrayfish
 */
public class RenderTypes extends RenderType
{
    private RenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn)
    {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static RenderType getBulletTrail()
    {
        return RenderType.makeType("cgm:projectile_trail", DefaultVertexFormats.POSITION_COLOR, GL_QUADS, 256, false, true, RenderType.State.getBuilder().cull(CULL_DISABLED).alpha(DEFAULT_ALPHA).transparency(TRANSLUCENT_TRANSPARENCY).build(false));
    }
}
