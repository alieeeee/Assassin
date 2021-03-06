package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceConfigurationError;

/**
 * Created by kuenj on 1/22/2016.
 */
public class WaitingRoom extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ArrayList<String> data = new ArrayList<String>();
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    TextView mLatitudeText;
    TextView mLongitudeText;
    TextView mLastUpdateText;
    String mLastUpdateTime;
    Button btRefresh;
    LocationRequest mLocationRequest;
    ParseUser user;
    private int numberOfUsersOnline = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.waiting_room);
        user = ParseUser.getCurrentUser();
        user.put("availability", true);
        user.saveInBackground();
        btRefresh = (Button)findViewById(R.id.refresh);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        btRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Refresh(numberOfUsersOnline);
            }
        });
        createLocationRequest();
        super.onCreate(savedInstanceState);
        ListView victimList = (ListView) findViewById(R.id.chooseVictimList);

        generateListContent(numberOfUsersOnline);
        victimList.setAdapter(new MyListAdapter(this, R.layout.victim_list_item, data));
        //ArrayAdapter<String> chooseVictimListAdapter = new ArrayAdapter<String>(this, android.R.layout.victim_list_item, )
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void Refresh(int numOfUsers) {
        String AustinId = "Pzg1gMmHDz";
        ParseQuery<ParseUser> users = ParseUser.getQuery();
        ParseQuery<ParseUser> Austin = users.whereEqualTo("objectId", AustinId);
        Austin.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> a, ParseException e) {
                if (e == null) {
                    if (a.size() == 1) {
                        ParseUser austin = a.get(0);
                        ParseGeoPoint g = austin.getParseGeoPoint("location");
                    }
                } else {

                }
            }
        });

    }


    private void generateListContent(int n){
        for(int i = 0; i < n; i++)
        {
            data.add("this is a row" + i);
        }
    }

    @Override
    public void onConnected(Bundle bundle) throws SecurityException{
        try  {
            // TODO: https://developer.android.com/training/location/receive-location-updates.html
            // check for requesting location update is on.
            startLocationUpdates();

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mCurrentLocation != null) {
                mLatitudeText = (TextView)findViewById(R.id.mLatitudeText);
                mLongitudeText = (TextView)findViewById(R.id.mLongitudeText);
                mLastUpdateText = (TextView)findViewById(R.id.mLastUpdateText);
                mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
                mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
                mLastUpdateText.setText(DateFormat.getTimeInstance().format(new Date()));
                }
        }
        catch (SecurityException e) {

        }
    }

    protected void startLocationUpdates() throws SecurityException{
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
        catch(SecurityException e){
            //TODO: Jeffery;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
        user.put("availability", false);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        user.put("availability", false);
    }

    @Override
    protected void onResume(){
        super.onResume();
        user.put("availability", true);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: Jeffery gonna implement this;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        updateDatabase();
        if(user.getString("targeted") != null){
            if(!user.getBoolean("availability")){
                user.put("notificationFlag",null);
            }
            else{
                // jump to TargetedActivity
            }
        }
    }
    private void updateDatabase(){
        ParseGeoPoint gp = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        user.put("location", gp);
        user.saveInBackground();
    }

    private void updateUI() {
        mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
        mLastUpdateText.setText(mLastUpdateTime);
    }
    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;

        private MyListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewholder = null;
            if(convertView == null){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.button = (Button) convertView.findViewById(R.id.victim_item_btn);
                viewHolder.button.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "Matching you with victim", Toast.LENGTH_SHORT).show();

                        Intent match = new Intent("android.intent.action.AssassinActivity");
//                        //Passing target info to match
//                        Bundle b = new Bundle();
//                        b.putString("target","38B8wq64nD");
//                        match.putExtras(b);
                        startActivity(match);
                    }
                });
                convertView.setTag(viewHolder);
            }
            else{
                mainViewholder = (ViewHolder) convertView.getTag();
            }

            return convertView;
        }
    }
    public class ViewHolder{
        Button button;
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
}


