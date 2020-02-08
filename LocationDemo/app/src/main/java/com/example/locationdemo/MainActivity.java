package com.example.locationdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.WebTiledLayer;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final String TAG= "MainActivity";

    private LocationDisplay locationDisplay=null;
    //位置 类
    private Location location=null;
    //定位管理 类
    private LocationManager locationManager=null;
    private String provider=null;

    private boolean status = false;
    private float bear;

    Point point=null;

    Button openbutton;
    Button closebutton;
    TextView latitudeview;
    TextView longitudeview;
    TextView altitudeview;
    TextView timeview;
    TextView bearingview;
    TextView stastatusview;
    TextView arclatitudeview;
    TextView arclongitudeview;
    TextView arcaltitudeview;

    MapView mapView = null;
    ArcGISMap Map = null;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //获取控件
        findview();

        final WebTiledLayer webTiledLayer =
                TianDiTuMethodsClass.CreateTianDiTuTiledLayer(TianDiTuMethodsClass.LayerType.TIANDITU_VECTOR_MERCATOR);

        webTiledLayer.loadAsync();
        Map = new ArcGISMap(new Basemap(webTiledLayer));
        Point center = new Point(116.224033,29.704225, SpatialReference.create(4490));// CGCS2000
        mapView.setViewpointCenterAsync(center,12);
        mapView.setMap(Map);

        //设置监听器
        openbutton.setOnClickListener(view -> {
            if (!status) {
                openGPSSettings();
                getLocation();
                //获取基于GPS的位置信息
                LocationDataSource.Location location = locationDisplay.getLocation();
                //基于WGS84的经纬度坐标。
                point= location.getPosition();
                if (point != null) {
                    arclatitudeview.setText(""+point.getY());
                    arclongitudeview.setText(""+point.getX());
                    arcaltitudeview.setText(""+point.getZ());
                }
                else Log.e(TAG, "未获取到坐标");

                locationDisplay.addLocationChangedListener(locationChangedListener);
                status = true;
            }
        });
        closebutton.setOnClickListener(view ->{
            closeGps();
            arclatitudeview.setText("");
            arclongitudeview.setText("");
            arcaltitudeview.setText("");

        } );

    }

    //获取界面内的控件
    private void findview() {
        openbutton = (Button) findViewById(R.id.open);
        closebutton = (Button) findViewById(R.id.close);
        latitudeview = (TextView) findViewById(R.id.latitudevalue);
        longitudeview = (TextView) findViewById(R.id.longtitudevalue);
        altitudeview = (TextView) findViewById(R.id.altitudevalue);
        arclatitudeview = (TextView) findViewById(R.id.Arclatitudevalue);
        arclongitudeview = (TextView) findViewById(R.id.Arclongtitudevalue);
        arcaltitudeview = (TextView) findViewById(R.id.Arcaltitudevalue);
        timeview = (TextView) findViewById(R.id.timevalue);
        bearingview = (TextView) findViewById(R.id.bearingvalue);
        stastatusview = (TextView) findViewById(R.id.Status);
        mapView = (MapView)findViewById(R.id.mapView);//设置UI和代码绑定
    }

    //关闭GPS定位
    private void closeGps() {
        if (status == true) {
            locationManager.removeUpdates(locationListener);
            latitudeview.setText("");
            longitudeview.setText("");
            timeview.setText("");
            altitudeview.setText("");
            bearingview.setText("");

            status = false;
            stastatusview.setText("" + status);
        }
    }

    //ArcGis中位置是否发生变化
    private  LocationDisplay.LocationChangedListener locationChangedListener=new LocationDisplay.LocationChangedListener(){
        @Override
        public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
            Log.e(TAG, "坐标发生变化");
            LocationDataSource.Location location=locationChangedEvent.getLocation();
            point=location.getPosition();
            if (point != null) {
                arclatitudeview.setText(""+point.getY());
                arclongitudeview.setText(""+point.getX());
                arcaltitudeview.setText(""+point.getZ());
            }
            else Log.e(TAG, "未获取到坐标");
        }
    };

    //负责监听位置信息的变化情况
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateToNewLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            updateToNewLocation(null);
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    //打开GPS卫星设置
    private void openGPSSettings() {
        //获取位置
        locationDisplay = mapView.getLocationDisplay();
        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
        locationDisplay.startAsync();


        //获取位置管理服务
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Gps模块正常", Toast.LENGTH_SHORT).show();
            return;
        }
        status = false;
        Toast.makeText(this, "开启Gps模块", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        startActivityForResult(intent, 0);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation() {

        //对数据进行设置
        Criteria criteria = new Criteria();
        //查询精度：高
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //查询海拔：
        criteria.setAltitudeRequired(true);
        //查询方位角
        criteria.setBearingRequired(true);
        //允许付费
        criteria.setCostAllowed(true);
        //电量要求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        //查询速度
        criteria.setSpeedRequired(true);

        provider = locationManager.getBestProvider(criteria, true);
        Toast.makeText(this, "可获取位置", Toast.LENGTH_SHORT).show();

        // 获取GPS信息，获取provider中的信息
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        // 通过GPS获取位置
        updateToNewLocation(location);
        // 设置监听器，最小更新时间为 N  毫秒，或位移变化为 ,米
        // 实时获取位置数据，一旦发生变化，立即更新
        locationManager.requestLocationUpdates(provider,1000,0,locationListener);
        }

    private void updateToNewLocation(Location location)
    {
        if (location !=null)
        {
            bear=location.getBearing();
            double latitude=location.getLatitude();
            double longtitude=location.getLongitude();
            long GpsTime=location.getTime();
            Date date=new Date(GpsTime);
            DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            double GpsAlt=location.getAltitude();

            latitudeview.setText(""+latitude);
            longitudeview.setText(""+longtitude);
            timeview.setText(""+df.format(date));
            altitudeview.setText(""+GpsAlt);
            bearingview.setText(""+bear);
            stastatusview.setText(""+status);
        }
        else
        {
//            Toast.makeText(this, "无法获取位置", Toast.LENGTH_SHORT).show();
        }
    }

}
