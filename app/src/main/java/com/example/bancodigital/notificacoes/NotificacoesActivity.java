package com.example.bancodigital.notificacoes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bancodigital.R;
import com.example.bancodigital.adapter.NotificacaoAdapter;
import com.example.bancodigital.cobrar.PagarCobrancaActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.transferencia.TransferenciaReciboActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificacoesActivity extends AppCompatActivity implements NotificacaoAdapter.Onclick {

    private NotificacaoAdapter notificacaoAdapter;
    private final List<Notificacao> notificacaoList = new ArrayList<>();
    private SwipeableRecyclerView rvNotificacoes;
    private Dialog dialog;
    private TextView textInfo;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes);

        configToolbar();

        iniciaComponentes();

        configRv();

        recuperaNotificacoes();


    }



    private void configRv(){
        rvNotificacoes.setLayoutManager(new LinearLayoutManager(this));
        rvNotificacoes.setHasFixedSize(true);
        notificacaoAdapter = new NotificacaoAdapter(notificacaoList, getBaseContext(), this);
        rvNotificacoes.setAdapter(notificacaoAdapter);

        rvNotificacoes.setListener(new SwipeLeftRightCallback.Listener() {
            @Override
            public void onSwipedLeft(int position) {

                // delete
                showDialogRemover(notificacaoList.get(position));
            }

            @Override
            public void onSwipedRight(int position) {

                // lida / n lida
                showDialogStatusNotificacao(notificacaoList.get(position));
            }
        });

    }

    private void showDialogStatusNotificacao(Notificacao notificacao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        View view = getLayoutInflater().inflate(R.layout.layout_dialog, null);

        TextView textTitulo = view.findViewById(R.id.textTitulo);
        TextView textMensagem = view.findViewById(R.id.textMensagem);

        if(notificacao.isLida()){
            textTitulo.setText("Deseja marcar essa notificação como NÃO LIDA?");
            textMensagem.setText("Aperte SIM para marcar essa notificação como NÃO LIDA ou clique em FECHAR para cancelar.");

        }else{
            textTitulo.setText("Deseja marcar essa notificação como LIDA?");
            textMensagem.setText("Aperte SIM para marcar essa notificação como LIDA ou clique em FECHAR para cancelar.");
        }

        //Botao da caixa de aviso
        view.findViewById(R.id.btnOk).setOnClickListener(v -> {

            notificacao.salvar();

            dialog.dismiss();
        });

        //dialog.dismiss faz com que assim que clica no botao, feche a tela
        view.findViewById(R.id.btnFechar).setOnClickListener(v -> {
            dialog.dismiss();
            notificacaoAdapter.notifyDataSetChanged();
        });

        //para exibir as view
        builder.setView(view);

        dialog = builder.create();
        dialog.show();

    }

    private void showDialogRemover(Notificacao notificacao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        View view = getLayoutInflater().inflate(R.layout.layout_dialog, null);

        //Botao da caixa de aviso
        view.findViewById(R.id.btnOk).setOnClickListener(v -> {
            removerNotificacoes(notificacao);
            dialog.dismiss();
        });

        //dialog.dismiss faz com que assim que clica no botao, feche a tela
        view.findViewById(R.id.btnFechar).setOnClickListener(v -> {
            dialog.dismiss();
            notificacaoAdapter.notifyDataSetChanged();
        });

        //para exibir as view
        builder.setView(view);

        dialog = builder.create();
        dialog.show();

    }

    private void removerNotificacoes(Notificacao notificacao){
        DatabaseReference notificacaoRef = FirebaseHelper.getDatabaseReference()
                .child("notificacoes")
                .child(FirebaseHelper.getIdFirebase())
                .child(notificacao.getId());
        notificacaoRef.removeValue().addOnCompleteListener(task -> {

            notificacaoList.remove(notificacao);

            if (notificacaoList.isEmpty()){
                textInfo.setText("Nenhuma notificação recebida.");

            }else{
                textInfo.setText("");
            }

            if (task.isSuccessful()){
                Toast.makeText(this, "Notificação removida com sucesso!", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Erro ao remover a notificação!", Toast.LENGTH_SHORT).show();
            }
            notificacaoAdapter.notifyDataSetChanged();
        });
    }

    private void recuperaNotificacoes(){
        DatabaseReference notificacaoRef = FirebaseHelper.getDatabaseReference()
                .child("notificacoes")
                .child(FirebaseHelper.getIdFirebase());
        notificacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    notificacaoList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Notificacao notificacao = ds.getValue(Notificacao.class);
                        notificacaoList.add(notificacao);
                    }
                    textInfo.setText("");
                }else {
                    textInfo.setText("Você não tem nenhuma notificação.");
                }

                Collections.reverse(notificacaoList);
                progressBar.setVisibility(View.GONE);
                notificacaoAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configToolbar() {

        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Notificações");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {
        progressBar = findViewById(R.id.progressBar);
        textInfo = findViewById(R.id.textInfo);
        rvNotificacoes = findViewById(R.id.rvNotificacoes);
    }


    @Override
    public void OnclickListener(Notificacao notificacao) {

        if (notificacao.getOperacao().equals("COBRANCA")){
            Intent intent = new Intent(this, PagarCobrancaActivity.class);
            intent.putExtra("notificacao", notificacao);
            startActivity(intent);
        }else if (notificacao.getOperacao().equals("TRANSFERENCIA")){
            Intent intent = new Intent(this, TransferenciaReciboActivity.class);
            intent.putExtra("idtransferencia", notificacao.getIdOperacao());
            startActivity(intent);
        }else {

        }
    }
}