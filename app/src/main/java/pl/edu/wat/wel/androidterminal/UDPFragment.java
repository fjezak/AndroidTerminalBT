package pl.edu.wat.wel.androidterminal;

/**
 * Created by Domin on 2016-04-06.
 */
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class UDPFragment extends Fragment{

    public Button btnSend;
    public ToggleButton btnListen;
    EditText editTextPort, editTextLocalPort;
    String addressIP, sendTextEdit;
    int port, portLocal;
    TextView textRx;

    DatagramSocket datagramSocket, datagramSocketReceive; InetAddress receiverAddress;

    public UDPFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_udp, container, false);
        btnListen = (ToggleButton)root.findViewById(R.id.UDP_Listen_Button);
        btnSend = (Button)root.findViewById(R.id.UDP_Send_button);
        textRx = (TextView) root.findViewById(R.id.UDP_RecDataWy_TextView);

        btnListen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    addressIP = ((EditText) root.findViewById(R.id.UDP_IP_EditText)).getText().toString();
                    editTextPort = ((EditText) root.findViewById(R.id.UDP_Port_EditText));
                    editTextLocalPort = ((EditText) root.findViewById(R.id.UDP_Local_Port_EditText));

                    port = Integer.parseInt(editTextPort.getText().toString());
                    portLocal = Integer.parseInt(editTextLocalPort.getText().toString());

                    new UDPListenTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new UDPReceiveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    //UDPReceiveThread UDPRT = new UDPReceiveThread("odbierz");

                } else {
                    // The toggle is disabled
                    if(datagramSocket != null) {
                        datagramSocket.close();
                        datagramSocketReceive.close();
                    }
                }
            }
        });


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextEdit = ((EditText) root.findViewById(R.id.UDP_Wpisz_editText)).getText().toString();

                new UDPSendTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_udp, container, false);
        return root;
    }

    private class UDPListenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {

            datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }

            receiverAddress = null;
            try {
                receiverAddress = InetAddress.getByName(addressIP);

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    String dataBank = "";

    private class UDPReceiveTask extends AsyncTask<Void, String, Void> {
        String receivedData = "";

        @Override
        protected Void doInBackground(Void... arg0) {
            datagramSocketReceive=null;
            try {
            datagramSocketReceive = new DatagramSocket(portLocal);

                byte[] receivedBuffer = new byte[256];
            DatagramPacket packetReceived = new DatagramPacket(receivedBuffer, 256);

                while(true) {
                    datagramSocketReceive.receive(packetReceived);

                    receivedData = new String(packetReceived.getData(), 0, packetReceived.getLength());
                    publishProgress(receivedData);
                }

        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dataBank = dataBank + values[0];
            textRx.setText(dataBank);
            super.onProgressUpdate(values);
        }
    }

    private class UDPSendTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {

            DatagramPacket packet = new DatagramPacket(sendTextEdit.getBytes(), sendTextEdit.length(), receiverAddress, port);
            try {
                if (datagramSocket != null) {
                    datagramSocket.send(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


//    String dataBank = "";
//    private class UDPReceiveThread implements Runnable {
//        Thread thread;
//        String receivedData = "";
//        UDPReceiveThread(String name) {
//            thread = new Thread(this, name);
//            thread.start();
//        }
//        @Override
//        public void run() {
//            datagramSocketReceive=null;
//            try {
//                datagramSocketReceive = new DatagramSocket(portLocal);
//
//                byte[] receivedBuffer = new byte[256];
//                DatagramPacket packetReceived = new DatagramPacket(receivedBuffer, 256);
//
//                while (true) {
//                    datagramSocketReceive.receive(packetReceived);
//
//                    receivedData = new String(packetReceived.getData(), 0, packetReceived.getLength());
//                    //Arrays.toString(packetReceived.getData());
//                    dataBank = dataBank + receivedData;
//                    textRx.setText(dataBank);
//                }
//            } catch (NumberFormatException | IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
