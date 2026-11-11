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
