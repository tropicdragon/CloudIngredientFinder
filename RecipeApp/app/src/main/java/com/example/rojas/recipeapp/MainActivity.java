package com.example.rojas.recipeapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    MqttHelper mqttHelper;
    String lastMessage = "no message";
    Handler mHandler = new Handler();
    HashMap<Integer, DataPoint> dataPoints = new HashMap<>();
    int id = 1000;//TODO: pick random ID
    private String[] arrText =
            new String[]{"Item 1","Item 2","Item 3","Item 4"
                    ,"Item 5","Item 6","Item 7","Item 8","Item 9","Item 10"

            };
    /*
        private String[] arrText =
            new String[]{"Item 1","Item 2","Item 3","Item 4"
                    ,"Item 5","Item 6","Item 7","Item 8","Item 9","Item 10"
                    ,"Item 11","Item 12","Item 13","Item 14","Item 15"
                    ,"Item 16","Item 17","Item 18","Item 19","Item 20"
                    };
     */
    private String[] arrTemp;
    boolean searching = false;
    boolean sent = false;
    int numIDFound = 0;
    ArrayList<Integer> storeIDs;
    ArrayList<String> itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startMqtt();

        arrTemp = new String[arrText.length];
        arrTemp[0] = "apple";
        arrTemp[1] = "orange";
        arrTemp[2] = "strawberry";
        MyListAdapter myListAdapter = new MyListAdapter();
        ListView listView = (ListView) findViewById(R.id.listViewMain);
        listView.setAdapter(myListAdapter);

        //mqttHelper.subscribeToTopic();
    }
    class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if(arrText != null && arrText.length != 0){
                return arrText.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return arrText[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //ViewHolder holder = null;
            final ViewHolder holder;
            if (convertView == null) {

                holder = new ViewHolder();
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                convertView = inflater.inflate(R.layout.listview_item, null);
                holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
                holder.editText1 = (EditText) convertView.findViewById(R.id.editText1);

                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }

            holder.ref = position;

            holder.textView1.setText(arrText[position]);
            holder.editText1.setText(arrTemp[position]);
            holder.editText1.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable arg0) {
                    // TODO Auto-generated method stub
                    arrTemp[holder.ref] = arg0.toString();
                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView textView1;
            EditText editText1;
            int ref;
        }


    }


    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            /*
                  {
           "buyerID" : i ,
           "usertype" : "buyer",
           "direction" : "server",
           "location" :
           {
               "lat" : lat,
               "long" : long
           },

           "Items" : [
           {
               "itemname" : item_list[0],
           },
           {
               "itemname" : item_list[1],
           }
           ]
       }

             */
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String msg = mqttMessage.toString();

                JSONObject jsonObject = new JSONObject(msg);
                Log.w("Mqtt", jsonObject.toString());


                if(!sent)return;
                if(!searching){
                    String userType = jsonObject.getString("usertype");
                    int userId = jsonObject.getInt("buyerID");
                    String dir = jsonObject.getString("direction");
                    if(userType.equals("buyer") && userId == id &&
                            dir.equals("buyer")){
                        searching = true;
                        storeIDs = new ArrayList<>();
                        itemsList = new ArrayList<>();
                        Log.d("Mqtt", "get response\n" + msg);
                        JSONArray arr = jsonObject.getJSONArray("Items");
                        for(int i = 0; i < arr.length(); i++){
                            JSONObject obj = arr.getJSONObject(i);
                            Log.d("adding", obj.toString());

                            int sellerId = obj.getInt("sellerID");
                            String item = obj.getString("itemname");
                            storeIDs.add(sellerId);
                            itemsList.add(item);


                        }
                        return;
                    }else return;
                }else {
                    Log.d("Mqtt", "searching if valid:");

                    int id;
                    try{
                        id =jsonObject.getInt("sellerID");
                    } catch (Exception e){
                        id = -1;
                        Log.d("error" , e.toString());
                    }
                    if(!storeIDs.contains(id))return;
                    String name = jsonObject.getString("sellername");
                    String contact = jsonObject.getString("sellercontact");
                    JSONObject location = jsonObject.getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lon = location.getDouble("long");
                    JSONArray items = jsonObject.getJSONArray("Items");
                    DataPoint point = new DataPoint(id, name, contact, lat, lon);
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject obj = items.getJSONObject(i);
                        int itemID = obj.getInt("itemID");
                        String itemName = obj.getString("itemname");
                        double price = obj.getDouble("price");
                        int quantity = obj.getInt("quantity");
                        point.addItem(itemID, itemName, price, quantity);
                        for(int j = 0; j < itemsList.size();j++){
                            String str = itemsList.get(j);
                            if(str.equals(itemName) && storeIDs.get(j) == id){
                                numIDFound++;
                                point.itemAppend(itemName, price);
                            }
                        }

                    }
                    for(String str : itemsList){
                        Log.w("String",str + "* ");

                    }

                    dataPoints.put(id, point);


                    final TextView dataReceived = findViewById(R.id.dataReceived);
                    lastMessage = msg;
                    Log.w("Debug", "updating screen");

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            {
                                if(numIDFound == itemsList.size())
                                dataReceived.setText("Found all Sellers");

                            }
                        }
                    };
                    mHandler.post(runnable);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
/*
sample json
       {
           "sellerID" : i ,
           "sellername" : seller_names[i],
          # "notified" : ""
           "usertype" : "buyer",
           "sellercontact" : seller_contacts[i],
           "location" :
           {
               "lat" : lat,
               "long" : long
           },

           "Items" : [
           {
               "itemID" : 0,
               "itemname" : item_list[0],
               "price" : price_list[0],
               "quantity" : random.randint(0,max_quantity)
               #"sellerID" :
           },
           {
               "itemID" : 1,
               "itemname" : item_list[1],
               "price" : price_list[1],
               "quantity" : random.randint(0,max_quantity)
           }
           ]
       }
 */
    public void resetState(){
        searching=false;
        sent=false;
        numIDFound =0;
    }


    public void onClick(View view) throws JSONException {
        resetState();
        TextView dataReceived = findViewById(R.id.dataReceived);
        String message;
        JSONObject json = new JSONObject();
        json.put("buyerID", id);
        json.put("usertype", "buyer");
        json.put("direction", "server");
        JSONObject locjson = new JSONObject();
        Location loc = getLastBestLocation();
        locjson.put("lat", loc.getLatitude());
        locjson.put("long", loc.getLongitude());
        json.put("location", locjson);
        JSONArray array = new JSONArray();
        sent = true;
        for( int i = 0; i < arrTemp.length && arrTemp[i] != null && arrTemp[i].length() >0;i++){
            JSONObject obj = new JSONObject();
            obj.put("itemname", arrTemp[i].trim().toLowerCase());
            array.put(obj);
        }
        json.put("Items", array);
        message = json.toString();
        try {

            mqttHelper.publishMessage(message);
            Log.d("Mqtt", "sending message:\n" + message);
        } catch (MqttException e) {
            Log.e("Mqtt", e.toString());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e("Mqtt", e.toString());
            e.printStackTrace();
        }
        dataReceived.setText("Request Sent");


    }

    private Location getLastBestLocation() {
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.w("Maps", "GPS is off");
            return null;
        }
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }
    public void toMaps(View view) {
        Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        myIntent.putExtra("data", new ArrayList<DataPoint>(dataPoints.values())); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    public class MqttHelper {
        public MqttAndroidClient mqttAndroidClient;

        final String serverUri = "tcp://ec2-54-197-24-57.compute-1.amazonaws.com:1883";

        final String clientId = "AndroidClient";
        final String subscriptionTopic = "localgateway_to_awsiot";

        final String username = "xxxxxxx";
        final String password = "yyyyyyyyyy";

        public MqttHelper(Context context){
            mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    Log.w("mqtt", s);
                }

                @Override
                public void connectionLost(Throwable throwable) {

                }

                @Override
                public void messageArrived(String topic, final MqttMessage mqttMessage) throws Exception {
                    Log.w("Mqtt", mqttMessage.toString());
                    final TextView messageText = (TextView) findViewById(R.id.dataReceived);


                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
            connect();
        }

        public void setCallback(MqttCallbackExtended callback) {
            mqttAndroidClient.setCallback(callback);
        }

        public void publishMessage(@NonNull String msg)
                throws MqttException, UnsupportedEncodingException {
            byte[] encodedPayload = new byte[0];
            encodedPayload = msg.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setId(5866);
            message.setRetained(true);

            mqttAndroidClient.publish(subscriptionTopic, message);
        }
        private void connect(){
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setUserName(username);
            mqttConnectOptions.setPassword(password.toCharArray());

            try {

                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        subscribeToTopic();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                    }
                });


            } catch (MqttException ex){
                ex.printStackTrace();
            }
        }


        private void subscribeToTopic() {
            try {
                mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.w("Mqtt","Subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Mqtt", "Subscribed fail!");
                    }
                });

            } catch (MqttException ex) {
                System.err.println("Exception whilst subscribing");
                ex.printStackTrace();
            }
        }
    }
}
