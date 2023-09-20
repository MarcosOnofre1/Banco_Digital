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
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DatabaseReference;

public class CadastroActivity extends AppCompatActivity {

    private EditText edtNome;
    private EditText edtEmail;
    private EditText edtTelefone;
    private EditText edtSenha;
    private EditText edtConfirmaSenha;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        iniciaComponentes();


    }

    public void validaDados(View view) {
        String nome = edtNome.getText().toString();
        String email = edtEmail.getText().toString();
        String telefone = edtTelefone.getText().toString();
        String senha = edtSenha.getText().toString();
        String confirmaSenha = edtConfirmaSenha.getText().toString();

        if (!nome.isEmpty()) {
            if (!email.isEmpty()) {
                if (!telefone.isEmpty()) {
                    if (!senha.isEmpty()) {
                        if (!confirmaSenha.isEmpty()) {

                            // ESSE "equals" VERIFICA SE AS SENHA + CONFIRMASENHA SAO IGUAIS.
                            if (senha.equals(confirmaSenha)) {

                                progressBar.setVisibility(View.VISIBLE);

                                Usuario usuario = new Usuario();
                                usuario.setNome(nome);
                                usuario.setEmail(email);
                                usuario.setTelefone(telefone);
                                usuario.setSenha(senha);
                                usuario.setSaldo((double) 0);

                                cadastrarUsuario(usuario);

                            } else {
                                edtSenha.setError("Senhas diferentes.");
                                edtConfirmaSenha.setError("Senhas diferentes");
                            }

                        } else {
                            edtConfirmaSenha.requestFocus();
                            edtConfirmaSenha.setError("Confirme sua senha.");
                        }
                    } else {
                        edtSenha.requestFocus();
                        edtSenha.setError("Informe sua senha.");
                    }
                } else {
                    edtTelefone.requestFocus();
                    edtTelefone.setError("Informe o numero de telefone.");
                }
            } else {
                edtEmail.requestFocus();
                edtEmail.setError("Informe seu e-mail.");
            }
        } else {
            edtNome.requestFocus();
            edtNome.setError("Informe seu nome.");
        }

    }

    //Aqui no createUserWithEmailAndPassword ele recebe o email e a senha "getEmail" e "getSenha" e depois completa com .addCompleteListener
    //pra completar a lista e dps com IF ve se deu tudo certo na criação do cadastro do usuario.
    private void cadastrarUsuario(Usuario usuario) {
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                //aqui ele vai armazenar o id do usuario que acabou de se cadastrar
                String id = task.getResult().getUser().getUid();
                usuario.setId(id);

                //se tudo ocorrer certo, ele chama o "salvarDadosUsuario"
                salvarDadosUsuario(usuario);

            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

            }

        });

    }

    private void salvarDadosUsuario(Usuario usuario) {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                        .child("usuarios")
                        .child(usuario.getId());
        usuarioRef.setValue(usuario).addOnCompleteListener(task -> {
           if(task.isComplete()){
               finish();
               startActivity(new Intent(this, MainActivity.class));

           }else {
               progressBar.setVisibility(View.GONE);
               Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
           }
        });

    }

    private void iniciaComponentes() {
        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmaSenha = findViewById(R.id.edtConfirmaSenha);
        progressBar = findViewById(R.id.progressBar);
    }

}