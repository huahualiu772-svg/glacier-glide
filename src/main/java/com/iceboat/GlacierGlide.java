package com.iceboat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Glacier Glide 主模组类。
 * 功能由 Mixin（com.iceboat.mixin.EntityMixin）实现：
 * 让船能从水面滑上冰面。
 */
@Mod(GlacierGlide.MODID)
public class GlacierGlide {
    public static final String MODID = "iceboat";

    public GlacierGlide(IEventBus modEventBus) {
        // 暂无需要注册的内容，保留空构造用于模组加载
    }
}
