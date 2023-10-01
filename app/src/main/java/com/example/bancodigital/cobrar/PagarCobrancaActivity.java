package com.example.bancodigital.cobrar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bancodigital.R;
import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Cobranca;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Pagamento;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PagarCobrancaActivity extends AppCompatActivity {

    private TextView textValor;
    private TextView textData;
    private TextView textUsuario;
    private ImageView imagemUsuario;
    private ProgressBar progressBar;

    private AlertDialog dialog;

    private Usuario usuarioDestino;
    private Usuario usuarioOrigem;
    private Cobranca cobranca;
    private Notificacao notificacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagar_cobranca);

        configToolbar();

        iniciaComponentes();

        recuperaUsuarioOrigem();

        getExtra();

    }

    public void confirmaPagamento(View view) {
        if (cobranca != null){
            if (!cobranca.isPaga()){
                if (usuarioDestino != null && usuarioOrigem != null) {
                    if (usuarioOrigem.getSaldo() >= cobranca.getValor()) {

                        //aqui é onde tira o saldo do usuario de origem e faz a cobranca e atualiza o seu saldo.
                        usuarioOrigem.setSaldo(usuarioOrigem.getSaldo() - cobranca.getValor());
                        usuarioOrigem.atualizarSaldo();

                        // e aqui é onde o usuario do destino da cobranca recebe e atualiza o seu saldo.
                        usuarioDestino.setSaldo(usuarioDestino.getSaldo() + cobranca.getValor());
                        usuarioDestino.atualizarSaldo();

                        // JA PAGO
                        atualizaStatusCobranca();

                        //Slava no Extrato do usuário que ENVIOU o pagamento
                        salvarExtrato(usuarioOrigem, "SAIDA");

                        //Salva no Extrato do usuário que RECEBEU o pagamento
                        salvarExtrato(usuarioDestino, "ENTRADA");

                    } else {
                        showDialog("Saldo insuficiente.");
                    }
                } else {
                    Toast.makeText(this, "Ainda estamos recuperando as informações.", Toast.LENGTH_SHORT).show();
                }
            }else{
                showDialog("O pagamento já foi realizado para essa cobrança.");
            }
        }else {
            Toast.makeText(this, "Ainda estamos recuperando as informações.", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarExtrato(Usuario usuario, String tipo) {
        Extrato extrato = new Extrato();
        extrato.setOperacao("PAGAMENTO");
        extrato.setValor(cobranca.getValor());
        extrato.setTipo(tipo);

        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(usuario.getId())
                .child(extrato.getId());

        extratoRef.setValue(extrato).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                //registra a data do evento quando foi efetuado no firebase
                DatabaseReference updateExtrato = extratoRef
                        .child("data");
                updateExtrato.setValue(ServerValue.TIMESTAMP);

                salvarPagamento(extrato);

            } else {
                showDialog("Não foi possível efetuar o pagamento, tente mais tarde.");

            }
        });


    }

    private void salvarPagamento(Extrato extrato) {

        Pagamento pagamento = new Pagamento();
        pagamento.setId(extrato.getId());
        pagamento.setIdCobranca(cobranca.getId());
        pagamento.setValor(cobranca.getValor());
        pagamento.setIdUserDestino(usuarioDestino.getId());
        pagamento.setIdUserOrigem(usuarioOrigem.getId());

        DatabaseReference pagamentoRef = FirebaseHelper.getDatabaseReference()
                .child("pagamentos")
                .child(extrato.getId());
        pagamentoRef.setValue(pagamento).addOnCompleteListener(aVoid -> {

            DatabaseReference update = pagamentoRef.child("data");
            update.setValue(ServerValue.TIMESTAMP);

        });

        if (extrato.getTipo().equals("ENTRADA")) {
            configNotificao(extrato.getId());

        } else {
            Intent intent = new Intent(this, ReciboPagamentoActivity.class);
            intent.putExtra("idPagamento", pagamento.getId());
            startActivity(intent);
        }


    }

    private void atualizaStatusCobranca() {
        DatabaseReference cobrancaRef = FirebaseHelper.getDatabaseReference()
                .child("cobrancas")
                .child(FirebaseHelper.getIdFirebase())
                .child(notificacao.getIdOperacao())
                .child("paga");
        //registra que essa cobrança ja foi paga.
        cobrancaRef.setValue(true);
    }

    private void getExtra() {
        notificacao = (Notificacao) getIntent().getSerializableExtra("notificacao");

        recuperaCobranca();

    }

    private void recuperaCobranca() {
        DatabaseReference cobrancaRef = FirebaseHelper.getDatabaseReference()
                .child("cobrancas")
                .child(FirebaseHelper.getIdFirebase())
                .child(notificacao.getIdOperacao());
        cobrancaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cobranca = snapshot.getValue(Cobranca.class);

                recuperaUsuarioDestino();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    // Configura Notificação
    private void configNotificao(String idOperecao) {
        Notificacao notificacao = new Notificacao();
        notificacao.setIdOperacao(idOperecao);
        notificacao.setIdDestinario(usuarioDestino.getId());
        notificacao.setIdEmitente(usuarioOrigem.getId());
        notificacao.setOperacao("PAGAMENTO");

        // Envia notificação para o usuario que ira receber a cobrança
        enviarNotificacao(notificacao);
    }

    // Envia notificação para o usuario que ira receber o pagamento
    private void enviarNotificacao(Notificacao notificacao) {
        DatabaseReference noticacaoRef = FirebaseHelper.getDatabaseReference()
                .child("notificacoes")
                .child(notificacao.getIdDestinario())
                .child(notificacao.getId());
        noticacaoRef.setValue(notificacao).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                DatabaseReference updateRef = noticacaoRef
                        .child("data");
                updateRef.setValue(ServerValue.TIMESTAMP);

            } else {

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_info, null);


        //titulo e corpo da caixa de janela quando aparecer o erro
        TextView textTitulo = view.findViewById(R.id.textTitulo);
        textTitulo.setText("Atenção");

        TextView mensagem = view.findViewById(R.id.textMensagem);
        mensagem.setText(msg);


        //Botao da caixa de aviso
        Button btnOk = view.findViewById(R.id.btnOk);
        //dialog.dismiss faz com que assim que clica no botao, feche a tela
        btnOk.setOnClickListener(v -> dialog.dismiss());

        //para exibir as view
        builder.setView(view);

        dialog = builder.create();
        dialog.show();

    }

    // USUARIO QUE FEZ A COBRANÇA
    private void recuperaUsuarioDestino() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(cobranca.getIdEmitente());
        //aqui foi criado o ValueEventListener para monitorar em tempo real
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuarioDestino = snapshot.getValue(Usuario.class);
                configDados();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperaUsuarioOrigem() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(FirebaseHelper.getIdFirebase());
        //aqui foi criado o ValueEventListener para monitorar em tempo real
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuarioOrigem = snapshot.getValue(Usuario.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configDados() {

        // recupera o nome e a imagem do usuario
        textUsuario.setText(usuarioDestino.getNome());
        if (usuarioDestino.getUrlImagem() != null) {
            Picasso.get().load(usuarioDestino.getUrlImagem())
                    .placeholder(R.drawable.drloading)
                    .into(imagemUsuario);
        }

        textData.setText(GetMask.getDate(cobranca.getData(), 3));
        textValor.setText(getString(R.string.text_valor, GetMask.getValor(cobranca.getValor())));
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Confirma os Dados");
        findViewById(R.id.ibVoltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {

        textValor = findViewById(R.id.textValor);
        textData = findViewById(R.id.textData);
        textUsuario = findViewById(R.id.textUsuario);
        imagemUsuario = findViewById(R.id.imagemUsuario);
        progressBar = findViewById(R.id.progressBar);

    }


}