package com.example.bancodigital.cobrar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Transferencia;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CobrancaConfirmaActivity extends AppCompatActivity {

    private TextView textValor;
    private TextView textUsuario;
    private ImageView imagemUsuario;
    private ProgressBar progressBar;
    private Cobranca cobranca;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranca_confirma);

        iniciaComponentes();

        configToolbar();

        configDados();


    }

    public void confirmaCobranca(View view) {

        DatabaseReference cobrancaRef = FirebaseHelper.getDatabaseReference()
                .child("cobrancas")
                .child(cobranca.getIdDestinatario())
                .child(cobranca.getId());
        cobrancaRef.setValue(cobranca).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                progressBar.setVisibility(View.VISIBLE);

                DatabaseReference updateRef = cobrancaRef
                        .child("data");
                updateRef.setValue(ServerValue.TIMESTAMP);

                // configura notificacao
                configNotificao();

            } else {
                showDialog();
            }
        });
    }

    // Configura Notificação
    private void configNotificao() {
        Notificacao notificacao = new Notificacao();
        notificacao.setIdOperacao(cobranca.getId());
        notificacao.setIdDestinario(cobranca.getIdDestinatario());
        notificacao.setIdEmitente(FirebaseHelper.getIdFirebase());
        notificacao.setOperacao("COBRANCA");

        // Envia notificação para o usuario que ira receber a cobrança
        enviarNotificacao(notificacao);
    }

    // Envia notificação para o usuario que ira receber a cobrança
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

                Toast.makeText(this, "Cobrança enviada com sucesso!", Toast.LENGTH_SHORT).show();

                // Usando esse metodo, faz com que todas as activity que esta acumulada (um por cima da outra) seja limpa e volta p/ tela inicial.
                Intent intent = new Intent(this, MainActivity.class);
                //aqui perguntar sobre oque por aqui pra dividir a flag
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } else {

                progressBar.setVisibility(View.GONE);
                showDialog();
            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_info, null);


        //titulo e corpo da caixa de janela quando aparecer o erro
        TextView textTitulo = view.findViewById(R.id.textTitulo);
        textTitulo.setText("Atenção");

        TextView mensagem = view.findViewById(R.id.textMensagem);
        mensagem.setText("Não foi possivel salvar os dados \ntente novamente mais tarde.");


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
        Usuario usuarioDestino = (Usuario) getIntent().getSerializableExtra("usuario");
        cobranca = (Cobranca) getIntent().getSerializableExtra("cobranca");

        // recupera o nome e a imagem do usuario
        textUsuario.setText(usuarioDestino.getNome());
        if (usuarioDestino.getUrlImagem() != null) {
            Picasso.get().load(usuarioDestino.getUrlImagem())
                    .placeholder(R.drawable.drloading)
                    .into(imagemUsuario);
        }

        textValor.setText(getString(R.string.text_valor, GetMask.getValor(cobranca.getValor())));
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Confirma os Dados");
    }

    private void iniciaComponentes() {

        textValor = findViewById(R.id.textValor);
        textUsuario = findViewById(R.id.textUsuario);
        imagemUsuario = findViewById(R.id.imagemUsuario);
        progressBar = findViewById(R.id.progressBar);

    }
}