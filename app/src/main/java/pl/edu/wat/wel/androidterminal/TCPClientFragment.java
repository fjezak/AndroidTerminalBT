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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class TCPClientFragment extends Fragment{


    ToggleButton btnConnect;
    Button btnSend;
    EditText editTextPort;
    String addressIP, sendTextEdit;
    Socket socket, socketReceive;
    TextView dataReceived;

    int port;

    String line;

    public TCPClientFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_tcpclient, container, false);
        dataReceived = (TextView) root.findViewById(R.id.TCPC_RecDataWy_TextView);
        btnConnect = (ToggleButton) root.findViewById(R.id.TCPC_Connect_Button);
        btnSend=(Button) root.findViewById(R.id.TCPC_Send_button);

        btnConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    addressIP = ((EditText) root.findViewById(R.id.TCPC_IP_EditText)).getText().toString();
                    editTextPort = ((EditText) root.findViewById(R.id.TCPC_Port_EditText));
                    port = Integer.parseInt(editTextPort.getText().toString());

                    new TCPConnect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new TCPReceiveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                  //  new TCPRead().execute();

                } else {
                    // The toggle is disabled
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextEdit = ((EditText) root.findViewById(R.id.TCPC_Wpisz_editText)).getText().toString();
                new TCPSend().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        // Inflate the layout for this fragment
        return root;
    }


    private class TCPConnect extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0){
            try {
                socket = new Socket(addressIP, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class TCPSend extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0){
//            PrintWriter out = null;
//            out = new PrintWriter(socket.getOutputStream(),true);

            OutputStream out = null;
            try {
                out = socket.getOutputStream();
                out.write(sendTextEdit.getBytes());
                out.flush();
               // out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    String dataBank = "";

    private class TCPReceiveTask extends AsyncTask<Void, String, Void> {
        String receivedData = "";
        InputStream in = null;
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                socketReceive = new Socket(addressIP, port);
                while(true) {
                    in = socketReceive.getInputStream();


                    String receivedData;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));



                    while((receivedData = bufferedReader.readLine().toString()) != null){

                    }

                    publishProgress(receivedData);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dataBank = dataBank + values[0];
            dataReceived.setText(dataBank);
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Void... arg0) {
            try {
                in.close();
                socketReceive.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //    private class TCPRead extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... params){
//            String res = null;
//            try{
//                InputStream is = socket.getInputStream();
//                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                res = br.readLine();
//
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return res;
//        }
//
//        protected void onPostExecute(String s) {
//            //Write server message to the text view
//            dataSer.setText(s);
//        }
//    }

}
