package com.example.bancodigital.deposito;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Deposito;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DepositoReciboActivity extends AppCompatActivity {

    private TextView textCodigo;
    private TextView textData;
    private TextView textValor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposito_recibo);

        iniciaComponente();

        configToolbar();

        getDeposito();

        configCliques();

    }

    private void configCliques(){
        findViewById(R.id.btnOkDeposito).setOnClickListener(v -> finish());
    }

    private void getDeposito(){
        String idDeposito = (String) getIntent().getSerializableExtra("idDeposito");

        DatabaseReference depositoRef = FirebaseHelper.getDatabaseReference()
                .child("depositos")
                .child(idDeposito);
        depositoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Deposito deposito = snapshot.getValue(Deposito.class);
                configDados(deposito);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //aqui mostrará no comprovante o codigo do comprovante, o valor e a data.
    private void configDados(Deposito deposito){
        textCodigo.setText(deposito.getId());
        textData.setText(GetMask.getDate(deposito.getData(),3));
        // aqui foi criado uma string "text_valor" para utilizar nesse metodo
        textValor.setText(getString(R.string.text_valor, GetMask.getValor(deposito.getValor())));

    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Recibo");
    }

    private void iniciaComponente(){
        textCodigo = findViewById(R.id.textCodigo);
        textData = findViewById(R.id.textData);
        textValor = findViewById(R.id.textValor);


    }

}