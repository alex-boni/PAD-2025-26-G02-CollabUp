package es.ucm.fdi.pad.collabup.controlador;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import es.ucm.fdi.pad.collabup.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Imports de los nuevos fragmentos
import es.ucm.fdi.pad.collabup.controlador.fragmento.CalendarioFragment;
import es.ucm.fdi.pad.collabup.controlador.fragmento.CollabsFragment;
import es.ucm.fdi.pad.collabup.controlador.fragmento.AjustesFragment;
import es.ucm.fdi.pad.collabup.controlador.fragmento.HomeFragment;


public class AppController extends AppCompatActivity {

    // Variables que controlarán a los fragmentos
    private final HomeFragment homeFragment = new HomeFragment();
    private final CollabsFragment collabsFragment = new CollabsFragment();
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
                replaceFragment(collabsFragment);
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