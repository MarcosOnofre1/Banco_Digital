package com.example.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bancodigital.R;
import com.example.bancodigital.adapter.UsuarioAdapter;
import com.example.bancodigital.cobrar.CobrancaConfirmaActivity;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.model.Cobranca;
import com.example.bancodigital.model.Transferencia;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelecionarUsuarioActivity extends AppCompatActivity implements UsuarioAdapter.OnClick {

    private UsuarioAdapter usuarioAdapter;
    private final List<Usuario> usuarioList = new ArrayList<>();
    private RecyclerView rvUsuarios;


    private EditText edtPesquisa;
    private TextView textPesquisa;
    private TextView textLimpar;
    private String pesquisa = "";
    private LinearLayout llPesquisa;

    private TextView textInfo;
    private ProgressBar progressBar;
    private Transferencia transferencia;
    private Cobranca cobranca;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_usuario);

        configToolbar();

        iniciaComponentes();

        configRv();

        recuperaUsuarios();

        configPesquisa();

        configCliques();

        getExtra();

    }

    private void getExtra(){
        if (getIntent().hasExtra("transferencia")){
            transferencia = (Transferencia) getIntent().getSerializableExtra("transferencia");
        }else if (getIntent().hasExtra("cobranca")){
            cobranca = (Cobranca) getIntent().getSerializableExtra("cobranca");
        }
    }

    private void configCliques(){
        textLimpar.setOnClickListener(v -> {
            pesquisa = "";
            configFiltro();
            recuperaUsuarios();
            ocutarTeclado();
        });
    }

    private void configPesquisa(){

        edtPesquisa.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH){

                ocutarTeclado();

                progressBar.setVisibility(View.VISIBLE);

                //seria a informação que o usuario digitou na editText
                pesquisa = v.getText().toString();

                if(!pesquisa.equals("")){

                    configFiltro();

                    pesquisarUsuarios();

                }else{
                    recuperaUsuarios();

                    configFiltro();
                }
            }
            return false;
        });
    }

    private void pesquisarUsuarios() {
        // so irá deixar na lista os usuarios que o nome deles corresponde ao que foi pesquisado
        for(Usuario usuario : new ArrayList<>(usuarioList)){

            //toLowerCase = Usa-se para nao ter interferencia na hora da pesquisa se esta em Maisúsculo ou Minúsculo o nome do usuario.
            if (!usuario.getNome().toLowerCase().contains(pesquisa.toLowerCase())){
                usuarioList.remove(usuario);
            }
        }

        // significa que nao tem nenhum usuario encontrado com esse nome
        if(usuarioList.isEmpty()){
            textInfo.setText("Nenhum usuário encontrado com este nome.");
        }

        progressBar.setVisibility(View.GONE);
        usuarioAdapter.notifyDataSetChanged();

    }

    // no caso so temos 1 tipo de filtro que é o de pesquisa, mas tem a possibilidade de config varios tipos de filtros aqui
    private void configFiltro(){

        if(!pesquisa.equals("")){
            textPesquisa.setText("Pesquisa: " + pesquisa);
            llPesquisa.setVisibility(View.VISIBLE);
        }else {
            textPesquisa.setText("");
            llPesquisa.setVisibility(View.GONE);
        }
    }

    private void recuperaUsuarios() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios");
        //addListenerForSing que ira fazer a pesquisa uma vez
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    //aqui ele limpa a duplicação que daria depois da pesquisa do usuario e volta ao normal
                    usuarioList.clear();

                    for (DataSnapshot ds : snapshot.getChildren()){
                        // aqui ele vai recuperar o usuario
                        Usuario usuario = ds.getValue(Usuario.class);

                        // se o usuario for diferente de nulo, ele vai fazer a validação, ou seja
                        // so vai adicionar esse usuario na lista se ele for diferente do meu ID, entao faz esse "if"
                        if (usuario != null){
                            // a validação seria se esse "usuario" que ele acabou de recuperar, se o ID dele é diferente do ID do usuario que estiver logado
                            if (!usuario.getId().equals(FirebaseHelper.getIdFirebase())){
                                usuarioList.add(usuario);
                            }

                        }
                    }
                    textInfo.setText("");
                }else {

                    textInfo.setText("Nenhum usuário cadastrado.");
                }
                progressBar.setVisibility(View.GONE);
                usuarioAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configRv(){
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvUsuarios.setHasFixedSize(true);
        usuarioAdapter = new UsuarioAdapter(usuarioList, this);
        rvUsuarios.setAdapter(usuarioAdapter);
    }

    private void configToolbar() {

        //para adicionar o titulo na toolbar, é necessario passar para TextView = findViewById
        // e dps o nome do TextView.setText("nome do titulo")
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Selecione Usuário");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> finish());
    }

    private void ocutarTeclado(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edtPesquisa.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void iniciaComponentes(){
        rvUsuarios = findViewById(R.id.rvUsuarios);
        textInfo = findViewById(R.id.textInfo);
        progressBar = findViewById(R.id.progressBar);
        edtPesquisa = findViewById(R.id.edtPesquisa);
        textPesquisa = findViewById(R.id.textPesquisa);
        textLimpar = findViewById(R.id.textLimpar);
        llPesquisa = findViewById(R.id.llPesquisa);
    }

    @Override
    public void OnclickListener(Usuario usuario) {

        String idUsuario = usuario.getId();

        if (transferencia != null){

            transferencia.setIdUserDestino(idUsuario);
            Intent intent = new Intent(this, TransferenciaConfirmaActivity.class);
            intent.putExtra("transference", transferencia);
            intent.putExtra("usuario", usuario);
            startActivity(intent);

        }else if (cobranca != null){

            cobranca.setIdDestinatario(idUsuario);
            Intent intent = new Intent(this, CobrancaConfirmaActivity.class);
            intent.putExtra("cobranca", cobranca);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        }
    }
}