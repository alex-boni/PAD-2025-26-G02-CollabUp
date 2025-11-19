package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.CollabItemActivity;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItemAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

public class CalendarioFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private CollabItemAdapter adapter;

    // ID del Collab y usuario de ejemplo
    private final String EJEMPLO_USUARIO_ID = "DXjuPYl8UcW7QMkbAVTZUXRZZfz1";
    private final String EJEMPLO_COLLAB_ID = "ryO2NPfO9YaaWfNkhibD";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerItemsDia);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CollabItemAdapter(new ArrayList<>(), item -> {
            //Pasamos los parámetros necesarios
            Bundle bundle = new Bundle();
            bundle.putString("idI", item.getIdI());
            bundle.putString("idC", item.getIdC());
            //todo sacar esto de la collab
            bundle.putStringArrayList("miembros", new ArrayList<>());
            bundle.putStringArrayList("collabViews", new ArrayList<>());

            Intent intent = new Intent(getContext(), CollabItemActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        /*
        // listener para cuando el usuario cambia la fecha
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {

            // Comprobamos si la fecha seleccionada es 15/11/2025
            // NOTA: 'month' está basado en 0 (Enero=0, Noviembre=10)
            if (year == 2025 && month == 10 && dayOfMonth == 15) {

                Toast.makeText(getContext(), "Abriendo Collab de ejemplo...", Toast.LENGTH_SHORT).show();

                // Abrir el fragmento de detalle
                CollabDetailFragment detailFragment = CollabDetailFragment.newInstance(EJEMPLO_COLLAB_ID);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentApp, detailFragment)
                        .addToBackStack(null) // Para poder volver atrás
                        .commit();
            }
        });

         */

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            Timestamp fecha = new Timestamp(cal.getTime());

            // Cargo los items de ese día del usuario
            new CollabItem().obtenerCollabItemsUsrFecha(EJEMPLO_USUARIO_ID, EJEMPLO_COLLAB_ID, fecha, new OnDataLoadedCallback<List<CollabItem>>() {
                @Override
                public void onSuccess(List<CollabItem> result) {
                    adapter.setItems(result);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error al cargar items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }
}