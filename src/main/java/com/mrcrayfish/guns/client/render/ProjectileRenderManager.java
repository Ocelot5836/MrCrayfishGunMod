package com.mrcrayfish.guns.client.render;

import com.mrcrayfish.guns.client.render.bullet.BulletRender;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ocelot
 */
public final class ProjectileRenderManager
{
    private static final Map<ResourceLocation, BulletRender<?>> RENDERS = new HashMap<>();

    private ProjectileRenderManager()
    {
    }

    public static void registerFactory(Item ammo, BulletRender<?> render)
    {
        RENDERS.put(ammo.getRegistryName(), render);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ProjectileEntity> BulletRender<T> getFactory(ResourceLocation id)
    {
        return (BulletRender<T>) RENDERS.getOrDefault(id, null);
    }
}
