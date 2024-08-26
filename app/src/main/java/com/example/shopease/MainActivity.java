package com.example.shopease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    List<String> categories; // Para as categorias
    Spinner categorySpinner; // Para as categorias

    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    SearchView searchView;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recylerView);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();

        adapter = new MyAdapter(MainActivity.this, dataList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("produtos");
        dialog.show();


        // Inicialize o Spinner
        categorySpinner = findViewById(R.id.category_spinner);
        categories = new ArrayList<>();
        categories.add("Todas");  // Adiciona a opção "Todas" para exibir todos os produtos

        // Popula o Spinner com as categorias
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                filterByCategory(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Não fazer nada
            }
        });

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                Set<String> uniqueCategories = new HashSet<>(); // Usado para coletar categorias únicas

                for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                    DataClass dataClass = itemSnapshot.getValue(DataClass.class);
                    dataClass.setKey(itemSnapshot.getKey());
                    dataList.add(dataClass);
                    Log.d("MainActivity", "Produto adicionado: " + dataClass.getNomeProduto());

                    if (dataClass.getCategoria() != null && !dataClass.getCategoria().isEmpty()) {
                        uniqueCategories.add(dataClass.getCategoria());
                    }
                }
                // Atualiza as categorias no Spinner
                categories.clear(); // Limpa a lista antes de adicionar novos itens
                categories.add("Todas"); // Adiciona a opção "Todas"
                categories.addAll(uniqueCategories);

                // Notifica o adapter do Spinner sobre as mudanças na lista de categorias
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, categories);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(spinnerAdapter);

                adapter.notifyDataSetChanged();

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });
    }
    public void searchList(String text){
        ArrayList<DataClass> searchList = new ArrayList<>();
        categorySpinner.setSelection(categories.indexOf("Todas"));
        for (DataClass dataClass: dataList){
            if (dataClass.getNomeProduto().toLowerCase().contains(text.toLowerCase())){
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }

    @Override
    public void onBackPressed() {
        // Inicia a TelaInicial
        super.onBackPressed();
        Intent intent = new Intent(MainActivity.this, TelaInicial.class);
        startActivity(intent);
        Log.d("MainActivity", "Voltar Para A Tela De Inicio");
        // Finaliza a MainActivity atual
        finish();
    }
    public void filterByCategory(String category) {
        List<DataClass> filteredList = new ArrayList<>();
        if (category.equals("Todas")) {
            filteredList = dataList;
        } else {
            for (DataClass dataClass : dataList) {
                // Verifica se a categoria não é null antes de chamar equals
                if (dataClass.getCategoria() != null && dataClass.getCategoria().equals(category)) {
                    filteredList.add(dataClass);
                }
            }
        }
        adapter.searchDataList((ArrayList<DataClass>) filteredList);
    }

}