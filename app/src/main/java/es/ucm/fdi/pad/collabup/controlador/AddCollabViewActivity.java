package es.ucm.fdi.pad.collabup.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import java.lang.reflect.InvocationTargetException;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.Registry;

public class AddCollabViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_add_collabview);

        Intent intent = getIntent();
        String collabId = intent.getStringExtra("COLLAB_ID");
        String collabName = intent.getStringExtra("COLLAB_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Añadir a " + collabName);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Traer toolbar al frente y asegurar que recibe toques
        toolbar.bringToFront();
        toolbar.setClickable(true);
        toolbar.setFocusable(true);

        // Asegurar que la pulsación en la flecha hace finish()
        toolbar.setNavigationOnClickListener(v -> finish());

        LinearLayout containerView = findViewById(R.id.collabview_list);
        View fragmentContainer = findViewById(R.id.fragment_container);

        float density = getResources().getDisplayMetrics().density;
        int thumbMargin = (int) (6 * density);
        int defaultItemHeight = (int) (72 * density);

        Registry<CollabView> registry = Registry.getRegistry(CollabView.class);
        for (Class<? extends CollabView> cvClass : registry.getAll()) {
            try {
                CollabView cvInstance = (CollabView) cvClass.getMethod("getStaticInstance").invoke(null);

                View mini = cvInstance.getStaticAddCollabViewInListEntry(this);
                if (mini == null) continue; // evitar añadir vistas nulas

                // Respetar la altura que el propio mini establezca; si no tiene, usar defaultItemHeight
                ViewGroup.LayoutParams existingLp = mini.getLayoutParams();
                int heightToUse = defaultItemHeight;
                if (existingLp != null) {
                    heightToUse = existingLp.height;
                }

                // Asegurar LayoutParams adecuados para que no se salgan del contenedor
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        heightToUse
                );
                lp.setMargins(thumbMargin, thumbMargin, thumbMargin, thumbMargin);
                mini.setLayoutParams(lp);

                mini.setOnClickListener(v -> {
                    fragmentContainer.setVisibility(View.VISIBLE);
                    findViewById(R.id.collabview_container).setVisibility(View.GONE);

                    toolbar.setTitle("Configurar " + cvClass.getSimpleName());
                    toolbar.setNavigationOnClickListener(nav -> getSupportFragmentManager().popBackStack());

                    ConfigurarNuevoCollabViewFragment fragment = ConfigurarNuevoCollabViewFragment.newInstance(collabId, cvClass.getSimpleName());
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });

                containerView.addView(mini);

            } catch (IllegalAccessException |
                     NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException("Error al instanciar CollabView: " + cvClass.getSimpleName(), e);
            }
        }

        // Cuando se haga pop del fragment, mostrar de nuevo la lista y restaurar toolbar
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                fragmentContainer.setVisibility(View.GONE);
                findViewById(R.id.collabview_container).setVisibility(View.VISIBLE);

                toolbar.setTitle("Añadir a " + collabName);
                toolbar.setNavigationOnClickListener(v -> finish());
            }
        });
    }
}
