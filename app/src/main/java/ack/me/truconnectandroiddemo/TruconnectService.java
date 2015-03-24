/* TruConnect Android Library & Example Applications
*
* Copyright (C) 2015, Sensors.com,  Inc. All Rights Reserved.
*
* The TruConnect Android Library and TruConnect example applications are provided free of charge by
* Sensors.com. The combined source code, and all derivatives, are licensed by Sensors.com SOLELY
* for use with devices manufactured by ACKme Networks, or devices approved by Sensors.com.
*
* Use of this software on any other devices or hardware platforms is strictly prohibited.
*
* THIS SOFTWARE IS PROVIDED BY THE AUTHOR AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
* BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
* PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
* INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
* LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ack.me.truconnectandroiddemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ack.me.truconnectandroid.truconnect.TruconnectCallbacks;
import ack.me.truconnectandroid.truconnect.TruconnectCommand;
import ack.me.truconnectandroid.truconnect.TruconnectErrorCode;
import ack.me.truconnectandroid.truconnect.TruconnectManager;
import ack.me.truconnectandroid.truconnect.TruconnectResult;

public class TruconnectService extends Service
{
    public static final String ACTION_SCAN_RESULT = "ACTION_SCAN_RESULT";
    public static final String ACTION_CONNECTED = "ACTION_CONNECTED";
    public static final String ACTION_DISCONNECTED = "ACTION_DISCONNECTED";
    public static final String ACTION_MODE_WRITE = "ACTION_MODE_WRITE";
    public static final String ACTION_MODE_READ = "ACTION_MODE_READ";
    public static final String ACTION_DATA_WRITE = "ACTION_DATA_WRITE";
    public static final String ACTION_DATA_READ = "ACTION_DATA_READ";
    public static final String ACTION_COMMAND_SENT = "ACTION_COMMAND_SENT";
    public static final String ACTION_COMMAND_RESULT = "ACTION_COMMAND_RESULT";
    public static final String ACTION_ERROR = "ACTION_ERROR";


    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final String EXTRA_COMMAND = "EXTRA_COMMAND";
    public static final String EXTRA_RESPONSE_CODE = "EXTRA_RESPONSE_CODE";
    public static final String EXTRA_ERROR = "EXTRA_ERROR";

    private final String TAG = "TruconnectService";

    private final int mStartMode = START_NOT_STICKY;
    private final IBinder mBinder = new LocalBinder();
    boolean mAllowRebind = true;
    private TruconnectManager mTruconnectManager;

    private TruconnectCallbacks mCallbacks;
    private LocalBroadcastManager mBroadcastManager;

    public class LocalBinder extends Binder
    {
        TruconnectService getService()
        {
            // Return this instance of LocalService so clients can call public methods
            return TruconnectService.this;
        }
    }

    @Override
    public void onCreate()
    {
        // The service is being created
        Log.d(TAG, "Creating service");
        Context context = getApplicationContext();

        mTruconnectManager = new TruconnectManager();
        mBroadcastManager = LocalBroadcastManager.getInstance(context);

        initCallbacks();
        initTruconnectManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // The service is starting, due to a call to startService()
        return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent)
    {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy()
    {
        // The service is no longer used and is being destroyed
        Log.d(TAG, "Destroying service");

        if (mTruconnectManager != null)
        {
            mTruconnectManager.stopScan();
            mTruconnectManager.disconnect();//ensure all connections are terminated
            mTruconnectManager.deinit();
        }
    }

    public TruconnectManager getManager()
    {
        return mTruconnectManager;
    }

    public boolean initTruconnectManager()
    {
        return mTruconnectManager.init(TruconnectService.this, mCallbacks);
    }

    private void initCallbacks()
    {
        mCallbacks = new TruconnectCallbacks()
        {
            @Override
            public void onScanResult(String deviceName)
            {
                Log.d(TAG, "onScanResult");

                Intent intent = new Intent(ACTION_SCAN_RESULT);
                intent.putExtra(EXTRA_DATA, deviceName);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onConnected(String deviceName)
            {
                Log.d(TAG, "onConnected");

                Intent intent = new Intent(ACTION_CONNECTED);
                intent.putExtra(EXTRA_DATA, deviceName);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onDisconnected()
            {
                Log.d(TAG, "onDisconnected");

                Intent intent = new Intent(ACTION_DISCONNECTED);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onModeWritten(int mode)
            {
                Log.d(TAG, "onModeWritten");

                Intent intent = new Intent(ACTION_MODE_WRITE);
                intent.putExtra(EXTRA_MODE, mode);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onModeRead(int mode)
            {
                Log.d(TAG, "onModeRead");

                Intent intent = new Intent(ACTION_MODE_READ);
                intent.putExtra(EXTRA_MODE, mode);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onDataWritten(String data)
            {
                Log.d(TAG, "onDataWritten");

                Intent intent = new Intent(ACTION_DATA_WRITE);
                intent.putExtra(EXTRA_DATA, data);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onDataRead(String data)
            {
                Log.d(TAG, "onDataRead");

                Intent intent = new Intent(ACTION_DATA_READ);
                intent.putExtra(EXTRA_DATA, data);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onCommandSent(TruconnectCommand command)
            {
                Log.d(TAG, "onCommandSent");

                Intent intent = new Intent(ACTION_COMMAND_SENT);
                intent.putExtra(EXTRA_COMMAND, command);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onCommandResult(TruconnectCommand command, TruconnectResult result)
            {
                Log.d(TAG, "onCommandResult");

                Intent intent = new Intent(ACTION_COMMAND_RESULT);
                intent.putExtra(EXTRA_COMMAND, command);

                if (result != null)
                {
                    intent.putExtra(EXTRA_RESPONSE_CODE, result.getResponseCode());
                    intent.putExtra(EXTRA_DATA, result.getData());
                }

                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onError(TruconnectErrorCode error)
            {
                Intent intent = new Intent(ACTION_ERROR);
                intent.putExtra(EXTRA_ERROR, error);
                mBroadcastManager.sendBroadcast(intent);

                Log.d(TAG, "onError - " + error);
            }
        };
    }

    public static int getMode(Intent intent)
    {
        return intent.getIntExtra(EXTRA_MODE, 0);
    }

    public static String getData(Intent intent)
    {
        return intent.getStringExtra(EXTRA_DATA);
    }

    public static TruconnectCommand getCommand(Intent intent)
    {
        return (TruconnectCommand)intent.getSerializableExtra(EXTRA_COMMAND);
    }

    public static int getResponseCode(Intent intent)
    {
        return intent.getIntExtra(EXTRA_RESPONSE_CODE, -1);
    }

    public static TruconnectErrorCode getErrorCode(Intent intent)
    {
        return (TruconnectErrorCode)intent.getSerializableExtra(EXTRA_ERROR);
    }
}