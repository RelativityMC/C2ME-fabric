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

package com.ishland.c2me.opts.accel.opencl.common.workarounds;

import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.amd.AMDBlocklists;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.intel.IntelBlocklists;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.mesa.MesaBlocklists;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.mesa.MesaWorkarounds;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Blocklists {

    public static Set<Reference> getBlockListReasons(OpenCLDeviceMetadata metadata) {
        EnumSet<Reference> set = EnumSet.noneOf(Reference.class);
        if (AMDBlocklists.isUsingBrokenGCNOnOfficialAMDDriver(metadata)) {
            set.add(Reference.BROKEN_DRIVER_AMD_GCN);
        }
        if (AMDBlocklists.isUsingAM5IntegratedGPU(metadata)) {
            set.add(Reference.SLOW_AMD_AM5_IGPU_GFX1036);
        }
        if (IntelBlocklists.isARL_S(metadata)) {
            set.add(Reference.SLOW_INTEL_IGPU_ARL_S);
        }
        if (MesaWorkarounds.isRusticl(metadata) && !MesaBlocklists.isExplicitlyEnabled()) {
            set.add(Reference.RUSTICL_ENABLED_BY_DISTRO);
        }
        return Collections.unmodifiableSet(set);
    }

    public enum Reference {

        /**
         * AMD drivers on GCN is known to crash on compilation
         */
        BROKEN_DRIVER_AMD_GCN,

        /**
         * AMD AM5 iGPU gfx1036 is too slow to be useful
         */
        SLOW_AMD_AM5_IGPU_GFX1036,

        /**
         * Intel ARL-S iGPUs are on the slower side
         */
        SLOW_INTEL_IGPU_ARL_S,

        /**
         * Block distro-enabled rusticl
         */
        RUSTICL_ENABLED_BY_DISTRO,

        ;
    }

}
