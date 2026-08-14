/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc;

import com.ishland.c2me.base.common.integration.lithostitched.FNLBindings;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;

import java.util.Objects;

public class GenericFastNoiseNode implements AstNode {
    public final AstNode inputX;
    public final AstNode inputY;
    public final AstNode inputZ;
    public final FNLBindings.FNLState state;
    public final Object config;

    public GenericFastNoiseNode(AstNode inputX, AstNode inputY, AstNode inputZ, FNLBindings.FNLState state, Object config) {
        this.inputX = Objects.requireNonNull(inputX);
        this.inputY = Objects.requireNonNull(inputY);
        this.inputZ = Objects.requireNonNull(inputZ);
        this.state = Objects.requireNonNull(state);
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[] { this.inputX, this.inputY, this.inputZ };
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode inputX = this.inputX.transform(transformer);
        AstNode inputY = this.inputY.transform(transformer);
        AstNode inputZ = this.inputZ.transform(transformer);
        if (inputX == this.inputX && inputY == this.inputY && inputZ == this.inputZ) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new GenericFastNoiseNode(inputX, inputY, inputZ, this.state, this.config));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GenericFastNoiseNode that = (GenericFastNoiseNode) o;
        return inputX.equals(that.inputX)
                && inputY.equals(that.inputY)
                && inputZ.equals(that.inputZ)
                && Integer.compare(state.seed(), that.state.seed()) == 0
                && Float.compare(state.frequency(), that.state.frequency()) == 0
                && Integer.compare(state.noise_type(), that.state.noise_type()) == 0
                && Integer.compare(state.rotation_type_3d(), that.state.rotation_type_3d()) == 0
                && Integer.compare(state.fractal_type(), that.state.fractal_type()) == 0
                && Integer.compare(state.octaves(), that.state.octaves()) == 0
                && Float.compare(state.lacunarity(), that.state.lacunarity()) == 0
                && Float.compare(state.gain(), that.state.gain()) == 0
                && Float.compare(state.weighted_strength(), that.state.weighted_strength()) == 0
                && Float.compare(state.ping_pong_strength(),  that.state.ping_pong_strength()) == 0
                && Integer.compare(state.cellular_distance_func(), that.state.cellular_distance_func()) == 0
                && Integer.compare(state.cellular_return_type(),  that.state.cellular_return_type()) == 0
                && Float.compare(state.cellular_jitter_mod(), that.state.cellular_jitter_mod()) == 0
                && Integer.compare(state.domain_warp_type(),  that.state.domain_warp_type()) == 0
                && Float.compare(state.domain_warp_amp(),  that.state.domain_warp_amp()) == 0;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + inputX.hashCode();
        result = 31 * result + inputY.hashCode();
        result = 31 * result + inputZ.hashCode();
        result = 31 * result + Integer.hashCode(state.seed());
        result = 31 * result + Float.hashCode(state.frequency());
        result = 31 * result + Integer.hashCode(state.noise_type());
        result = 31 * result + Integer.hashCode(state.rotation_type_3d());
        result = 31 * result + Integer.hashCode(state.fractal_type());
        result = 31 * result + Integer.hashCode(state.octaves());
        result = 31 * result + Float.hashCode(state.lacunarity());
        result = 31 * result + Float.hashCode(state.gain());
        result = 31 * result + Float.hashCode(state.weighted_strength());
        result = 31 * result + Float.hashCode(state.ping_pong_strength());
        result = 31 * result + Integer.hashCode(state.cellular_distance_func());
        result = 31 * result + Integer.hashCode(state.cellular_return_type());
        result = 31 * result + Float.hashCode(state.cellular_jitter_mod());
        result = 31 * result + Integer.hashCode(state.domain_warp_type());
        result = 31 * result + Float.hashCode(state.domain_warp_amp());
        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (o == null || getClass() != o.getClass()) return false;
        GenericFastNoiseNode that = (GenericFastNoiseNode) o;
        return inputX.relaxedEquals(that.inputX)
                && inputY.relaxedEquals(that.inputY)
                && inputZ.relaxedEquals(that.inputZ)
                && Integer.compare(state.seed(), that.state.seed()) == 0
                && Float.compare(state.frequency(), that.state.frequency()) == 0
                && Integer.compare(state.noise_type(), that.state.noise_type()) == 0
                && Integer.compare(state.rotation_type_3d(), that.state.rotation_type_3d()) == 0
                && Integer.compare(state.fractal_type(), that.state.fractal_type()) == 0
                && Integer.compare(state.octaves(), that.state.octaves()) == 0
                && Float.compare(state.lacunarity(), that.state.lacunarity()) == 0
                && Float.compare(state.gain(), that.state.gain()) == 0
                && Float.compare(state.weighted_strength(), that.state.weighted_strength()) == 0
                && Float.compare(state.ping_pong_strength(),  that.state.ping_pong_strength()) == 0
                && Integer.compare(state.cellular_distance_func(), that.state.cellular_distance_func()) == 0
                && Integer.compare(state.cellular_return_type(),  that.state.cellular_return_type()) == 0
                && Float.compare(state.cellular_jitter_mod(), that.state.cellular_jitter_mod()) == 0
                && Integer.compare(state.domain_warp_type(),  that.state.domain_warp_type()) == 0
                && Float.compare(state.domain_warp_amp(),  that.state.domain_warp_amp()) == 0;
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;
        result = 31 * result + inputX.relaxedHashCode();
        result = 31 * result + inputY.relaxedHashCode();
        result = 31 * result + inputZ.relaxedHashCode();
        result = 31 * result + Integer.hashCode(state.seed());
        result = 31 * result + Float.hashCode(state.frequency());
        result = 31 * result + Integer.hashCode(state.noise_type());
        result = 31 * result + Integer.hashCode(state.rotation_type_3d());
        result = 31 * result + Integer.hashCode(state.fractal_type());
        result = 31 * result + Integer.hashCode(state.octaves());
        result = 31 * result + Float.hashCode(state.lacunarity());
        result = 31 * result + Float.hashCode(state.gain());
        result = 31 * result + Float.hashCode(state.weighted_strength());
        result = 31 * result + Float.hashCode(state.ping_pong_strength());
        result = 31 * result + Integer.hashCode(state.cellular_distance_func());
        result = 31 * result + Integer.hashCode(state.cellular_return_type());
        result = 31 * result + Float.hashCode(state.cellular_jitter_mod());
        result = 31 * result + Integer.hashCode(state.domain_warp_type());
        result = 31 * result + Float.hashCode(state.domain_warp_amp());
        return result;
    }
}
