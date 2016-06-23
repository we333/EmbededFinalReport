package com.example.we.connecttopi_socket;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity
{
    Button btnBuzOn, btnBuzOff, btnDoorStatus;
    CheckBox monitorEnable;
    TextView res;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBuzOn = (Button)findViewById(R.id.btn_buz_on);
        btnBuzOff = (Button)findViewById(R.id.btn_buz_off);
        btnDoorStatus = (Button)findViewById(R.id.btn_door_status);
        monitorEnable = (CheckBox)findViewById(R.id.checkBox);
        res = (TextView) findViewById(R.id.textView);

        handler.post(task);     // action loop for checking DoorStatus

        btnBuzOn.setOnClickListener(listener);
        btnBuzOff.setOnClickListener(listener);
        btnDoorStatus.setOnClickListener(listener);
    }

    // action background loop for checking DoorStatus
    private Handler handler = new Handler();
    private Runnable task =new Runnable()
    {
        public void run()
        {
            handler.postDelayed(this,2000); // check DoorStatus each 2s
            if(monitorEnable.isChecked())
                new DoorMonitor().execute();
        }
    };

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if(v.getId() == R.id.btn_buz_on)
            {
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ClientCallServer("A");
                    }
                })).start();
            }
            else if(v.getId() == R.id.btn_buz_off)
            {
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ClientCallServer("B");
                    }
                })).start();
            }
            else if(v.getId() == R.id.btn_door_status)
            {
                new CheckDoorStatus().execute();
            }
            else ;
        }
    };

    // Extends AsyncTask because using MainThread UI Control
    private class DoorMonitor extends AsyncTask<String, Void, Integer>
    {
        protected Integer doInBackground(String... urls)
        {
            return ClientCallServer("C");
        }
        protected void onPostExecute(Integer sum)
        {
            if(sum == 1)
            {
                res.setText("Opeing");
                //     Intent intent = new Intent(getApplicationContext(), DoorOpening.class);
                //     startActivity(intent);
            }
            else
            {
                res.setText("Closing");
                //        Intent intent = new Intent(getApplicationContext(), DoorClosing.class);
                //        startActivity(intent);
            }
        }
    }

    // Extends AsyncTask because using MainThread UI Control
    private class CheckDoorStatus extends AsyncTask<String, Void, Integer>
    {
        protected Integer doInBackground(String... urls)
        {
            return ClientCallServer("C");
        }
        protected void onPostExecute(Integer sum)
        {
            if(sum == 1)
            {
                res.setText("Opeing");
                //    Intent intent = new Intent(getApplicationContext(), DoorOpening.class);
                //    startActivity(intent);
            }
            else
            {
                res.setText("Closing");
                //        Intent intent = new Intent(getApplicationContext(), DoorClosing.class);
                //        startActivity(intent);
            }
        }
    }

    private Integer ClientCallServer(String cmd)
    {
        Boolean isOpeing = false;
        try
        {
            Socket socket=new Socket("192.168.0.104",14000);
            PrintWriter os=new PrintWriter(socket.getOutputStream());
            BufferedReader is=new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String readline = new String(cmd);

            os.println(readline);		// 命令字符串输出到Server
            os.flush();					// 刷新后,Server可以马上收到字符串

            String status = new String(is.readLine());
            String result = new String("F");

            if(result.equals(status))
                isOpeing = true;
            else
                isOpeing = false;

            os.close();
            is.close();
            socket.close();
        }catch (IOException e) {e.printStackTrace();}

        if(isOpeing)    return 1;
        else            return 0;
    }
}
