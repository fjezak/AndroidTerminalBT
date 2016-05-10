package pl.edu.wat.wel.androidterminal;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;


public class BluetoothFragment extends Fragment {


    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";


    private ListView lvMainChat;
    private EditText etMain;
    private Button btnSend;


    private String connectedDeviceName = null;

    private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    // private ChatService chatService = null;

    private ConnectThread connectThread;

    private static final UUID MY_UUID_SECURE = UUID
            .fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");


    private Intent intent;
    Button button;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Switch switch1;
    EditText editText,editText1;
    TextView textView, textView2;
    BluetoothSocket socket = null;
    PrintWriter out;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;

    private static final int REQUEST_ENABLE_BT = 3;



    ///??????//
    public BluetoothFragment() {
        // Required empty public constructor
    }


   /* private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            textView2.setText(
                                    connectedDeviceName);
                            chatArrayAdapter.clear();
                            break;
                        case ChatService.STATE_CONNECTING:
                            textView2.setText(R.string.title_connecting);
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            textView.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    chatArrayAdapter.add(connectedDeviceName + ":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:

                    connectedDeviceName = msg.getData().getString("Connected to " + DEVICE_NAME);
                    Toast.makeText(getActivity(),
                            connectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getActivity(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }
    });*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        intent = new Intent(getActivity(), Devices.class);
        button = (Button) root.findViewById(R.id.BT_LIST_Button);
        button1 = (Button) root.findViewById(R.id.BT_Discoverable_Button);
        switch1 = (Switch) root.findViewById(R.id.OnOff);
        textView = (TextView) root.findViewById(R.id.switchStatus);
        textView2 = (TextView) root.findViewById(R.id.BT_status);
        button2 = (Button) root.findViewById(R.id.Bt_Connect_Server);
        button3 = (Button) root.findViewById(R.id.Bt_Connect_Client);
        button4 = (Button) root.findViewById(R.id.BT_SEND_button);
        editText =(EditText) root.findViewById(R.id.Bt_Mac_edittext);
        editText1 =(EditText) root.findViewById(R.id.BT_wpisz_editText);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled()) {


                    startActivityForResult(intent, REQUEST_CONNECT_DEVICE_SECURE);

                } else {
                    Toast.makeText(getActivity(), "Turn On Bluetooth", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        ////przycisc bą
        button1.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensureDiscoverable();
                switch1.setChecked(true);
            }
        }));
        //Server
        button2.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new ServerConnectBt().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        }));

        //Client
        button3.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice serwer = ba.getRemoteDevice(editText.getText().toString());
                new ClientConnectBt(serwer).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        }));


        //Send
        button4.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new BtSend().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            }
        }));

        ///początkowa wartosc switcha
        if (!bluetoothAdapter.isEnabled()) {
            switch1.setChecked(false);
        } else {
            switch1.setChecked(true);
        }

        //attach a listener to check for changes in state
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {


                if (isChecked) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                } else {
                    textView.setText("Bluetooth OFF");
                    textView.setTextColor(Color.RED);
                    bluetoothAdapter.disable();
                }


            }
        });

        //check the current state before we display the screen
        if (switch1.isChecked()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            bluetoothAdapter.disable();
            textView.setText("Bluetooth Off");
            textView.setTextColor(Color.RED);

        }


        return root;

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);

                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {

                    textView.setText("Bluetooth On");
                    textView.setTextColor(Color.GREEN);
                } else {
                    switch1.setChecked(false);
                }
                break;


        }
    }


    public void connectDevice(Intent data) {
        String address = data.getExtras().getString(
                Devices.DEVICE_ADDRESS);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        connectThread = new ConnectThread(device);
        connectThread.start();


        Log.d("INFO", "Połączono z serwerem!");
    }
    public void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

    }





    public class ClientConnectBt extends AsyncTask<Void, Void, Void> {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ClientConnectBt(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (Exception e) {
            }
            mmSocket = tmp;
        }



        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                Log.d("INFO", "Próba połączenia....");
                mmSocket.connect();
                Log.d("INFO", "Połączono z serwerem!");
                BufferedReader in = new BufferedReader(new InputStreamReader(mmSocket.getInputStream()));
                String input = in.readLine();


                Log.d("INFO", "Serwer mówi: " + input);

            } catch (Exception ce) {
                try {
                    mmSocket.close();
                } catch (Exception cle) {
                }
            }


            return null;
        }
    }



    public class BtSend extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0){
//            PrintWriter out = null;
//            out = new PrintWriter(socket.getOutputStream(),true);

            try {

                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(editText1);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }



    // runs while attempting to make an outgoing connection
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private String socketType;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;


            try {

                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);

            } catch (IOException e) {
            }
            socket = tmp;
        }

        public void run() {
            setName("ConnectThread" + socketType);

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e2) {
                }

                return;
            }

            // Reset the ConnectThread because we're done


            // Start the connected thread
            // connected(socket, device, socketType);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public class ServerConnectBt extends AsyncTask<Void, Void, Void> {

        private final BluetoothServerSocket mmServerSocket;

        public ServerConnectBt() {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothServerSocket tmp = null;
            try {
                UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Usługa witająca", uuid);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Log.d("INFO", "Uruchamiam serwer");

            while (true) {
                try {
                    Log.d("INFO", "Czekam na połączenie od clienta");
                    socket = mmServerSocket.accept();
                    Log.d("INFO", "Mam clienta!");
                /*Utworzenie strumieni wejściowego i wyjściowego*/


                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String input = in.readLine();


                    Log.d("INFO", "client  mówi: " + input);
                    // BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                } catch (IOException e) {
                    break;
                }
                if (socket != null) {
               /*
                * Jakieś dodatkowe operacje na połączeniu
                * */
                    try {
                        mmServerSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;


                }


            }

            return null;
        }
    }



}


/*

public class BluetoothFragment extends Fragment {



    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private TextView lvMainChat;
    private EditText etMain;
    private Button btnSend;

    private String connectedDeviceName = null;
    private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private ChatService chatService = null;



   private Intent intent;
    private View view;


    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to,
                                    connectedDeviceName));
                            chatArrayAdapter.clear();
                            break;
                        case ChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    chatArrayAdapter.add(connectedDeviceName + ":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:

                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                   // Toast.makeText(getApplicationContext(),
                          //  "Connected to " + connectedDeviceName,
                       //     Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                   // Toast.makeText(getApplicationContext(),
                       //     msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                         //   .show();
                    break;
            }
            return false;
        }


    });


    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        getWidgetReferences();
        bindEventHandler();

        if (bluetoothAdapter == null) {


            Toast.makeText(getActivity(), "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
         //   finish();
            return;
        }


    }





    private void getWidgetReferences() {
        final Button lvMainChat = (TextView) findViewById(R.id.BT_RecData_TextView);


      final  LvMainChat  LvMainChat= (LvMainChat) getView().findViewById(R.id.BT_RecData_TextView);
        etMain = (EditText) findViewById(R.id.BT_wpisz_editText);
        btnSend = (Button) findViewById(R.id.BT_SEND_button);

    }

    private void bindEventHandler() {
        etMain.setOnEditorActionListener(mWriteListener);

        btnSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String message = etMain.getText().toString();
                sendMessage(message);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                  //  finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(
                Devices.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        chatService.connect(device, secure);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                serverIntent = new Intent(getActivity(), Devices.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                serverIntent = new Intent(getActivity(), Devices.class);
                startActivityForResult(serverIntent,
                        REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        if (chatService.getState() != ChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);

            outStringBuffer.setLength(0);
            etMain.setText(outStringBuffer);
        }
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    private final void setStatus(int resId) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private void setupChat() {
        chatArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);
        lvMainChat.setAdapter(chatArrayAdapter);

        chatService = new ChatService(getActivity(), handler);

        outStringBuffer = new StringBuffer("");
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (chatService == null)
                setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (chatService != null) {
            if (chatService.getState() == ChatService.STATE_NONE) {
                chatService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



    View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);
    intent = new Intent(getActivity(), Devices.class);
    final Button button = (Button) root.findViewById(R.id.BT_LIST_Button);

    button.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            startActivity(intent);
        }
    });

    return root;
       // return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

}












*/