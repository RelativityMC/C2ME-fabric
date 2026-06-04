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

package com.ishland.c2me.base.common.client_warning;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class ModExperimentalWarningScreen extends Screen {

    private final Text MESSAGE = Text.literal("Be careful!\nThis version include features that are still under development. Your world might crash, break. Use at your own risk!");
    private final GridWidget grid = new GridWidget().setColumnSpacing(10).setRowSpacing(20);
    private final Runnable onClose;

    public ModExperimentalWarningScreen(Runnable onClose) {
        super(Text.literal("You are running a experimental version of C2ME").setStyle(Style.EMPTY.withBold(true)));
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        super.init();
        GridWidget.Adder adder = this.grid.createAdder(2);
        Positioner positioner = adder.copyPositioner().alignHorizontalCenter();
        adder.add(new TextWidget(this.title, this.textRenderer), 2, positioner);
        MultilineTextWidget multilineTextWidget = adder.add(new MultilineTextWidget(MESSAGE, this.textRenderer).setCentered(true), 2, positioner);
        multilineTextWidget.setMaxWidth(310);
        adder.add(ButtonWidget.builder(ScreenTexts.PROCEED, button -> this.onClose.run()).build(), 2, positioner);
        this.grid.forEachChild(this::addDrawableChild);
        this.grid.refreshPositions();
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        SimplePositioningWidget.setPos(this.grid, 0, 0, this.width, this.height, 0.5F, 0.5F);
    }

    @Override
    public void close() {
        super.close();
        this.onClose.run();
    }
}
