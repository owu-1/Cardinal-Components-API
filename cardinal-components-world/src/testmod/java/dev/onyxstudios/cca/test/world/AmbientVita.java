/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 OnyxStudios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.onyxstudios.cca.test.world;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.test.base.BaseVita;
import dev.onyxstudios.cca.test.base.CardinalGameTest;
import dev.onyxstudios.cca.test.base.Vita;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.Objects;

public abstract class AmbientVita extends BaseVita implements AutoSyncedComponent {

    public abstract void syncWithAll(MinecraftServer server);

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int vita = buf.readInt();
        this.setVitality(vita);
        World world = Objects.requireNonNull(MinecraftClient.getInstance().player).getWorld();
        // Very bad shortcut to get a dimension's name
        Text worldName = Text.literal(
            Objects.requireNonNull(world.getRegistryKey() == World.OVERWORLD ? "Overworld" : "Alien World")
        );
        Text worldVita = Text.translatable(
                "componenttest:title.world_vitality",
                Vita.get(world).getVitality(),
                Vita.get(world.getLevelProperties()).getVitality()
        );
        InGameHud inGameHud = MinecraftClient.getInstance().inGameHud;
        inGameHud.setTitleTicks(-1, -1, -1);
        inGameHud.setTitle(worldName);
        inGameHud.setSubtitle(worldVita);
    }

    /**
     * proper implementation of {@code writeToPacket}, writes a single int instead of a whole tag
     */
    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity player) {
        buf.writeInt(this.getVitality());
    }

    public static class WorldVita extends AmbientVita implements ClientTickingComponent {
        private final World world;

        public WorldVita(World world) {
            this.world = world;
        }

        @Override
        public void syncWithAll(MinecraftServer server) {
            this.world.syncComponent(KEY);
        }

        @Override
        public void clientTick() {
            if (this.world.getTime() % 2400 == 0) {
                CardinalGameTest.LOGGER.info("The world still runs, and is now worth {}", this.vitality);
            }
        }
    }

}
