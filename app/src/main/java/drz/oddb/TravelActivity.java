package drz.oddb;
import android.content.Intent;
//import android.support.constraint.ConstraintLayout;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Polyline;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import drz.oddb.Memory.*;
import drz.oddb.Transaction.SystemTable.BiPointerTable;
import drz.oddb.Transaction.SystemTable.ClassTable;
import drz.oddb.Transaction.SystemTable.ClassTableItem;
import drz.oddb.Transaction.SystemTable.DeputyTable;
import drz.oddb.Transaction.SystemTable.ObjectTable;
import drz.oddb.Transaction.SystemTable.SwitchingTable;
import drz.oddb.Transaction.TransAction;



public class TravelActivity extends AppCompatActivity {

    List<LatLng> latLngs = new ArrayList<LatLng>();

    public void assginLatLng(){

        for(int i=0;i<MainActivity.data1[0].length;i++){

            latLngs.add(new LatLng(MainActivity.data1[4][i],MainActivity.data1[4][i+1]));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);



        MapView showMap = findViewById(R.id.map);

        TencentMap mTencentMap = showMap.getMap();




//        latLngs.add(new LatLng(39.984864,116.305756));
//        latLngs.add(new LatLng(39.983618,116.305848));
//        latLngs.add(new LatLng(39.982347,116.305966));
//        latLngs.add(new LatLng(39.982412,116.308111));
//        latLngs.add(new LatLng(39.984122,116.308224));
//        latLngs.add(new LatLng(39.984955,116.308099));

// 构造 PolylineOpitons
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

// 绘制折线
        Polyline polyline = mTencentMap.addPolyline(polylineOptions);

        mTencentMap.addOnMapLoadedCallback(() -> { });



//
//        Button mapButton=findViewById(R.id.);
//        mapButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                for (int i = 0; i <= 4; i++) {
//                    System.out.println(Arrays.deepToString(new float[][]{MainActivity.data1[i]}));
//                }
//
//                //TODO: 此处进行地图展示动作 float data[][]为轨迹数据值,需要提前执行，不行你自己先定义一组float[] [],
//                // CREATEMAP WITH user AS user, travel AS travel ,x AS x, y AS y FROM  allAPP WHERE user="whu";
//            }
//        });



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