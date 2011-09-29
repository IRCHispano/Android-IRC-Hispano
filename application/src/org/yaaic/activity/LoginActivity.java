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
package org.yaaic.activity;

import org.yaaic.R;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.listener.ServerListener;
import org.yaaic.model.Broadcast;
import org.yaaic.receiver.ServerReceiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

/**
 * List of servers
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class LoginActivity extends Activity implements ServiceConnection, ServerListener {
    private IRCBinder binder;
    private ServerReceiver receiver;
    private static int instanceCount = 0;

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        /*
         * With activity:launchMode = standard, we get duplicated activities
         * depending on the task the app was started in. In order to avoid
         * stacking up of this duplicated activities we keep a count of this
         * root activity and let it finish if it already exists
         * 
         * Launching the app via the notification icon creates a new task,
         * and there doesn't seem to be a way around this so this is needed
         */
        if (instanceCount > 0) {
            finish();
        }
        instanceCount++;
        setContentView(R.layout.login);
    }

    /**
     * On Destroy
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        instanceCount--;
    }

    /**
     * On resume
     */
    @Override
    public void onResume()
    {
        super.onResume();

        // Start and connect to service
        Intent intent = new Intent(this, IRCService.class);
        intent.setAction(IRCService.ACTION_BACKGROUND);
        startService(intent);
        bindService(intent, this, 0);

        receiver = new ServerReceiver(this);
        registerReceiver(receiver, new IntentFilter(Broadcast.SERVER_UPDATE));
    }

    /**
     * On pause
     */
    @Override
    public void onPause()
    {
        super.onPause();

        if (binder != null && binder.getService() != null) {
            binder.getService().checkServiceStatus();
        }

        unbindService(this);
        unregisterReceiver(receiver);
    }

    /**
     * Service connected to Activity
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        binder = (IRCBinder) service;
    }

    /**
     * Service disconnected from Activity
     */
    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        binder = null;
    }

    @Override
    public void onStatusUpdate()
    {
    }
}
