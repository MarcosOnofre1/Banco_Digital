package com.example.bancodigital.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bancodigital.R;
import com.example.bancodigital.helper.GetMask;
import com.example.bancodigital.model.Extrato;

import java.util.List;

public class ExtratoAdapter extends RecyclerView.Adapter<ExtratoAdapter.MyViewHolder> {


    private List<Extrato> extratoList;
    private Context context;

    public ExtratoAdapter(List<Extrato> extratoList, Context context) {
        this.extratoList = extratoList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_extrato, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Extrato extrato = extratoList.get(position);

        // esse getOperacao vai ter o nome da operação EX: "DEPOSITO", "RECARGA", "TRANSF." e etc. Ja o substring(0 <-ZERO) vai pegar
        // a primeira letra da operação, ou seja "D", "R", "T".
        String icon = extrato.getOperacao().substring(0, 1);
        holder.textIcon.setText(icon);
        holder.textOperacao.setText(extrato.getOperacao());
        holder.textData.setText(GetMask.getDate(extrato.getData(), 4));
        holder.textValor.setText(context.getString(R.string.text_valor, GetMask.getValor(extrato.getValor())));

        //ENTRADAS E SAIDAS:

        switch (extrato.getOperacao()) {
            case "DEPOSITO":
                holder.textIcon.setBackgroundResource(R.drawable.bg_entrada);
                break;
            case "RECARGA":
                holder.textIcon.setBackgroundResource(R.drawable.bg_saida);
                break;
            // VAI TER UMA CORREÇÃO AQUI MAIS PRA FRENTE
            case "TRANSFERENCIA":
                if (extrato.getTipo().equals("ENTRADA")) {
                    holder.textIcon.setBackgroundResource(R.drawable.bg_entrada);
                } else {
                    holder.textIcon.setBackgroundResource(R.drawable.bg_saida);
                }
                break;


        }


    }

    @Override
    public int getItemCount() {
        return extratoList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textIcon, textOperacao, textData, textValor;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textIcon = itemView.findViewById(R.id.textIcon);
            textOperacao = itemView.findViewById(R.id.textOperacao);
            textData = itemView.findViewById(R.id.textData);
            textValor = itemView.findViewById(R.id.textValor);

        }
    }
}
