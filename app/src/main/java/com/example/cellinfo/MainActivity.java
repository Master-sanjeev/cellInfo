package com.example.cellinfo;

import android.Manifest;
import android.annotation.SuppressLint;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView info, location;
    ArrayList<String> lat = new ArrayList<>();
    ArrayList<String> lon = new ArrayList<>();
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        info = findViewById(R.id.info_dup);
        location = findViewById((R.id.loc));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitudeTextView.setText(location.getLatitude() + "");
                            longitTextView.setText(location.getLongitude() + "");
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
}


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void trigger(View view) throws IOException {

        Log.i("trigger", "trigger function fired");
        BG task = new BG();
        task.execute("https://ap1.unwiredlabs.com/v2/process.php");

    }

    public class BG extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("HarryBG", "onPreExecute: ran");
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        protected String doInBackground(String... urls) {

            String s = "";
            String text = "";
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String operator = "Network operator : " + tm.getNetworkOperator();
//        location.setText(operator);



            @SuppressLint("MissingPermission") List<CellInfo> cellInfoList = tm.getAllCellInfo();

            if (cellInfoList != null && cellInfoList.size() > 0) {
                for (CellInfo info : cellInfoList) {

//                text += info.toString() +"\n\n";
                    if (info instanceof CellInfoLte) {
                        LTEStruct lte = new LTEStruct(this);
                        lte.parse(info.toString());
                        //write out parsed results for what it's worth
//                    Log.i("LTE parseOutput", "tAdvance: " + lte.tAdvance + "\r\nCQI: " + lte.CQI + "\r\nRSSNR: " + lte.RSSNR + "\r\nRSRP: " + lte.RSRP + "\r\nSS: " + lte.SS +
//                            "\r\nCID: " + lte.CID + "\r\nTimestamp: " + lte.timeStamp + "\r\nTAC: " + lte.TAC + "\r\nPCI: " + lte.PCI + "\r\nMNC: " + lte.MNC + "\r\nMCC: " + lte.MCC + "\r\nRegistered: " + lte.isRegistered);
                        if(lte.MCC == 0)
                            continue;
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
                        String jsonInputString = "{\"token\": \"pk.4808dce2252a7e5ef0872b000a33809d\",\"radio\": \"lte\",\"mcc\": "+lte.MCC +",\"mnc\": "+lte.MNC +",\"cells\": [{\"lac\": "+lte.TAC+",\"cid\": "+lte.CID+",\"psc\": 0}],\"address\": 1}";
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
                            s = response.toString();
                            Log.d("Network details", s);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "Something went wrong";
                        }

                        if(s.substring(11, 13).equals("ok")){
                            int index = s.indexOf("\"lat\":") + ("\"lat\":").length();
                            int endIndex = s.indexOf(",",index);
                            lat.add(s.substring(index, endIndex).trim());

                            index = s.indexOf("\"lon\":") + ("\"lon\":").length();
                            endIndex = s.indexOf(",",index);
                            lon.add(s.substring(index, endIndex).trim());

                        }

                        text += "MCC: " + lte.MCC + "\nMNC: " + lte.MNC + "\nLAC: " + lte.TAC + "\nCID: " + lte.CID+"\nLat :"+lat.get(lat.size()-1)+"\nLon :"+lon.get(lon.size()-1)+"\n\n";
                    } else
                        Log.i("LTE testing", "not LTE cell info measured");

                }

            }
            else
                text = "No data";

            return text;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("HarryBG", "onPostExecute: ran");
            Log.d("HarryBG", s);
            location.setText(s);
        }

    }



}


