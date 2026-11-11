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

package com.ishland.c2me.tests.testmod.mixin.pregen;

import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.Main;
import net.minecraft.server.dedicated.EulaReader;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Main.class)
public class MixinMain {

    @Shadow @Final private static Logger LOGGER;

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/EulaReader;isEulaAgreedTo()Z"))
    private static boolean redirectEULA(EulaReader eulaReader) {
        LOGGER.info("Automatically agreed to EULA. If you don't, please stop using this test suite.");
        return true;
    }

    @Redirect(method = "createServerConfig", at = @At(value = "NEW", target = "(Lnet/minecraft/resource/DataPackSettings;Lnet/minecraft/resource/featuretoggle/FeatureSet;)Lnet/minecraft/resource/DataConfiguration;"))
    private static DataConfiguration enableAllFeatures(DataPackSettings dataPackSettings, FeatureSet featureSet) {
        return new DataConfiguration(dataPackSettings, FeatureFlags.VANILLA_FEATURES);
    }

}
