package com.example.bancodigital.model;

import com.example.bancodigital.helper.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Extrato {

    private String id;

    //aqui a operação seria, se acaso fazar um "deposito", a operacao vai ser "deposito", fizer uma "recarga de celular"
    // a operaçao é "recarga de celular" e etc.
    private String operacao;
    private long data;
    private double valor;

    //aqui o tipo seria, se por acaso eu fizer um deposito, meu "tipo" é "ENTRADA", se fizer um pagamento, meu "tipo é "SAIDA" e etc.
    private String tipo;

    public Extrato() {
        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference();
        setId(extratoRef.push().getKey());

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
