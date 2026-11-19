/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ishland.c2me.rewrites.chunk_serializer;

import com.ishland.c2me.base.common.config.ConfigSystem;

public final class ModuleEntryPoint {

    @SuppressWarnings("unused")
    public static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.gcFreeChunkSerializer")
            .comment("""
                    EXPERIMENTAL FEATURE
                    This replaces the way your chunks are saved.
                    Please keep regular backups of your world if you are using this feature,
                    and report any world issues you encounter with this feature to our GitHub.
                    
                    Whether to use the fast reduced allocation chunk serializer
                    (may cause incompatibility with other mods)
                    """)
            .incompatibleMod("architectury", "*")
            .getBoolean(false, false);

}
