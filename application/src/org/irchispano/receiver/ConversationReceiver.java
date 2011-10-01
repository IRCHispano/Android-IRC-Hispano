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
package org.irchispano.receiver;

import org.irchispano.Yaaic;
import org.irchispano.listener.ConversationListener;
import org.irchispano.model.Broadcast;
import org.irchispano.model.Extra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A channel receiver for receiving channel updates
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ConversationReceiver extends BroadcastReceiver
{
    private final ConversationListener listener;
    private final int serverId;

    /**
     * Create a new channel receiver
     * 
     * @param serverId Only listen on channels of this server
     * @param listener
     */
    public ConversationReceiver(int serverId, ConversationListener listener)
    {
        this.listener = listener;
        this.serverId = serverId;
    }

    /**
     * On receive broadcast
     * 
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        int serverId = Yaaic.getInstance().retrieveServerId();
        if (serverId != this.serverId) {
            return;
        }

        String action = intent.getAction();

        if (action.equals(Broadcast.CONVERSATION_MESSAGE)) {
            listener.onConversationMessage(intent.getExtras().getString(Extra.CONVERSATION));
        } else if (action.equals(Broadcast.CONVERSATION_NEW)) {
            listener.onNewConversation(intent.getExtras().getString(Extra.CONVERSATION));
        } else if (action.equals(Broadcast.CONVERSATION_REMOVE)) {
            listener.onRemoveConversation(intent.getExtras().getString(Extra.CONVERSATION));
        } else if (action.equals(Broadcast.CONVERSATION_TOPIC)) {
            listener.onTopicChanged(intent.getExtras().getString(Extra.CONVERSATION));
        }

    }
}
