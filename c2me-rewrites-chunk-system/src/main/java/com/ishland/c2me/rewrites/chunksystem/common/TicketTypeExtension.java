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
