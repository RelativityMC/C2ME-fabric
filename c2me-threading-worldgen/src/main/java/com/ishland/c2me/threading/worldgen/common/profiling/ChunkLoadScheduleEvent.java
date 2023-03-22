package com.ishland.c2me.threading.worldgen.common.profiling;

import jdk.jfr.Category;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

@Name("minecraft.ChunkLoadSchedule")
@Label("Chunk Load Scheduling")
@Category({"Minecraft", "Chunk Loading"})
@StackTrace(false)
@Enabled(false)
public class ChunkLoadScheduleEvent extends Event {

    public static final EventType TYPE = EventType.getEventType(ChunkLoadScheduleEvent.class);

    @Name("worldPosX")
    @Label("First Block X World Position")
    public final int worldPosX;
    @Name("worldPosZ")
    @Label("First Block Z World Position")
    public final int worldPosZ;
    @Name("chunkPosX")
    @Label("Chunk X Position")
    public final int chunkPosX;
    @Name("chunkPosZ")
    @Label("Chunk Z Position")
    public final int chunkPosZ;
    @Name("status")
    @Label("Status")
    public final String targetStatus;
    @Name("level")
    @Label("Level")
    public final String level;

    public ChunkLoadScheduleEvent(ChunkPos chunkPos, RegistryKey<World> world, String targetStatus) {
        this.targetStatus = targetStatus;
        this.level = world.toString();
        this.chunkPosX = chunkPos.x;
        this.chunkPosZ = chunkPos.z;
        this.worldPosX = chunkPos.getStartX();
        this.worldPosZ = chunkPos.getStartZ();
    }
}
