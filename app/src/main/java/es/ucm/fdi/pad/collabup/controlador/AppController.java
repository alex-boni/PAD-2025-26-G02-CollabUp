package es.ucm.fdi.pad.collabup.controlador;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.fragmento.AjustesFragment;
import es.ucm.fdi.pad.collabup.controlador.fragmento.CalendarioFragment;
import es.ucm.fdi.pad.collabup.controlador.fragmento.CollabListFragment;
import es.ucm.fdi.pad.collabup.controlador.fragmento.HomeFragment;
import es.ucm.fdi.pad.collabup.modelo.collabView.Calendario;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.Lista;
import es.ucm.fdi.pad.collabup.modelo.collabView.Registry;
import es.ucm.fdi.pad.collabup.modelo.collabView.TablonNotas;


public class AppController extends AppCompatActivity {

    static {
        // Registrar los tipos de CollabView disponibles
        Registry<CollabView> reg = Registry.getOrCreateRegistry(CollabView.class);
        reg.register(Lista.class);
        reg.register(Calendario.class);
        reg.register(TablonNotas.class);
    }

    // Variables que controlarán a los fragmentos
    private final HomeFragment homeFragment = new HomeFragment();
    private final CollabListFragment collabListFragment = new CollabListFragment();
    private final CalendarioFragment amigosFragment = new CalendarioFragment();
    private final AjustesFragment ajustesFragment = new AjustesFragment();

    private BottomNavigationView bottomNavigationViewApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_layout);

        bottomNavigationViewApp = findViewById(R.id.bottomNavigationViewApp);

        // Carga el fragmento inicial (Home)
        replaceFragment(homeFragment);

        // Listener para la barra de navegación inferior
        bottomNavigationViewApp.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                replaceFragment(homeFragment);
            } else if (itemId == R.id.nav_colabs) {
                replaceFragment(collabListFragment);
                // Toast.makeText(this, "Collabs (próximamente)", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_calendar) {
                replaceFragment(amigosFragment);
                // Toast.makeText(this, "Amigos (próximamente)", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_ajustes) {
                replaceFragment(ajustesFragment);
                // Toast.makeText(this, "Ajustes (próximamente)", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    /**
     * Reemplaza el fragmento actual en el contenedor 'fragmentApp'
     */
    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // El ID 'fragmentApp' ahora existe en app_layout.xml
            transaction.replace(R.id.fragmentApp, fragment);
            transaction.commit();
        }
    }
}