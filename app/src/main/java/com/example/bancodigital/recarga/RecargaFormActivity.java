package com.example.bancodigital.recarga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.bancodigital.R;
import com.example.bancodigital.deposito.DepositoReciboActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Deposito;
import com.example.bancodigital.model.Extrato;
import com.example.bancodigital.model.Recarga;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.santalu.maskara.widget.MaskEditText;

import java.util.Locale;

public class RecargaFormActivity extends AppCompatActivity {

    private CurrencyEditText edtValor;
    private MaskEditText edtTelefone;
    private AlertDialog dialog;
    private ProgressBar progressBar;

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga_form);

        iniciaComponentes();

        configToolbar();

        ocutarTeclado();

        recuperaUsuario();

    }

    public void validaDados(View view) {
        double valor = (double) edtValor.getRawValue() / 100;
        //lembrando que esse .trim() é nao conter espaçamentos na digitação
        //getUnMasked = recupera a informação sem a maskara, ou seja, ali contaria 11 numeros fora a maskara.
        String numero = edtTelefone.getUnMasked();

        if (valor >= 15) {
            if (!numero.isEmpty()) {
                if (numero.length() == 11) {
                    if (usuario != null){
                        if (usuario.getSaldo() >= valor){

                            progressBar.setVisibility(View.VISIBLE);

                            salvarExtrato(valor, numero);

                        }else {
                            showDialog("Saldo insuficiente.");
                        }

                    }else {
                        showDialog("Aguarde \nAinda estamos recuperando as informações.");
                    }

                } else {
                    showDialog("O número informado está incompleto.");
                }
            } else {
                showDialog("Por favor \nInforme um número.");
            }
        } else {
            showDialog("Recarga mínima de R$ 15,00.");
        }
    }

    private void recuperaUsuario() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(FirebaseHelper.getIdFirebase());
        //aqui foi criado o ValueEventListener para monitorar em tempo real
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuario = snapshot.getValue(Usuario.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    private void salvarRecarga(Extrato extrato, String numero) {

        Recarga recarga = new Recarga();
        recarga.setId(extrato.getId());
        recarga.setNumero(numero);
        recarga.setValor(extrato.getValor());

        DatabaseReference recargaRef = FirebaseHelper.getDatabaseReference()
                .child("recargas")
                .child(recarga.getId());

        recargaRef.setValue(recarga).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                //mesma coisa do "SalvarExtrato" ele atualiza a data do deposito
                DatabaseReference updateRecarga = recargaRef
                        .child("data");
                updateRecarga.setValue(ServerValue.TIMESTAMP);

                //aqui entramos na activity da recarga
                Intent intent = new Intent(this, RecargaReciboActivity.class);
                intent.putExtra("idRecarga", recarga.getId());
                startActivity(intent);
                finish();


            } else {
                progressBar.setVisibility(View.GONE);
                showDialog("Não foi possível efetuar a recarga, tente mais tarde.");
            }

        });


    }

    private void salvarExtrato(double valor, String numero) {
        Extrato extrato = new Extrato();
        extrato.setOperacao("RECARGA");
        extrato.setValor(valor);
        extrato.setTipo("SAIDA");

        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase())
                .child(extrato.getId());

        extratoRef.setValue(extrato).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                usuario.setSaldo(usuario.getSaldo() - extrato.getValor());
                usuario.atualizarSaldo();

                //registra a data do evento quando foi efetuado no firebase
                DatabaseReference updateExtrato = extratoRef
                        .child("data");
                updateExtrato.setValue(ServerValue.TIMESTAMP);

                salvarRecarga(extrato, numero);

            } else {
                showDialog("Não foi possível efetuar o deposito, tente mais tarde.");

            }
        });


    }


    private void configToolbar() {

        //para adicionar o titulo na toolbar, é necessario passar para TextView = findViewById
        // e dps o nome do TextView.setText("nome do titulo")
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Recarga");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> finish());
    }

    private void ocutarTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edtValor.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void iniciaComponentes() {
        edtValor = findViewById(R.id.edtValor);
        //aqui usando esse metodo para ja ficar em Reais na nossa editText
        edtValor.setLocale(new Locale("PT", "br"));

        edtTelefone = findViewById(R.id.edtTelefone);

        progressBar = findViewById(R.id.progressBar);
    }

}