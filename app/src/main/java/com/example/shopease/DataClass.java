package com.example.shopease;

public class DataClass {

    private String imagemProduto;
    private String nomeProduto;
    private String quantidadeProduto;
    private String key;
    private String categoria;  // Campo de categoria

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImagemProduto() {
        return imagemProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public String getQuantidadeProduto() {
        return quantidadeProduto;
    }

    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }


    public DataClass(String nomeProduto, String quantidadeProduto, String imagemProduto) {
        this.nomeProduto = nomeProduto;
        this.quantidadeProduto = quantidadeProduto;
        this.imagemProduto = imagemProduto;
    }
    public DataClass(){

    }
}
