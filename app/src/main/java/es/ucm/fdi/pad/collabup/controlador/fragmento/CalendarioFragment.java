package es.ucm.fdi.pad.collabup.controlador.fragmento;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItemAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

public class CalendarioFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private CollabItemAdapter adapter;


    //Para tener el usuario:
    private FirebaseAuth mAuth;
    private FirebaseUser usuarioFirebase;
    private Usuario usuario;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendario, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //Layouts (orden importante)
        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerItemsDia);

        //Sacamos las cosas del usuario
        mAuth = FirebaseAuth.getInstance();
        usuarioFirebase = mAuth.getCurrentUser();
        usuario = new Usuario();
        usuario.obtener(usuarioFirebase.getUid(), new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                usuario = data;

                // Cargar items del día actual (para que al inicio salgan los items de hoy)
                Calendar hoy = Calendar.getInstance();
                cargarItemsDia(hoy);

                // Listener al cambiar de día -> se abre lista de items
                calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    cargarItemsDia(cal);
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Creamos el adapter de la lista de items
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CollabItemAdapter(new ArrayList<>(), item -> {
            //Pasamos los parámetros necesarios
            Bundle bundle = new Bundle();
            bundle.putString("idI", item.getIdI());
            bundle.putString("idC", item.getIdC());
            CollabItemFragment fragment = new CollabItemFragment();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

    }

    private void cargarItemsDia(Calendar cal) {
        Timestamp fecha = new Timestamp(cal.getTime());

        // Cargo los items de ese día del usuario de cualquier collab (de ese usuario)
        //Saco collabs del usuario:
        new Collab().obtenerCollabsDelUsuario(usuario.getUID(), new OnDataLoadedCallback<ArrayList<Collab>>() {
            @Override
            public void onSuccess(ArrayList<Collab> listaCollabs) {
                List<CollabItem> todosItems = new ArrayList<>(); //auxiliar por firebase
                AtomicInteger contador = new AtomicInteger(0); //por firebase

                // Aquí tenemos todas las collabs del usuario -> sacamos los items asignados a ese usuario en esa fecha
                for (Collab collab : listaCollabs) {
                    new CollabItem().obtenerCollabItemsUsrFecha(usuario.getUID(), collab.getId(), fecha, new OnDataLoadedCallback<List<CollabItem>>() {
                        @Override
                        public void onSuccess(List<CollabItem> result) {
                            todosItems.addAll(result);
                            if (contador.incrementAndGet() == listaCollabs.size()) {
                                adapter.setItems(todosItems); //porque sino se carga raro
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Error al cargar items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error al cargar collabs del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}