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

package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.flowsched.scheduler.ItemTicket;

public class TicketTypeExtension {

    /**
     * Ticket type for vanilla level loading
     */
    public static final ItemTicket.TicketType VANILLA_LEVEL = new ItemTicket.TicketType("c2me:vanilla_level");
    /**
     * Ticket type for vanilla deferred level loading
     */
    public static final ItemTicket.TicketType VANILLA_DEFERRED_LOAD = new ItemTicket.TicketType("c2me:vanilla_deferred_load");
    /**
     * Ticket type for on-demand load off-thread
     */
    public static final ItemTicket.TicketType ONDEMAND_LOAD = new ItemTicket.TicketType("c2me:ondemand_load");
    /**
     * Ticket type for lighting
     */
    public static final ItemTicket.TicketType LIGHT_TICKET = new ItemTicket.TicketType("c2me:light_ticket");

    private TicketTypeExtension() {
    }

}
