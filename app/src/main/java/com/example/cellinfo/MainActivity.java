package com.example.cellinfo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public class BG extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("HarryBG", "onPreExecute: ran");
        }

        @Override
        protected String doInBackground(String... urls) {
            String text = "";
            Log.d("HarryBG", "doInBackground: ran");
//            Log.d("HarryBG", urls[0]);
            String result = "";
//            URL url;
//            HttpURLConnection conn;
            URL url = null;
            try {
                url = new URL("https://ap1.unwiredlabs.com/v2/process.php");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            URLConnection con = null;
            try {
                con = url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpURLConnection http = (HttpURLConnection) con;
            try {
                http.setRequestMethod("POST"); // PUT is another valid option
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json; utf-8");
            http.setRequestProperty("Accept", "application/json");
            http.setDoOutput(true);
            String jsonInputString = "{\"token\": \"pk.4808dce2252a7e5ef0872b000a33809d\",\"radio\": \"lte\",\"mcc\": 405,\"mnc\": 872,\"cells\": [{\"lac\": 60,\"cid\": 34326800,\"psc\": 0}],\"address\": 1}";
            try (OutputStream os = http.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                Log.d("Network details", response.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return "Something went wrong";
            }
            return text;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("HarryBG", "onPostExecute: ran");
            Log.d("HarryBG", s);

        }

    }



    TextView info, location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        info = findViewById(R.id.info_dup);
        location = findViewById((R.id.loc));
        BG task = new BG();
        task.execute("https://ap1.unwiredlabs.com/v2/process.php");

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void trigger(View view) throws IOException {

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String operator = "Network operator : " + tm.getNetworkOperator();
//        location.setText(operator);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return;
        }

        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        String text = "";
        if (cellInfoList != null && cellInfoList.size() > 0) {
            for (CellInfo info : cellInfoList) {

//                text += info.toString() +"\n\n";
                if (info instanceof CellInfoLte) {
                    LTEStruct lte = new LTEStruct(this);
                    lte.parse(info.toString());
                    //write out parsed results for what it's worth
//                    Log.i("LTE parseOutput", "tAdvance: " + lte.tAdvance + "\r\nCQI: " + lte.CQI + "\r\nRSSNR: " + lte.RSSNR + "\r\nRSRP: " + lte.RSRP + "\r\nSS: " + lte.SS +
//                            "\r\nCID: " + lte.CID + "\r\nTimestamp: " + lte.timeStamp + "\r\nTAC: " + lte.TAC + "\r\nPCI: " + lte.PCI + "\r\nMNC: " + lte.MNC + "\r\nMCC: " + lte.MCC + "\r\nRegistered: " + lte.isRegistered);
                    text += "MCC: " + lte.MCC + "\nMNC: " + lte.MNC + "\nLAC: " + lte.TAC + "\nCID: " + lte.CID+"\n\n";
                } else
                    Log.i("LTE testing", "not LTE cell info measured");
            }

        }
        else
            text = "No data";

        location.setText(text);


    }





}


