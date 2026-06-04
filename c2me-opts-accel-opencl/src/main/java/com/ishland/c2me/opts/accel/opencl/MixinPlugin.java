/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixinPlugin extends ModuleMixinPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixinPlugin.class);
    private static final boolean DISABLE_FEATURES_SURFACE_RULES = Boolean.getBoolean("c2me.opencl.debug.disable_features_surface_rules");

    static {
        if (DISABLE_FEATURES_SURFACE_RULES) {
            LOGGER.warn("c2me.opencl.debug.disable_features_surface_rules is enabled, features and surface rules will be disabled!");
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;

        if (mixinClassName.startsWith("com.ishland.c2me.opts.accel.opencl.mixin.debug.disable_features_surface_rules."))
            return DISABLE_FEATURES_SURFACE_RULES;

        return true;
    }
}
