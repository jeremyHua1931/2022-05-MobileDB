package drz.oddb;

import android.content.DialogInterface;
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

import java.io.Serializable;

import drz.oddb.Memory.*;
import drz.oddb.Transaction.SystemTable.BiPointerTable;
import drz.oddb.Transaction.SystemTable.ClassTable;
import drz.oddb.Transaction.SystemTable.ClassTableItem;
import drz.oddb.Transaction.SystemTable.DeputyTable;
import drz.oddb.Transaction.SystemTable.ObjectTable;
import drz.oddb.Transaction.SystemTable.SwitchingTable;
import drz.oddb.Transaction.TransAction;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected  void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //找到控件
        Button button_db =findViewById(R.id.db_button);
        Button button_travel=findViewById(R.id.travel_button);

        button_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=null;
                intent=new Intent(HomeActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        button_travel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=null;
                intent=new Intent(HomeActivity.this,TravelActivity.class);
                startActivity(intent);
            }
        });



    }


}
