package com.example.bancodigital.transferencia;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Transferencia;

import java.util.Locale;

public class TransferenciaFormActivity extends AppCompatActivity {

    private CurrencyEditText edtValor;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferencia_form);

        configToolbar();

        iniciaComponentes();

    }

    public void validaDados(View view){
        double valor = (double) edtValor.getRawValue() / 100;

        if (valor > 0){

            ocutarTeclado();

            Transferencia transferencia = new Transferencia();
            transferencia.setIdUserOrigem(FirebaseHelper.getIdFirebase());
            transferencia.setValor(valor);

            Intent intent = new Intent(this, TransferirUsuarioActivity.class);
            intent.putExtra("transferencia", transferencia);
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
        mensagem.setText("Digite um valor maior que 0.");


        //Botao da caixa de aviso
        Button btnOk = view.findViewById(R.id.btnOk);
        //dialog.dismiss faz com que assim que clica no botao, feche a tela
        btnOk.setOnClickListener(v -> dialog.dismiss());

        //para exibir as view
        builder.setView(view);

        dialog = builder.create();
        dialog.show();

    }

    private void configToolbar() {

        //para adicionar o titulo na toolbar, é necessario passar para TextView = findViewById
        // e dps o nome do TextView.setText("nome do titulo")
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Transferir");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> finish());
    }

    private void ocutarTeclado(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edtValor.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void iniciaComponentes() {
        edtValor = findViewById(R.id.edtValor);
        //aqui usando esse metodo para ja ficar em Reais na nossa editText
        edtValor.setLocale(new Locale("PT", "br"));
    }

}