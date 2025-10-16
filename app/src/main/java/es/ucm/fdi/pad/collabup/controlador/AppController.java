package es.ucm.fdi.pad.collabup.controlador;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import es.ucm.fdi.pad.collabup.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
// Imports de los fragments que se usarán. Se comentan porque no tenemos las clases.
// import com.example.ourleagues.fragment.app.AjustesFragment;
// import com.example.ourleagues.fragment.app.AmigosFragment;
// import com.example.ourleagues.fragment.app.InicioFragment;
// import com.example.ourleagues.fragment.app.PerfilFragment;
// import com.example.ourleagues.fragment.app.TorneosFragment;

public class AppController extends AppCompatActivity {

    /*
     * Variables que controlarán a los fragmentos.
     * Se comentan porque las clases de los Fragmentos (InicioFragment, AmigosFragment, etc.)
     * no han sido proporcionadas. Para que esto funcione, cada una de estas clases
     * debe existir y heredar de androidx.fragment.app.Fragment.
     */
    // private final InicioFragment inicioFragment = new InicioFragment();
    // private final AmigosFragment amigosFragment = new AmigosFragment();
    // private final TorneosFragment torneosFragment = new TorneosFragment();
    // private final PerfilFragment perfilFragment = new PerfilFragment();
    // private final AjustesFragment ajustesFragment = new AjustesFragment();

    private BottomNavigationView bottomNavigationViewApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_layout);

        bottomNavigationViewApp = findViewById(R.id.bottomNavigationViewApp);

        // Carga el fragmento inicial. Comentado porque inicioFragment no está definido.
        // replaceFragment(inicioFragment);

        // Listener para la barra de navegación inferior.
       /* bottomNavigationViewApp.setOnItemSelectedListener(item -> {
            // El switch reemplaza al 'when' de Kotlin.
            // Se comentan las llamadas a replaceFragment porque las instancias de los fragments no están disponibles.
            int itemId = item.getItemId();
            if (itemId == R.id.inicio) {
                // replaceFragment(inicioFragment);
            } else if (itemId == R.id.amigos) {
                // replaceFragment(amigosFragment);
            } else if (itemId == R.id.torneos) {
                // replaceFragment(torneosFragment);
            } else if (itemId == R.id.perfil) {
                // replaceFragment(perfilFragment);
            } else if (itemId == R.id.ajustes) {
                // replaceFragment(ajustesFragment);
            }
            return true;
        });*/
    }

    /*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }*/

    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentApp, fragment);
            transaction.commit();
        }
    }
}