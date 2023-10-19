package com.example.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Transferencia;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class TransferenciaConfirmaActivity extends AppCompatActivity {

    private TextView textValor;
    private TextView textUsuario;
    private ImageView imagemUsuario;
    private Usuario usuarioDestino;
    private Usuario usuarioOrigem;
    private Transferencia transferencia;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferencia_confirma);

        configToolbar();

        iniciaComponentes();

        configDados();

        recuperaUsuarioOrigem();

    }

    private void recuperaUsuarioOrigem() {
        if (transferencia != null && transferencia.getIdUserOrigem() != null) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(transferencia.getIdUserOrigem());
            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    usuarioOrigem = snapshot.getValue(Usuario.class);


                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    private void enviaNotificacao(String idOperacao) {
        Notificacao notificacao = new Notificacao();
        notificacao.setOperacao("TRANSFERENCIA");
        notificacao.setIdDestinario(usuarioDestino.getId());
        notificacao.setIdEmitente(usuarioOrigem.getId());
        notificacao.setIdOperacao(idOperacao);
        notificacao.enviar();

    }

    // AQUI VALIDA SE SEM OU NAO SALDO NA CONTA DO USUARIO
    public void confirmaTransferencia(View view) {

        if (transferencia != null) {
            if (usuarioOrigem.getSaldo() >= transferencia.getValor()) {

                //aqui é onde tira o saldo do usuario de origem e faz a transferencia e atualiza o seu saldo.
                usuarioOrigem.setSaldo(usuarioOrigem.getSaldo() - transferencia.getValor());
                usuarioOrigem.atualizarSaldo();

                // e aqui é onde o usuario do destino da transferencia recebe e atualiza o seu saldo.
                usuarioDestino.setSaldo(usuarioDestino.getSaldo() + transferencia.getValor());
                usuarioDestino.atualizarSaldo();

                // Origem
                salvarExtrato(usuarioOrigem, "SAIDA");

                // Destino
                salvarExtrato(usuarioDestino, "ENTRADA");

            } else {
                showDialog("Saldo insuficiente.");
            }
        }
    }

    private void salvarExtrato(Usuario usuario, String tipo) {
        Extrato extrato = new Extrato();
        extrato.setOperacao("TRANSFERENCIA");
        extrato.setValor(transferencia.getValor());
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

                salvarTransferencia(extrato);

            } else {
                showDialog("Não foi possível efetuar o deposito, tente mais tarde.");

            }
        });


    }

    private void salvarTransferencia(Extrato extrato) {

        transferencia.setId(extrato.getId());

        DatabaseReference transferenciaRef = FirebaseHelper.getDatabaseReference()
                .child("transferencias")
                .child(transferencia.getId());
        transferenciaRef.setValue(transferencia).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                DatabaseReference updateTransferencia = transferenciaRef
                        .child("data");
                updateTransferencia.setValue(ServerValue.TIMESTAMP);

                if (extrato.getTipo().equals("ENTRADA")) {

                    enviaNotificacao(extrato.getId());

                    Intent intent = new Intent(this, TransferenciaReciboActivity.class);
                    intent.putExtra("idTransferencia", transferencia.getId());
                    startActivity(intent);
                }

            } else {
                showDialog("Não foi possível completar a transferencia");
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

    private void configDados() {

        usuarioDestino = (Usuario) getIntent().getSerializableExtra("usuario");
        transferencia = (Transferencia) getIntent().getSerializableExtra("transferencia");

        textUsuario.setText(usuarioDestino.getNome());
        if (usuarioDestino.getUrlImagem() != null) {
            Picasso.get().load(usuarioDestino.getUrlImagem())
                    .placeholder(R.drawable.loading)
                    .into(imagemUsuario);
        }

        // Verifica se a transferência não é nula antes de acessar os métodos
        if (transferencia != null) {
            // Certifique-se de que o valor retornado por GetMask.getValor() seja o esperado.
            String valorFormatado = GetMask.getValor(transferencia.getValor());
            textValor.setText(getString(R.string.text_valor, valorFormatado));
        }

    }

    private void configToolbar() {

        //para adicionar o titulo na toolbar, é necessario passar para TextView = findViewById
        // e dps o nome do TextView.setText("nome do titulo")
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Confirma os Dados");
    }

    private void iniciaComponentes() {

        textValor = findViewById(R.id.textValor);
        textUsuario = findViewById(R.id.textUsuario);
        imagemUsuario = findViewById(R.id.imagemUsuario);

    }


}