package com.example.shopease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

public class DetailActivity extends AppCompatActivity {

    TextView detailDesc, detailTitle, detailUser;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;
    String key = "";
    String imageUrl = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        detailDesc = findViewById(R.id.detailDesc);
        detailUser = findViewById(R.id.detailUser);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            detailDesc.setText(bundle.getString("QuantidadeProduto"));
            detailTitle.setText(bundle.getString("NomeProduto"));
            key = bundle.getString("key");
            imageUrl = bundle.getString("Image");
            detailUser.setText(bundle.getString("NomeUsuario"));
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("produtos");
                FirebaseStorage storage = FirebaseStorage.getInstance();


                StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
                // Tentativa de excluir a imagem

                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Imagem deletada com sucesso, então deletamos o item do database
                        reference.child(key).removeValue();
                        Toast.makeText(DetailActivity.this, "Deletado", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // A imagem não foi encontrada, mas ainda assim deletamos o item do database
                        if (e instanceof com.google.firebase.storage.StorageException &&
                                ((com.google.firebase.storage.StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            reference.child(key).removeValue();
                            Toast.makeText(DetailActivity.this, "Imagem não encontrada, mas o produto foi deletado", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(DetailActivity.this, "Erro ao deletar o produto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, UpdateActivity.class)
                        .putExtra("nomeProduto", detailTitle.getText().toString())
                        .putExtra("quantidadeProduto", detailDesc.getText().toString())
                        .putExtra("imagemProduto", imageUrl)
                        .putExtra("key", key)
                        .putExtra("nomeUsuario", detailUser.getText().toString());
                startActivity(intent);


            }
        });
    }
}