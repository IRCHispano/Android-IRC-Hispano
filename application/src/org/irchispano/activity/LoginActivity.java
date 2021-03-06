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
package org.irchispano.activity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.irchispano.R;
import org.irchispano.Yaaic;
import org.irchispano.db.Database;
import org.irchispano.irc.IRCBinder;
import org.irchispano.irc.IRCService;
import org.irchispano.model.Authentication;
import org.irchispano.model.Identity;
import org.irchispano.model.Server;
import org.irchispano.model.Status;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * List of servers
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class LoginActivity extends Activity implements ServiceConnection {
    private IRCBinder binder;
    private final Pattern nicknamePattern = Pattern.compile("^[-0-9a-z_]{1,30}$", Pattern.CASE_INSENSITIVE);

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Yaaic.getInstance().loadServers(this);
        setContentView(R.layout.login);
        if (!tryToConnect()) {
            final Activity activity = this;
            final Button login = (Button) findViewById(R.id.login);
            login.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final EditText username = (EditText) findViewById(R.id.nickname);
                    final EditText password = (EditText) findViewById(R.id.authentication);
                    final EditText realname_ = (EditText) findViewById(R.id.realname);

                    Matcher nicknameMatcher = nicknamePattern.matcher(username.getText().toString());
                    if (!nicknameMatcher.find()) {
                        Toast.makeText(activity, getString(R.string.invalid_nickname), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Database db = new Database(activity);

                    String realname = realname_.getText().toString();
                    if (realname.equals("")) {
                        realname = "android";
                    }

                    // Identity
                    final int identity = (int) db.addIdentity(username.getText().toString(), "android", realname, new ArrayList<String>());
                    final Identity i = db.getIdentityById(identity);

                    // Authentication
                    final String passwordStr = password.getText().toString();
                    Authentication a = new Authentication();
                    if (!passwordStr.equals("")) {
                        a.setNickservPassword(passwordStr);
                    }

                    // Server
                    Server s = new Server();
                    s.setAuthentication(a);
                    s.setHost("irc.irc-hispano.org");
                    s.setIdentity(i);
                    s.setPort(6667);
                    s.setCharset("ISO-8859-15");
                    s.setTitle("IRC-Hispano");
                    s.setMayReconnect(true);
                    db.addServer(s, identity);

                    db.close();

                    tryToConnect();
                }
            });
        }
    }

    private boolean tryToConnect()
    {
        Yaaic.getInstance().loadServers(this);
        ArrayList<Server> servers = Yaaic.getInstance().getServersAsArrayList();

        Server server = null;
        for (Server server_ : servers) {
            server = server_;
        }

        if (server == null) {
            return false;
        }

        Intent intent = new Intent(this, ConversationActivity.class);

        if (server.getStatus() == Status.DISCONNECTED && !server.mayReconnect()) {
            server.setStatus(Status.PRE_CONNECTING);
            intent.putExtra("connect", true);
        }

        startActivity(intent);

        return true;
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
    }

    /**
     * Service connected to Activity
     */
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        binder = (IRCBinder) service;
    }

    /**
     * Service disconnected from Activity
     */
    public void onServiceDisconnected(ComponentName name)
    {
        binder = null;
    }
}
