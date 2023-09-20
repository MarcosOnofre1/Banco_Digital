package com.example.bancodigital.recarga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Deposito;
import com.example.bancodigital.model.Recarga;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class RecargaReciboActivity extends AppCompatActivity {

    private TextView textCodigo;
    private TextView textData;
    private TextView textValor;
    private TextView textNumero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga_recibo);

        iniciaComponente();

        configToolbar();

        getRecarga();

        configCliques();

    }

    private void configCliques(){
        findViewById(R.id.btnOkRecarga).setOnClickListener(v -> finish());
    }

    private void getRecarga(){
        String idRecarga = (String) getIntent().getSerializableExtra("idRecarga");

        DatabaseReference RecargaRef = FirebaseHelper.getDatabaseReference()
                .child("recargas")
                .child(idRecarga);

        RecargaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recarga recarga = snapshot.getValue(Recarga.class);
                configDados(recarga);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void configDados(Recarga recarga){
        textCodigo.setText(recarga.getId());
        textData.setText(GetMask.getDate(recarga.getData(),3));
        // aqui foi criado uma string "text_valor" para utilizar nesse metodo
        textValor.setText(getString(R.string.text_valor, GetMask.getValor(recarga.getValor())));
        textNumero.setText(recarga.getNumero());

    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Recibo");
    }

    private void iniciaComponente(){
        textCodigo = findViewById(R.id.textCodigo);
        textData = findViewById(R.id.textData);
        textValor = findViewById(R.id.textValor);
        textNumero = findViewById(R.id.textNumero);

    }

}