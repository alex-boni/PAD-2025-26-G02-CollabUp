package es.ucm.fdi.pad.collabup.modelo.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import es.ucm.fdi.pad.collabup.controlador.fragmento.CollabItemViewListFragment;
import es.ucm.fdi.pad.collabup.controlador.fragmento.CollabItemsListFragment;
import es.ucm.fdi.pad.collabup.controlador.fragmento.CollabViewsListFragment;

public class TabCollabItemViewAdapter extends FragmentStateAdapter {
    private final String collabId;

    public TabCollabItemViewAdapter(@NonNull Fragment fragment, String collabId) {
        super(fragment);
        this.collabId = collabId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        //despacho a los fragments segun la posicion
        switch (position) {
            case 0:
                     return CollabViewsListFragment.newInstance(collabId); //Tenemos que crear este fragmento
            case 1:
                return CollabItemsListFragment.newInstance(collabId);
            default:
                return new TabCollabItemViewAdapter.PlaceholderFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Contador de pestañas de mi tabLayout
    }

    // --- Fragmento  para posiciones no definidas ---
    public static class PlaceholderFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Retorna una vista vacía o un mensaje indicando que no hay contenido
            return new View(container.getContext());
        }
    }
}
