package com.example.shopease;

public class DataClass {

    private String dataTitle;
    private String imagemProduto;
    private String nomeProduto;

    private String quantidadeProduto;
    public String getDataTitle() {
        return dataTitle;
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



    public DataClass(String nomeProduto, String quantidadeProduto, String imagemProduto) {
        this.nomeProduto = nomeProduto;
        this.quantidadeProduto = quantidadeProduto;
        this.imagemProduto = imagemProduto;
    }
}
