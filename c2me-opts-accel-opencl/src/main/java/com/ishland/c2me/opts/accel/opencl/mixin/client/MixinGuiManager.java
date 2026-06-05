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

package com.ishland.c2me.opts.accel.opencl.mixin.client;

import com.ishland.c2me.opts.accel.opencl.common.progress.GlobalProgressStash;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.GuiManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiManager.class)
public class MixinGuiManager {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDeferredSubtitles()V"))
    private void onRenderSubtitles(CallbackInfo ci, @Local DrawContext drawContext) {
        String progressText = GlobalProgressStash.PROGRESS_TEXT;
        if (progressText == null) {
            return;
        }

        int width = this.client.getWindow().getScaledWidth() - 20;
        Text text = Text.of(progressText);
        drawContext.drawWrappedTextWithShadow(
                this.client.textRenderer,
                text,
                10,
                this.client.getWindow().getScaledHeight() - this.client.textRenderer.getWrappedLinesHeight(text, width) - 10,
                width,
                0xffe0e0e0
        );
    }

}
