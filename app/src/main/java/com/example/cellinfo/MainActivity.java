package com.example.cellinfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView info, location, dist;
    ArrayList<String> lat = new ArrayList<>(); //contains latitute of the ith base station
    ArrayList<String> lon = new ArrayList<>(); //contains lognitute of the ith base station
    List<Integer> signals = new ArrayList<>(); //contains signal strengths of ith base station
    FusedLocationProviderClient fusedLocationProviderClient;
    double latitude, longitude; //phone's coordinates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        info = findViewById(R.id.info_dup);
        location = findViewById((R.id.loc));
        info = findViewById(R.id.textView4);
        dist = findViewById(R.id.textView5);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

    }
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //We have a location

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    info.setText("GPS coordinates : "+"Latitude : "+latitude+"  Longitude : "+longitude);

                } else  {
                    Log.d("TAG", "onSuccess: Location was null...");
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: " + e.getLocalizedMessage() );
            }
        });
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
                        Log.i("cell info : ",info.toString());
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

                            signals.add(lte.STH);
                        }

                        text += "MCC: " + lte.MCC + "\nMNC: " + lte.MNC + "\nLAC: " + lte.TAC + "\nCID: " + lte.CID+"\nLat :"+lat.get(lat.size()-1)+"\nLon :"+lon.get(lon.size()-1)+"\nStrength : "+lte.STH+"\n\n";
                    } else
                        Log.i("LTE testing", "not LTE cell info measured");

                }

            }
            else
                text = "No data";



//            location.setText(text);

//            String text = "";

            return text;
        }
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("HarryBG", "onPostExecute: ran");
            Log.d("HarryBG", s);
            location.setText(s);


//            double total_dist = 0, avg_distance;
//            ArrayList<Double> distances = new ArrayList<>();
//            for(int i=0; i<lat.size(); i++){
//                distances.add(distance(latitude, Double.parseDouble(lat.get(i)), longitude, Double.parseDouble(lon.get(i))));
//            }
//
//            for(double dist : distances){
//                total_dist += dist;
//            }
//
//            avg_distance = total_dist/distances.size()*1000;
//
//            if(avg_distance < 1000)
//                dist.setText("No sign of spoofing. \nAverage distance to nearest mobile towers : "+Double.toString(avg_distance)+" meters");
//            else
//                dist.setText(avg_distance+getString(R.string.avg));

            int sum = 0;
            for(int signal : signals){
                sum += signal;
            }
//            String testing = "";
            for(int i=0; i<signals.size(); i++){
//                testing += "Signal "+Integer.toString(signals.get(i));
                signals.set(i, ((sum-signals.get(i)+1)*100)/sum);
//                testing += "after flipping Signal "+Integer.toString(signals.get(i));
            }


//            for(int signal : signals){
//                testing += "signal :"+Integer.toString(signal);
//            }
            ArrayList<Double[]> coordinates = applyeWeights();

//            for(Double[] point : coordinates){
//               testing += "replicated coord : "+Double.toString(point[0])+" "+Double.toString(point[1]);
//            }

            Double[] avg_coord = avgCoordinates(coordinates);
            double mean_distance = distance(latitude, avg_coord[0], longitude, avg_coord[1]);
            dist.setText("Mean distance : "+mean_distance+" avg coord : "+avg_coord[0]+" "+avg_coord[1]);
        }

    }

    public ArrayList<Double[]> applyeWeights(){

        ArrayList<Double[]> coordinates = new ArrayList<>();
        int i=0;
        for(int weight : signals){

            while(weight != 0){
                Double[] coord = new Double[]{Double.parseDouble(lat.get(i)), Double.parseDouble(lon.get(i))};
                coordinates.add(coord);
                weight--;
            }
            i++;
        }

        return coordinates;
    }

    public Double[] avgCoordinates(ArrayList<Double[]> points){
        if(points.size() == 1){
            return points.get(0);
        }

        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        for(Double[] coord : points){
            double lat = Math.toRadians(coord[0]);
            double log = Math.toRadians(coord[1]);

            x += Math.cos(lat)*Math.cos(log);
            y += Math.cos(lat)*Math.sin(log);
            z += Math.sin(lat);
        }

        double total = points.size();

        x = x/total;
        y = y/total;
        z = z/total;

        double centralLongitude = Math.atan2(y, x);
        double centralSquareRoot = Math.sqrt(x * x + y * y);
        double centralLatitude = Math.atan2(z, centralSquareRoot);

        return new Double[]{ Math.toDegrees(centralLatitude), Math.toDegrees(centralLongitude)};

    }

    public static double distance(double lat1,
                                  double lat2, double lon1,
                                  double lon2)
    {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return(c * r);
    }



}


