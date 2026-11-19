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

package com.ishland.c2me.opts.dfc.common.ast.noise;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.Objects;

public class DFTWeirdScaledSamplerNode implements AstNode {

    public final AstNode input;
    public final DensityFunction.Noise noise;
    public final DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper mapper;

    public DFTWeirdScaledSamplerNode(AstNode input, DensityFunction.Noise noise, DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper mapper) {
        this.input = Objects.requireNonNull(input);
        this.noise = Objects.requireNonNull(noise);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.input};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode input = this.input.transform(transformer);
        if (input == this.input) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new DFTWeirdScaledSamplerNode(input, this.noise, this.mapper));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFTWeirdScaledSamplerNode that = (DFTWeirdScaledSamplerNode) o;
        return Objects.equals(input, that.input) && Objects.equals(noise, that.noise) && mapper == that.mapper;
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + input.hashCode();
        result = 31 * result + noise.hashCode();
        result = 31 * result + mapper.hashCode();

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFTWeirdScaledSamplerNode that = (DFTWeirdScaledSamplerNode) o;
        return input.relaxedEquals(that.input) && Objects.equals(noise, that.noise) && mapper == that.mapper;
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + input.relaxedHashCode();
        result = 31 * result + noise.hashCode();
        result = 31 * result + mapper.hashCode();

        return result;
    }
}
