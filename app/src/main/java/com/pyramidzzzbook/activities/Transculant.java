package com.pyramidzzzbook.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.pyramidzzzbook.R;

public class Transculant extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transculant);
        startActivity(new Intent(Transculant.this, SplashActivity.class));
    }
    
}