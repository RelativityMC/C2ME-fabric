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

package com.ishland.c2me.opts.natives_math.common.ducks;

import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.world.gen.sampler.SampleBuffer;

public interface ShiftedBatchingCapableNoise {

    void c2me$fillShifted(final SampleBuffer buf, final SamplingRegion region,
                          final double preScaleXz, final double preScaleY,
                          final SampleBuffer shiftX, final SampleBuffer shiftY, final SampleBuffer shiftZ,
                          final double scaleXz, final double scaleY, final float outputScale);

}
