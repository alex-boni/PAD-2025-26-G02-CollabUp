package es.ucm.fdi.pad.collabup.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

//Para ver todos los collabItems de una collab específica
public class CollabItemsListActivity extends AppCompatActivity {
    private ListView lvCollabItems;
    private ArrayList<CollabItem> listaItems = new ArrayList<>();
    private ArrayAdapter<String> adapter; // Adapter simple solo con nombres de items
    private String collabId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collab_items_list);

        //(para volver atrás)
        Button btnVolver = findViewById(R.id.btnVolverCollabItem);
        btnVolver.setOnClickListener(v -> finish());

        lvCollabItems = findViewById(R.id.lvCollabItems);
        collabId = getIntent().getStringExtra("collabId");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvCollabItems.setAdapter(adapter);

        cargarCollabItems();

        lvCollabItems.setOnItemClickListener((parent, view, position, id) -> {
            CollabItem itemSeleccionado = listaItems.get(position);
            Intent intent = new Intent(CollabItemsListActivity.this, CollabItemActivity.class);
            intent.putExtra("idI", itemSeleccionado.getIdI());
            intent.putExtra("idC", collabId);
            startActivity(intent);
        });
    }

    private void cargarCollabItems() {
        CollabItem model = new CollabItem();
        model.setIdC(collabId);
        List<String> lista = new ArrayList<>();
        model.setcvAsignadas(lista);
        model.obtenerCollabItemsCollab(collabId, new OnDataLoadedCallback<ArrayList<CollabItem>>() {
            @Override
            public void onSuccess(ArrayList<CollabItem> items) {
                if (items != null && !items.isEmpty()) {
                    listaItems.clear();
                    listaItems.addAll(items);

                    ArrayList<String> nombres = new ArrayList<>();
                    for (CollabItem ci : items) nombres.add(ci.getNombre());

                    adapter.clear();
                    adapter.addAll(nombres);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CollabItemsListActivity.this, "No hay tareas para mostrar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CollabItemsListActivity.this, "Error al cargar tareas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        cargarCollabItems(); // recargamos la lista siempre que se muestre el activity
    }
}
