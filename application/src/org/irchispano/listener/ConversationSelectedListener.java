/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2011 Sebastian Kaspari

This file is part of Yaaic.

Yaaic is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Yaaic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.irchispano.listener;

import org.irchispano.irc.IRCService;
import org.irchispano.model.Conversation;
import org.irchispano.model.Server;
import org.irchispano.view.ConversationSwitcher;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Listener for conversation selections
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationSelectedListener implements OnItemSelectedListener
{
    private final Context ctx;
    private final Server server;
    private final TextView titleView;
    private final ConversationSwitcher switcher;

    /**
     * Create a new ConversationSelectedListener
     * 
     * @param server
     * @param titleView
     */
    public ConversationSelectedListener(Context ctx, Server server, TextView titleView, ConversationSwitcher switcher)
    {
        this.ctx = ctx;
        this.server = server;
        this.titleView = titleView;
        this.switcher = switcher;
    }

    /**
     * On conversation selected/focused
     */
    @Override
    public void onItemSelected(AdapterView<?> deck, View view, int position, long id)
    {
        Conversation conversation = (Conversation) deck.getItemAtPosition(position);

        if (conversation != null && conversation.getType() != Conversation.TYPE_SERVER) {
            titleView.setText(conversation.getName());
        } else {
            onNothingSelected(deck);
        }

        // Remember selection
        if (conversation != null) {
            Conversation previousConversation = server.getConversation(server.getSelectedConversation());

            if (previousConversation != null) {
                previousConversation.setStatus(Conversation.STATUS_DEFAULT);
            }

            if (conversation.getNewMentions() > 0) {
                Intent i = new Intent(ctx, IRCService.class);
                i.setAction(IRCService.ACTION_ACK_NEW_MENTIONS);
                i.putExtra(IRCService.EXTRA_ACK_SERVERID, server.getId());
                i.putExtra(IRCService.EXTRA_ACK_CONVTITLE, conversation.getName());
                ctx.startService(i);
            }

            conversation.setStatus(Conversation.STATUS_SELECTED);
            server.setSelectedConversation(conversation.getName());
        }

        switcher.invalidate();
    }

    /**
     * On no conversation selected/focused
     */
    @Override
    public void onNothingSelected(AdapterView<?> deck)
    {
        titleView.setText(server.getTitle());
    }
}
