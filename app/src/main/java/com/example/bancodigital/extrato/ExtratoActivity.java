package com.example.bancodigital.extrato;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.adapter.ExtratoAdapter;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Extrato;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExtratoActivity extends AppCompatActivity {

    private final List<Extrato> extratoList = new ArrayList<>();
    private ExtratoAdapter extratoAdapter;
    private RecyclerView rvExtrato;

    private TextView textInfo;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extrato);

        configToolbar();

        iniciaComponentes();

        configRv();

        recuperaExtrato();

    }

    private void configRv(){
        rvExtrato.setLayoutManager(new LinearLayoutManager(this));
        rvExtrato.setHasFixedSize(true);
        extratoAdapter = new ExtratoAdapter(extratoList, getBaseContext());
        rvExtrato.setAdapter(extratoAdapter);
    }

    private void recuperaExtrato() {
        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase());

        //essa lista de ValueEventListener vai ser atualizada em tempo real automaticamente.
        extratoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    extratoList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Extrato extrato = ds.getValue(Extrato.class);
                        extratoList.add(extrato);
                    }
                    textInfo.setText("");

                } else {
                    textInfo.setText("Nenhuma movimentação encontrada.");

                }

                Collections.reverse(extratoList);
                progressBar.setVisibility(View.GONE);
                extratoAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configToolbar() {

        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Extrato");
        findViewById(R.id.ibVoltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {
        progressBar = findViewById(R.id.progressBar);
        textInfo = findViewById(R.id.textInfo);
        rvExtrato = findViewById(R.id.rvExtrato);
    }

}