package com.example.bancodigital.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.bancodigital.R;
import com.example.bancodigital.autenticacao.LoginActivity;
import com.example.bancodigital.helper.FirebaseHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splat);

        // Esse metodo é utilizado para adicionar um tempo de delay para ir na tela
        new Handler(Looper.getMainLooper()).postDelayed(this::getAutenticacao, 2500);

    }

    //puxa o FirebaseHelper + getAutenticado para ver em que tela inicia
    private void getAutenticacao() {
        if (FirebaseHelper.getAutenticado()) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
           startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}