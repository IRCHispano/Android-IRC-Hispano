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
package org.irchispano.command.handler;

import org.irchispano.R;
import org.irchispano.command.BaseHandler;
import org.irchispano.exception.CommandException;
import org.irchispano.irc.IRCService;
import org.irchispano.model.Broadcast;
import org.irchispano.model.Conversation;
import org.irchispano.model.Message;
import org.irchispano.model.Server;

import android.content.Context;
import android.content.Intent;

/**
 * Command: /me <action>
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class MeHandler extends BaseHandler
{
    /**
     * Execute /me
     */
    @Override
    public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException
    {
        if (conversation.getType() == Conversation.TYPE_SERVER) {
            throw new CommandException(service.getString(R.string.only_usable_from_channel_or_query));
        }

        if (params.length > 1) {
            String action = BaseHandler.mergeParams(params);
            String nickname = service.getConnection(server.getId()).getNick();

            Message message = new Message(nickname + " " + action);
            message.setIcon(R.drawable.action);
            server.getConversation(conversation.getName()).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                server.getId(),
                conversation.getName()
            );
            service.sendBroadcast(intent);

            service.getConnection(server.getId()).sendAction(conversation.getName(), action);
        } else {
            throw new CommandException(service.getString(R.string.text_missing));
        }
    }

    /**
     * Usage of /me
     */
    @Override
    public String getUsage()
    {
        return "/me <text>";
    }

    /**
     * Description of /me
     */
    @Override
    public String getDescription(Context context)
    {
        return context.getString(R.string.command_desc_me);
    }
}
