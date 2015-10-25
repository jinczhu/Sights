package com.clarifai.androidstarter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

public class RecognitionActivity extends Activity {

    private Button selectButtonU;
    private Button selectButtonT;
    private TextView sightsIntro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        sightsIntro = (TextView) findViewById(R.id.welcome_view);
        selectButtonU = (Button) findViewById(R.id.selectu_button);
        selectButtonT = (Button) findViewById(R.id.selectt_button);

        selectButtonU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButtonU.setBackgroundColor(Color.BLUE);
                Intent userScreen = new Intent(getApplicationContext(), UseActivity.class);
                startActivity(userScreen);
            }
        });

        selectButtonT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButtonT.setBackgroundColor(Color.BLUE);
                Intent trainerScreen = new Intent(getApplicationContext(), TrainerActivity.class);
                startActivity(trainerScreen);
            }
        });
    }
}
