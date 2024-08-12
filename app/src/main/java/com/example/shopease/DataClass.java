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



    public DataClass(String dataTitle, String imagemProduto, String nomeProduto) {
        this.dataTitle = dataTitle;
        this.imagemProduto = imagemProduto;
        this.nomeProduto = nomeProduto;
        this.quantidadeProduto = quantidadeProduto;
    }
}
