package com.example.bancodigital.autenticacao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bancodigital.app.MainActivity;
import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtSenha;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        iniciaComponentes();

    }

    // aqui vai ser clicado no botao de "logar" e verificar pengando as informaçoes de email e a senha
    // exibe a progressBar e chama o metodo de "Logar"
    public void validaDados(View view){

        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();

        if(!email.isEmpty()){
            if(!senha.isEmpty()){

                progressBar.setVisibility(View.VISIBLE);

                //*esse seria o metodo*
                logar(email, senha);

            }else {
                edtSenha.requestFocus();
                edtSenha.setError("Informe sua senha.");
            }
        }else {
            edtEmail.requestFocus();
            edtEmail.setError("Informe seu e-mail.");
        }

    }

    // aqui é passado o email e senha, caso o "logar" seja com sucesso, ele finaliza a Activity de "Login" e
    // ele manda pra tela do MainActivity, caso contrario, exibe uma mensagem de erro

    private void logar(String email, String senha) {
        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {

            if (task.isSuccessful()){

                finish();
                startActivity(new Intent(this, MainActivity.class));

            }else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // click do TextView do criar conta para ir no cadastro
    public void criarConta(View view){
        startActivity(new Intent(this, CadastroActivity.class));

    }

    public void recuperarConta(View view){
        startActivity(new Intent(this, RecuperarContaActivity.class));

    }

    private void iniciaComponentes(){
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        progressBar = findViewById(R.id.progressBar);
    };
}