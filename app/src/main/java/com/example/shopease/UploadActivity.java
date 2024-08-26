package com.example.shopease;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class UploadActivity extends AppCompatActivity {

    ImageView uploadimg;
    Button saveButton;
    EditText nomeProduto, quantidadeProduto;
    String imageURL;
    Uri uri;
    ToggleButton toggleButton;
    Spinner categoriaProdutoSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uploadimg = findViewById(R.id.uploadimg);
        nomeProduto = findViewById(R.id.nomeProduto);
        quantidadeProduto = findViewById(R.id.quantidadeProduto);
        saveButton = findViewById(R.id.saveButton);
        toggleButton = findViewById(R.id.toggleButton);
        categoriaProdutoSpinner = findViewById(R.id.categoriaProdutoSpinner);

        // Lista de categorias para o Spinner de produto
        String[] categorias = {"Selecione uma categoria", "Secos/Mercearia" ,"Frios e Laticinios", "Sucos e Bebidas", "Biscoitos", "Doces e Guloseimas", "Massas", "Temperos e Molhos", "Óleos e Gorduras", "Carnes", "Congelados", "Peixes" ,"Hortifruti", "Limpeza", "Higiene Pessoal"};

        // Adapter para o Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriaProdutoSpinner.setAdapter(adapter);

        // Lógica do botão de Peso/Quantidade
        toggleButton.setChecked(false);
        quantidadeProduto.setHint("Quantidade:");
        quantidadeProduto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Iniciar aceitando somente inteiros

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Quando o toggleButton estiver marcado (modo "Peso")
                quantidadeProduto.setHint("Peso:");
                quantidadeProduto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL); // Aceitar números decimais
            } else {
                // Quando o toggleButton estiver desmarcado (modo "Quantidade")
                quantidadeProduto.setHint("Quantidade:");
                quantidadeProduto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Aceitar somente inteiros
            }
        });


        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            uri = data.getData();
                            uploadimg.setImageURI(uri);
                        }
                        else {
                            Toast.makeText(UploadActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        uploadimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }
    public void saveData(){
        if (uri == null){ // Fecha o UploadActivity ao não selecionar uma imagem para evitar Crash
            Toast.makeText(UploadActivity.this, "Preenha o campo de imagem!", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Android Images")
                .child(Objects.requireNonNull(uri.getLastPathSegment()));

        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlImage = uriTask.getResult();
                imageURL = urlImage.toString();
                uploadData();
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
            }
        });
    }
    public void uploadData(){

        String nome = nomeProduto.getText().toString();
        String quant = quantidadeProduto.getText().toString();
        float quantidadeValor = Float.parseFloat(quant);
        String sufixo;
        String categoria = categoriaProdutoSpinner.getSelectedItem().toString(); // Obter a categoria do produto pelo Spinner

        if ("Selecione uma categoria".equals(categoria)) { // Impossibilita de enviar categoria Vazia
            Toast.makeText(this, "Por favor, selecione uma categoria.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nome.isEmpty() || quant.isEmpty()) { // Impossibilita de enviar nome ou quantidade Vazio
            Toast.makeText(this, "Por favor preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (toggleButton.isChecked()) {
            // Quando estiver no modo "Peso"
            if (quantidadeValor > 10) {
                sufixo = " gramas"; // Define o sufixo como gramas
                quant = String.valueOf((int) quantidadeValor); // Remove casas decimais, se necessário
            } else {
                sufixo = " kg"; // Define o sufixo como kg
            }
        } else {
            // Quando estiver no modo "Quantidade"
            sufixo = " unidades";
        }
        String textoComSufixo = quant + sufixo;



        DataClass dataClass = new DataClass(nome, textoComSufixo, imageURL, categoria);
        dataClass.setCategoria(categoria); // Defina a categoria

        // Formatar a data para um formato seguro para o Firebase
        String currentDate = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());

        // Usar a combinação do nome do produto e data como chave
        String productKey = nome + "_" + currentDate;

        FirebaseDatabase.getInstance().getReference("produtos").child(productKey)
                .setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(UploadActivity.this, "Produto Salvo Com Sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}