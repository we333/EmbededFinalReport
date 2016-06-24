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
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;



public class MainActivity extends AppCompatActivity
{
    private static Boolean isDoorOpening = false;
    private static Boolean isSendMail = false;
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

        monitor.post(task);     // action loop for checking DoorStatus

        btnBuzOn.setOnClickListener(listener);
        btnBuzOff.setOnClickListener(listener);
        btnDoorStatus.setOnClickListener(listener);
    }

    // action background loop for checking DoorStatus
    private Handler monitor = new Handler();
    private Runnable task =new Runnable()
    {
        public void run()
        {
            monitor.postDelayed(this,2000); // check DoorStatus each 2s
            if(monitorEnable.isChecked())
            {
                new DoorMonitor().execute();
                if(isDoorOpening && !isSendMail)
                {
                    isSendMail = true;
                    new SendMail().execute();
                }
            }
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
                    public void run() {ClientCallServer("A");
                    }
                })).start();
            }
            else if(v.getId() == R.id.btn_buz_off)
            {
                (new Thread(new Runnable() {
                    @Override
                    public void run() {ClientCallServer("B");
                    }
                })).start();
            }
            else if(v.getId() == R.id.btn_door_status)
            {
                new DoorMonitor().execute();
            }
            else ;
        }
    };

    private class SendMail extends AsyncTask<Integer, Integer, String> {
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Send Mail!", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }
        protected String doInBackground(Integer... params) {
            Mail m = new Mail("dongzhe2015", "ribenliuxue");

            String[] toArr = {"625125209@qq.com","wantone321@gmail.com"};
            m.setTo(toArr);
            m.setFrom("dongzhe2015@yahoo.co.jp");
            m.setSubject("DOOR!!");
            m.setBody("door is opening");

            try {
                //If you want add attachment use function addAttachment.
                //m.addAttachment("/sdcard/filelocation");
                if(m.send())
                    System.out.println("Email was sent successfully.");
                else
                    System.out.println("Email was not sent.");
            } catch(Exception e) {
                //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                Log.e("MailApp", "Could not send email", e);
            }
            return "";
        }
    }

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
                isDoorOpening = true;
                res.setText("Door Opeing");
            }
            else
            {
                isDoorOpening = false;
                isSendMail = false;
                res.setText("Door Closing");
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

            os.println(readline);       // 命令字符串输出到Server
            os.flush();                 // 刷新后,Server可以马上收到字符串

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
