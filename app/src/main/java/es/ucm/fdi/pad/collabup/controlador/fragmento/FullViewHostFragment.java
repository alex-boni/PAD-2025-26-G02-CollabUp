package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;

import java.lang.reflect.Method;

import es.ucm.fdi.pad.collabup.R;

public class FullViewHostFragment extends Fragment {
    private static final String ARG_TITLE = "ARG_TITLE";
    private Fragment contentFragment;
    private int containerId = View.generateViewId();

    public static FullViewHostFragment newInstance(String title) {
        FullViewHostFragment f = new FullViewHostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        f.setArguments(args);
        return f;
    }

    public void setContent(Fragment content) {
        this.contentFragment = content;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext();

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        AppBarLayout appBar = new AppBarLayout(ctx);
        appBar.setLayoutParams(new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Toolbar toolbar = new Toolbar(ctx);
        toolbar.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (56 * ctx.getResources().getDisplayMetrics().density)));
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : null;
        toolbar.setTitle(title != null ? title : "");

        // Resolver color de fondo y color de texto desde atributos del tema para mantener estilo
        TypedValue tv = new TypedValue();
        int bgColor = 0;
        // Usar color de fondo y color de texto primarios del tema Android como fallback
        if (ctx.getTheme().resolveAttribute(android.R.attr.colorBackground, tv, true)) {
            bgColor = tv.data;
        } else if (ctx.getTheme().resolveAttribute(android.R.attr.colorPrimary, tv, true)) {
            bgColor = tv.data;
        }
        toolbar.setBackgroundColor(bgColor);

        if (ctx.getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
            // preferir color del tema, pero forzamos a negro si no está definido
        }
        // Forzar título visible: usar negro (soluciona que el título no se vea)
        int black = ContextCompat.getColor(ctx, android.R.color.black);
        toolbar.setTitleTextColor(black);

        // Elevation similar al layout anterior (se usaba app:elevation="0dp")
        toolbar.setElevation(0f);

        // Navigation icon: intentar usar el drawable del proyecto primero (ic_arrow_back_tinted)
        boolean navSet = false;
        try {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_tinted);
            navSet = true;
        } catch (Exception ignored) {
        }

        if (!navSet) {
            try {
                toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
            } catch (Exception ignored) {}
        }

        // Poner description accesible (coincidir con otros layouts)
        try { toolbar.setNavigationContentDescription(R.string.back_description); } catch (Exception ignored) {}

        // Asegurar que la flecha es negra (tint al drawable)
        try {
            Drawable nav = toolbar.getNavigationIcon();
            if (nav != null) {
                Drawable wrapped = DrawableCompat.wrap(nav);
                DrawableCompat.setTint(wrapped, black);
                toolbar.setNavigationIcon(wrapped);
            }
        } catch (Exception ignored) {}

        appBar.addView(toolbar);

        FrameLayout contentContainer = new FrameLayout(ctx);
        contentContainer.setId(containerId);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentContainer.setLayoutParams(contentLp);

        root.addView(appBar);
        root.addView(contentContainer);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar navegación en la toolbar
        Toolbar toolbar = null;
        if (view instanceof ViewGroup) {
            toolbar = findToolbarInViewGroup((ViewGroup) view);
        }
        if (toolbar != null) {
            // Asegurar que el título provisto en los argumentos se aplica también aquí (recreación)
            String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : null;
            if (title != null && !title.isEmpty()) {
                toolbar.setTitle(title);
            }

            // Asegurar que el icono de navegación existe y está tintado de negro
            try {
                if (toolbar.getNavigationIcon() == null) {
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_tinted);
                }
                Drawable nav = toolbar.getNavigationIcon();
                if (nav != null) {
                    Drawable wrapped = DrawableCompat.wrap(nav);
                    DrawableCompat.setTint(wrapped, ContextCompat.getColor(requireContext(), android.R.color.black));
                    toolbar.setNavigationIcon(wrapped);
                }
                toolbar.setNavigationContentDescription(R.string.back_description);
            } catch (Exception ignored) {}

            toolbar.setNavigationOnClickListener(v -> {
                try {
                    if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                        getParentFragmentManager().popBackStack();
                        return;
                    }
                    // Si no hay backstack, cerrar la Activity (patrón usado en Activities del proyecto)
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } catch (Exception ignored) {
                    // Fallback: delegar al dispatcher de back
                    if (getActivity() != null) {
                        getActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            });
        }

        // Insertar el fragment de vista grande como hijo solo si aún no existe
        if (contentFragment != null) {
            try {
                if (getChildFragmentManager().findFragmentById(containerId) == null) {
                    getChildFragmentManager().beginTransaction().replace(containerId, contentFragment).commitAllowingStateLoss();
                }
            } catch (Exception ignored) {}
        }
    }

    private Toolbar findToolbarInViewGroup(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View c = vg.getChildAt(i);
            if (c instanceof Toolbar) return (Toolbar) c;
            if (c instanceof ViewGroup) {
                Toolbar t = findToolbarInViewGroup((ViewGroup) c);
                if (t != null) return t;
            }
        }
        return null;
    }

    // Intento de refrescar el contenido si este implementa refreshView()
    public void refreshView() {
        try {
            if (contentFragment != null) {
                Method m = contentFragment.getClass().getMethod("refreshView");
                m.invoke(contentFragment);
            }
        } catch (Exception ignored) {}
    }
}
