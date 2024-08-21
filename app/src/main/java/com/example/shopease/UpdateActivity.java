package com.example.shopease;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class UpdateActivity extends AppCompatActivity {

    ImageView updateImage;
    Button updateButton;
    EditText updateDesc, updateTitle;
    ToggleButton updateToggleButton;
    String title, desc;
    String imageUrl;
    String key, oldImageURL;
    Uri uri;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    Spinner updateCategorySpinner;
    String selectedCategory; // Para armazenar a categoria selecionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        updateButton = findViewById(R.id.updateButton);
        updateDesc = findViewById(R.id.updateQuant);
        updateImage = findViewById(R.id.updateImage);
        updateTitle = findViewById(R.id.updateNome);
        updateToggleButton = findViewById(R.id.updateToggleButton);
        updateCategorySpinner = findViewById(R.id.updateCategorySpinner); // Spinner

        // Configuração do Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.categorias_array, // Certifique-se de que esse array existe no strings.xml
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        updateCategorySpinner.setAdapter(adapter);

        updateToggleButton.setChecked(false);
        updateDesc.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Iniciar aceitando somente inteiros
        updateToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Quando o toggleButton estiver marcado (modo "Peso")
                updateDesc.setHint("Peso:");
                updateDesc.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL); // Aceitar números decimais
            } else {
                // Quando o toggleButton estiver desmarcado (modo "Quantidade")
                updateDesc.setHint("Quantidade:");
                updateDesc.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Aceitar somente inteiros
            }
        });


        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            assert data != null;
                            uri = data.getData();
                            updateImage.setImageURI(uri);
                        }
                        else{
                            Toast.makeText(UpdateActivity.this, "Imagem não selecionada", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            Glide.with(UpdateActivity.this).load(bundle.getString("imagemProduto")).into(updateImage);
            updateTitle.setText(bundle.getString("nomeProduto"));
            String quantidadeComSufixo = bundle.getString("quantidadeProduto");
            String quantidadeSemSufixo = removerSufixo(quantidadeComSufixo);
            updateDesc.setText(quantidadeSemSufixo);

            // Armazenar valores antigos
            key = bundle.getString("key");
            oldImageURL = bundle.getString("imagemProduto");
        }

        // Registrar listener para mudanças na categoria
        updateCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                Log.d("UpdateData", "Categoria selecionada no onItemSelected: " + selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Caso nada seja selecionado, podemos manter a categoria atual
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("produtos").child(key);
        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
                Intent intent = new Intent(UpdateActivity.this, MainActivity.class);
                intent.putExtra("categoria", "Frios e Laticínios"); // Exemplo de valor
                startActivity(intent);

            }
        });
    }
    public void saveData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Verifica se uma nova imagem foi selecionada
        if (uri != null) {
            // Novo upload de imagem
            storageReference = FirebaseStorage.getInstance().getReference().child("produtos").child(Objects.requireNonNull(uri.getLastPathSegment()));

            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete());
                    Uri urlImage = uriTask.getResult();
                    imageUrl = urlImage.toString();
                    updateData();
                    dialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(UpdateActivity.this, "Falha no upload da imagem", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Nenhuma nova imagem foi selecionada, usa o URL da imagem antiga
            imageUrl = oldImageURL;
            updateData();
            dialog.dismiss();
        }
    }
    public void updateData(){
        title = updateTitle.getText().toString().trim();
        desc = updateDesc.getText().toString().trim();
        float quantidadeValor = Float.parseFloat(desc);
        String sufixo;
        // selectedCategory = updateCategorySpinner.getSelectedItem().toString(); // Obter a categoria selecionada

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Por favor preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (updateToggleButton.isChecked()) {
            // Quando estiver no modo "Peso"
            if (quantidadeValor > 10) {
                sufixo = " gramas"; // Define o sufixo como gramas
                desc = String.valueOf((int) quantidadeValor); // Remove casas decimais, se necessário
            } else {
                sufixo = " kg"; // Define o sufixo como kg
            }
        } else {
            // Quando estiver no modo "Quantidade"
            sufixo = " unidades";
        }
        String textoComSufixo = desc + sufixo;

        // Certifique-se de que a categoria não está vazia
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Toast.makeText(this, "Por favor selecione uma categoria.", Toast.LENGTH_SHORT).show();
            return;
        }

        DataClass dataClass = new DataClass(title, textoComSufixo, imageUrl, selectedCategory);

        databaseReference.setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageURL);
                    reference.delete();
                    Toast.makeText(UpdateActivity.this, "Atualizado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //M
    private String removerSufixo(String quantidadeComSufixo) {
        // Remover qualquer um dos sufixos possíveis
        if (quantidadeComSufixo.endsWith(" unidades")) {
            return quantidadeComSufixo.replace(" unidades", "");
        } else if (quantidadeComSufixo.endsWith(" gramas")) {
            return quantidadeComSufixo.replace(" gramas", "");
        } else if (quantidadeComSufixo.endsWith(" kg")) {
            return quantidadeComSufixo.replace(" kg", "");
        }
        return quantidadeComSufixo; // Retorna a quantidade sem o sufixo
    }
}
