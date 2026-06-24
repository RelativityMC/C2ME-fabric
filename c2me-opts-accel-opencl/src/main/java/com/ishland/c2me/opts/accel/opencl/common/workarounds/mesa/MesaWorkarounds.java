package com.ishland.c2me.opts.accel.opencl.common.workarounds.mesa;

import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import org.lwjgl.opencl.CL12;

public class MesaWorkarounds {

    public static boolean isRusticl(OpenCLDeviceMetadata metadata) {
        String platformName = CLUtil.getPlatformInfoStringUTF8(metadata.platformPtr, CL12.CL_PLATFORM_NAME);

        return platformName.trim().equals("rusticl");
    }

}
