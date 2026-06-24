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
import com.ishland.c2me.opts.accel.opencl.common.workarounds.intel.IntelWorkarounds;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.mesa.MesaWorkarounds;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.nvidia.NvidiaWorkarounds;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Workarounds {

    public static Set<Reference> getWorkarounds(OpenCLDeviceMetadata metadata) {
        EnumSet<Reference> set = EnumSet.noneOf(Reference.class);
        if (IntelWorkarounds.isUsingIntelOnLinux(metadata)) {
            set.add(Reference.INTEL_LINUX_CLEANUP_HANG);
        }
        if (IntelWorkarounds.isUsingIntelOnWindows(metadata)) {
            if (IntelWorkarounds.isUsingGen9OnWindows(metadata)) {
                set.add(Reference.BUILTIN_TRAP_BROKEN);
                set.add(Reference.INTEL_STATIC_PROFILE_GUIDED_TRIMMING_UNAVAILABLE);
            }
            set.add(Reference.INTEL_IGC_OPTS_UNAVAILABLE);
        }
        if (Boolean.getBoolean("com.ishland.c2me.opts.accel.opencl.markBuiltinTrapBroken")) {
            set.add(Reference.BUILTIN_TRAP_BROKEN);
        }
        if (NvidiaWorkarounds.isNvidia(metadata)) {
            set.add(Reference.NVIDIA_INCOMPLETE_CL30_IMPLEMENTATION);
            if (NvidiaWorkarounds.isOlderThanSM50(metadata)) {
                set.add(Reference.NVIDIA_FAST_COMPILE_UNAVAILABLE);
            }
        }
        if (MesaWorkarounds.isRusticl(metadata)) {
            set.add(Reference.REQUIRE_EXPLICIT_FLUSHES);
        }
        return Collections.unmodifiableSet(set);
    }

    public enum Reference {

        /**
         * __builtin_trap(); causes some drivers to crash, such as Intel Gen9 iGPUs on Windows.
         */
        BUILTIN_TRAP_BROKEN,

        /**
         * Nvidia ships incomplete OpenCL 3.0 implementation
         * Stuff broken:
         * - -cl-no-subgroup-ifp being thrown as error
         */
        NVIDIA_INCOMPLETE_CL30_IMPLEMENTATION,

        /**
         * Fast compile is unavailable on older nvidia GPUs
         */
        NVIDIA_FAST_COMPILE_UNAVAILABLE,

        /**
         * The Intel compute driver hangs on cleanup during exit due to JVM running onexit hook with lock
         * and when the driver is releasing the executor, lwjgl tries to acquire the lock causing a deadlock.
         * This intentionally leaks a user event to avoid running the shutdown sequence at the cost of no hotplugging.
         */
        INTEL_LINUX_CLEANUP_HANG,

        /**
         * Disables -igc_opts for Intel Windows drivers
         */
        INTEL_IGC_OPTS_UNAVAILABLE,

        /**
         * Older Intel drivers does not have static profile guided trimming options
         */
        INTEL_STATIC_PROFILE_GUIDED_TRIMMING_UNAVAILABLE,

        /**
         * The driver requires explicit flushes to work
         * TODO do flush consolidation for everything
         */
        REQUIRE_EXPLICIT_FLUSHES,

        ;
    }

}
