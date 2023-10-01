package com.example.bancodigital.cobrar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Cobranca;
import com.example.bancodigital.transferencia.SelecionarUsuarioActivity;

import java.util.Locale;

public class CobrarFormActivity extends AppCompatActivity {

    private CurrencyEditText edtValor;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobrar_form);

        iniciaComponentes();

        configToolbar();
    }

    public void continuar(View view){

        double valorCobranca = (double) edtValor.getRawValue() / 100;

        if(valorCobranca > 0){

            Cobranca cobranca = new Cobranca();
            cobranca.setIdEmitente(FirebaseHelper.getIdFirebase());
            cobranca.setValor(valorCobranca);

            Intent intent = new Intent(this, SelecionarUsuarioActivity.class);
            intent.putExtra("cobranca", cobranca);
            startActivity(intent);

        }else {
            showDialog();
        }


    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_info, null);


        //titulo e corpo da caixa de janela quando aparecer o erro
        TextView textTitulo = view.findViewById(R.id.textTitulo);
        textTitulo.setText("Atenção");

        TextView mensagem = view.findViewById(R.id.textMensagem);
        mensagem.setText("Por favor \nDigite um valor maior que 0.");


        //Botao da caixa de aviso
        Button btnOk = view.findViewById(R.id.btnOk);
        //dialog.dismiss faz com que assim que clica no botao, feche a tela
        btnOk.setOnClickListener(v -> dialog.dismiss());

        //para exibir as view
        builder.setView(view);

        dialog = builder.create();
        dialog.show();

    }

    private void iniciaComponentes() {
        edtValor = findViewById(R.id.edtValor);
        //aqui usando esse metodo para ja ficar em Reais na nossa editText
        edtValor.setLocale(new Locale("PT", "br"));
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Receber");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> finish());
    }
}