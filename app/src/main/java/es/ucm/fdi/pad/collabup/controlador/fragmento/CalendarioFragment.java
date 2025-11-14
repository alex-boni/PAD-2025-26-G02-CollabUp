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

import java.util.Calendar;

import es.ucm.fdi.pad.collabup.R;

public class CalendarioFragment extends Fragment {

    private CalendarView calendarView;

    // ID del Collab de ejemplo
    private final String EJEMPLO_COLLAB_ID = "Bt8zGlf5fevw4Tqej0Kn";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        calendarView = view.findViewById(R.id.calendarView);

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

        return view;
    }
}