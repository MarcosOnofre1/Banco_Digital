package com.example.bancodigital.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.FirebaseHelper;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Notificacao;
import com.example.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.MyViewHolder> {

    private final List<Notificacao> notificacaoList;
    private final Context context;
    private final Onclick onclick;

    public NotificacaoAdapter(List<Notificacao> notificacaoList, Context context, Onclick onclick) {
        this.notificacaoList = notificacaoList;
        this.context = context;
        this.onclick = onclick;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_notificacao, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Notificacao notificacao = notificacaoList.get(position);

        String titulo = "";
        switch (notificacao.getOperacao()) {
            case "COBRANÇA":
                titulo = "Você recebeu uma cobrança.";
                break;
            case "TRANSFERENCIA":
                titulo = "Você recebeu uma transferência.";
                break;
            case "PAGAMENTO":
                titulo = "Você recebeu um pagamento.";
                break;
        }

        holder.textTitulo.setText(titulo);
        holder.textData.setText(GetMask.getDate(notificacao.getData(), 3));

        if (notificacao.isLida()) {
            holder.textTitulo.setTypeface(null, Typeface.NORMAL);
            holder.textData.setTypeface(null, Typeface.NORMAL);

        } else {
            holder.textTitulo.setTypeface(null, Typeface.BOLD);
            holder.textData.setTypeface(null, Typeface.BOLD);
        }

        recuperaUsuario(notificacao, holder);

        holder.itemView.setOnClickListener(v -> onclick.OnclickListener(notificacao));

    }

    //aqui vamos identificar o nome do usuario nas notificações.
    private void recuperaUsuario(Notificacao notificacao, MyViewHolder holder) {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(notificacao.getIdEmitente());
        //aqui foi criado o ValueEventListener para monitorar em tempo real
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                if (usuario != null){
                    holder.textEmitente.setText(context.getString(R.string.text_enviada_por, usuario.getNome()));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return notificacaoList.size();
    }

    public interface Onclick {
        void OnclickListener(Notificacao notificacao);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textTitulo, textData, textEmitente, textMensagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textTitulo = itemView.findViewById(R.id.textTitulo);
            textData = itemView.findViewById(R.id.textData);
            textEmitente = itemView.findViewById(R.id.textEmitente);
            textMensagem = itemView.findViewById(R.id.textMensagem);


        }
    }
}