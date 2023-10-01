package com.example.bancodigital.cobrar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Pagamento;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ReciboPagamentoActivity extends AppCompatActivity {

    private TextView textCodigo;
    private TextView textData;
    private TextView textValor;

    private TextView textUsuario;
    private ImageView imagemUsuario;
    private Usuario usuario;
    private Pagamento pagamento;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recibo_pagamento);

        configToolbar();

        iniciaComponente();

        configCliques();

        recuperaDados();
    }


    private void recuperaDados(){
        String idPagamento = getIntent().getStringExtra("idPagamento");

        DatabaseReference pagamentoRef = FirebaseHelper.getDatabaseReference()
                .child("pagamentos")
                .child(idPagamento);
        pagamentoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pagamento = snapshot.getValue(Pagamento.class);

                recuperaUsuario();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperaUsuario() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(pagamento.getIdUserDestino());
        //aqui foi criado o ValueEventListener para monitorar em tempo real
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuario = snapshot.getValue(Usuario.class);
                configDados();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configCliques(){
        findViewById(R.id.btnOk).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            //aqui perguntar sobre oque por aqui pra dividir a flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void configDados() {

        // recupera o nome e a imagem do usuario
        textUsuario.setText(usuario.getNome());
        if (usuario.getUrlImagem() != null) {
            Picasso.get().load(usuario.getUrlImagem())
                    .placeholder(R.drawable.drloading)
                    .into(imagemUsuario);
        }

        textCodigo.setText(pagamento.getId());
        textData.setText(GetMask.getDate(pagamento.getData(), 3));
        textValor.setText(getString(R.string.text_valor, GetMask.getValor(pagamento.getValor())));
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Recibo");
    }

    private void iniciaComponente(){
        textCodigo = findViewById(R.id.textCodigo);
        textData = findViewById(R.id.textData);
        textValor = findViewById(R.id.textValor);
        textUsuario = findViewById(R.id.textUsuario);
        imagemUsuario = findViewById(R.id.imagemUsuario);

    }
}