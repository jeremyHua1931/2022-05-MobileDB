package drz.oddb;
//import android.support.constraint.ConstraintLayout;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.Polyline;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;

        import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TravelActivity extends AppCompatActivity {

    List<LatLng> latLngs = new ArrayList<LatLng>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);


        Button button_map=findViewById(R.id.button_map);

        MapView showMap = findViewById(R.id.map);
        TencentMap mTencentMap = showMap.getMap();

        LatLng center = new LatLng(30.534864,  114.360756);
        LatLng center1 = new LatLng(30.536287, 114.359264);
        LatLng center2 = new LatLng(30.533607, 114.363116);


        mTencentMap.addOnMapLoadedCallback(() -> { });

        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                MainActivity.trans.query("CREATEMAP WITH  user AS user, travel AS travel ,x AS x, y AS y FROM  allAPP WHERE user=\"whu\";");
                assignLatLng();

                mTencentMap.addMarker(new MarkerOptions(center1)
                        .anchor(0.5f, 1));
                mTencentMap.addMarker(new MarkerOptions(center2)
                        .anchor(0.5f, 1));
                mTencentMap.addMarker(new MarkerOptions(center)
                        .anchor(0.5f, 1));
                mTencentMap.moveCamera(CameraUpdateFactory.newLatLngBoundsWithMapCenter(
                        new LatLngBounds.Builder()
                                .include(latLngs)
                                .build(),
                        center,
                        100));

                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(latLngs)
                        // 折线设置圆形线头
                        .lineCap(true)
                        // 折线的颜色为绿色
                        .color(0xff00ff00)
                        // 折线宽度为25像素
                        .width(25)
                        // 还可以添加描边颜色
                        .borderColor(0xffff0000)
                        // 描边颜色的宽度，线宽还是 25 像素，不过填充的部分宽度为 `width` - 2 * `borderWidth`
                        .borderWidth(5);
                Polyline polyline = mTencentMap.addPolyline(polylineOptions);

                mTencentMap.addOnMapLoadedCallback(() -> { });
            }
        });
    }


    public void assignLatLng(){

        for(int i=0;i<MainActivity.data1[0].length;i=i+2){

            System.out.println(MainActivity.data1[3][i+1]+"   "+MainActivity.data1[3][i]);
            latLngs.add(new LatLng(MainActivity.data1[3][i+1],MainActivity.data1[3][i]));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        MapView showMap = findViewById(R.id.map);
        showMap.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapView showMap = findViewById(R.id.map);
        showMap.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        MapView showMap = findViewById(R.id.map);
        showMap.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        MapView showMap = findViewById(R.id.map);
        showMap.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MapView showMap = findViewById(R.id.map);
        showMap.onDestroy();
    }
}