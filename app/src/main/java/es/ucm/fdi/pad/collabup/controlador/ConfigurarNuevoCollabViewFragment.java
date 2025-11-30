package es.ucm.fdi.pad.collabup.controlador;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabViewSetting;
import es.ucm.fdi.pad.collabup.modelo.collabView.Registry;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class ConfigurarNuevoCollabViewFragment extends Fragment {

    private static final String ARG_COLLAB_ID = "COLLAB_ID";
    private static final String ARG_COLLABVIEW = "COLLABVIEW";

    private String collabId;
    private CollabView instance;
    private final Map<String, View> settingWidgets = new HashMap<>();

    public static ConfigurarNuevoCollabViewFragment newInstance(@Nullable String collabId, @NonNull String collabViewType) {
        ConfigurarNuevoCollabViewFragment f = new ConfigurarNuevoCollabViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COLLAB_ID, collabId);
        args.putString(ARG_COLLABVIEW, collabViewType);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_new_collabview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String collabViewType = null;
        if (args != null) {
            collabId = args.getString(ARG_COLLAB_ID);
            collabViewType = args.getString(ARG_COLLABVIEW);
            Class<? extends CollabView> clazz = Registry.getRegistry(CollabView.class).get(collabViewType);
            try {
                instance = (CollabView) clazz.getMethod("getTemplateInstance").invoke(null);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        AppCompatActivity act = (AppCompatActivity) requireActivity();
        ActionBar actionBar = act.getSupportActionBar();
        if (actionBar != null) {
            if (instance != null) {
                actionBar.setTitle("Configurar " + instance.getClass().getSimpleName());
            } else if (collabViewType != null) {
                actionBar.setTitle("Configurar " + collabViewType);
            }
        }

        LinearLayout containerView = view.findViewById(R.id.ajustes_list);

        Set<CollabViewSetting> settings = instance != null ? instance.getStaticCreationSettings() : null;
        if (settings == null) {
            settings = Collections.emptySet();
        }

        if (settings.isEmpty()) {
            TextView ajustesTv = view.findViewById(R.id.ajustes);
            ajustesTv.setVisibility(TextView.GONE);
        }

        for (CollabViewSetting setting : settings) {
            int margin = (int) (8 * requireContext().getResources().getDisplayMetrics().density + 0.5f);
            LinearLayout group = new LinearLayout(requireContext());
            group.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams groupLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            groupLp.setMargins(0, margin, 0, margin);
            group.setLayoutParams(groupLp);
            int innerPadding = (int) (6 * requireContext().getResources().getDisplayMetrics().density + 0.5f);
            group.setBackgroundResource(R.drawable.outline_clickable);
            group.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);
            group.setClickable(true);
            group.setFocusable(true);
            group.setFocusableInTouchMode(true);
            switch (setting.getType()) {
                case TEXTO: {
                    TextView label = new TextView(requireContext());
                    label.setText(getString(R.string.setting_label, setting.getName(), setting.isRequired() ? "*" : ""));
                    LinearLayout.LayoutParams lpLabel = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    label.setLayoutParams(lpLabel);
                    group.addView(label);

                    EditText et = new EditText(requireContext());
                    et.setInputType(InputType.TYPE_CLASS_TEXT);
                    et.setHint(setting.getName());
                    et.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    List<String> c = setting.getConstraints();
                    if (c != null && !c.isEmpty()) {
                        String first = c.get(0);
                        try {
                            int max = Integer.parseInt(first);
                            et.setHint(setting.getName() + " (max " + max + " chars)");
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    group.addView(et);
                    settingWidgets.put(generateSettingId(setting.getName()) + "_widget", et);
                    break;
                }
                case NUMERO: {
                    TextView label = new TextView(requireContext());
                    label.setText(getString(R.string.setting_label, setting.getName(), setting.isRequired() ? "*" : ""));
                    label.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    group.addView(label);

                    EditText etNum = new EditText(requireContext());
                    etNum.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    etNum.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    List<String> c = setting.getConstraints();
                    if (c != null && !c.isEmpty()) {
                        String hint = setting.getName();
                        if (c.size() >= 2) {
                            hint += " (" + c.get(0) + " - " + c.get(1) + ")";
                        } else if (c.size() == 1) {
                            hint += " (min " + c.get(0) + ")";
                        }
                        etNum.setHint(hint);
                    } else {
                        etNum.setHint(setting.getName());
                    }

                    group.addView(etNum);
                    settingWidgets.put(generateSettingId(setting.getName()) + "_widget", etNum);
                    break;
                }
                case LISTA_OPCIONES: {
                    TextView label = new TextView(requireContext());
                    label.setText(getString(R.string.setting_label, setting.getName(), setting.isRequired() ? "*" : ""));
                    label.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    group.addView(label);

                    Spinner spinner = new Spinner(requireContext());
                    int spacing = (int) (4 * requireContext().getResources().getDisplayMetrics().density + 0.5f);
                    int bottomSpacing = (int) (8 * requireContext().getResources().getDisplayMetrics().density + 0.5f);
                    LinearLayout.LayoutParams spinnerLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    spinnerLp.topMargin = spacing;
                    spinnerLp.bottomMargin = bottomSpacing;
                    spinner.setLayoutParams(spinnerLp);

                    List<String> options = setting.getConstraints();
                    ArrayAdapter<String> adapter;
                    if (options == null || options.isEmpty()) {
                        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"(sin opciones)"});
                    } else {
                        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
                    }
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    group.addView(spinner);
                    settingWidgets.put(generateSettingId(setting.getName()) + "_widget", spinner);
                    break;
                }
                case BOOLEANO: {
                    CheckBox cb = new CheckBox(requireContext());
                    cb.setText(setting.getName());
                    cb.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    group.addView(cb);
                    settingWidgets.put(generateSettingId(setting.getName()) + "_widget", cb);
                    break;
                }
            }
            group.setId(generateSettingId(setting.getName()));
            containerView.addView(group);
        }

        View btn = view.findViewById(R.id.btn_create_collabview);
        if (btn != null) {
            btn.setOnClickListener(v -> onCreateActionClicked());
        }
    }

    private void onCreateActionClicked() {
        View root = getView();
        if (root == null || instance == null) return;
        EditText nombre = root.findViewById(R.id.editTextNombre);
        String collabViewName = nombre.getText().toString().trim();
        if (collabViewName.isEmpty()) {
            nombre.setError("El nombre no puede estar vacío");
            return;
        }
        CollabView actualInstance = instance.build(collabId, null, collabViewName, getSettingsFromUI(), new ArrayList<>());
        actualInstance.crear(new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "CollabView añadido con éxito", Toast.LENGTH_SHORT).show();
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al añadir CollabView: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Map<String, Object> getSettingsFromUI() {
        Set<CollabViewSetting> settings = instance != null ? instance.getStaticCreationSettings() : null;
        if (settings == null) {
            settings = Collections.emptySet();
        }

        Map<String, Object> settingsMap = new HashMap<>();

        for (CollabViewSetting setting : settings) {
            String widgetKey = generateSettingId(setting.getName()) + "_widget";
            View widget = settingWidgets.get(widgetKey);

            if (widget == null) {
                continue;
            }

            switch (setting.getType()) {
                case TEXTO: {
                    EditText et = (EditText) widget;
                    String text = et.getText().toString().trim();
                    if (setting.isRequired() && text.isEmpty()) {
                        et.setError("Este campo es obligatorio");
                        throw new IllegalStateException("Faltan campos obligatorios");
                    }
                    settingsMap.put(setting.getName(), text);
                    break;
                }
                case NUMERO: {
                    EditText etNum = (EditText) widget;
                    String numStr = etNum.getText().toString().trim();
                    if (setting.isRequired() && numStr.isEmpty()) {
                        etNum.setError("Este campo es obligatorio");
                        throw new IllegalStateException("Faltan campos obligatorios");
                    }
                    Integer number = null;
                    if (!numStr.isEmpty()) {
                        try {
                            number = Integer.parseInt(numStr);
                        } catch (NumberFormatException e) {
                            etNum.setError("Número inválido");
                            throw new IllegalStateException("Número inválido");
                        }
                    }
                    settingsMap.put(setting.getName(), number);
                    break;
                }
                case LISTA_OPCIONES: {
                    Spinner spinner = (Spinner) widget;
                    String selectedOption = (String) spinner.getSelectedItem();
                    settingsMap.put(setting.getName(), selectedOption);
                    break;
                }
                case BOOLEANO: {
                    CheckBox cb = (CheckBox) widget;
                    boolean checked = cb.isChecked();
                    settingsMap.put(setting.getName(), checked);
                    break;
                }
            }
        }

        return settingsMap;
    }

    private int generateSettingId(String settingName) {
        String sanitized = settingName.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
        return ("setting_" + sanitized).hashCode();
    }
}
