package es.ucm.fdi.pad.collabup.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

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

public class ConfigurarNuevoCollabViewActivity extends AppCompatActivity {

    private String collabId;

    private CollabView instance;

    private final Map<String, View> settingWidgets = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_configure_new_collabview);

        Intent intent = getIntent();
        collabId = intent.getStringExtra("COLLAB_ID");

        String collabViewType = intent.getStringExtra("COLLABVIEW");
        Class<? extends CollabView> clazz = Registry.getRegistry(CollabView.class).get(collabViewType);

        try {
            instance = (CollabView) clazz.getMethod("getStaticInstance").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configurar " + clazz.getSimpleName());

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

        LinearLayout containerView = findViewById(R.id.ajustes_list);

        Set<CollabViewSetting> settings = instance.getStaticCreationSettings();
        if (settings == null) {
            // Evitar NPE (NullPointerException si una implementación devuelve null
            settings = Collections.emptySet();
        }

        if (settings.isEmpty()) {
            // Desactivar el TextView de ajustes
            TextView ajustesTv = findViewById(R.id.ajustes);
            ajustesTv.setVisibility(TextView.GONE);
        }

        for (CollabViewSetting setting : settings) {
            // Grupo vertical para este setting con margen superior/inferior
            int margin = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
            LinearLayout group = new LinearLayout(this);
            group.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams groupLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            groupLp.setMargins(0, margin, 0, margin);
            group.setLayoutParams(groupLp);
            // outline + ripple, padding y foco
            int innerPadding = (int) (6 * getResources().getDisplayMetrics().density + 0.5f);
            group.setBackgroundResource(R.drawable.outline_clickable);
            group.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);
            group.setClickable(true);
            group.setFocusable(true);
            group.setFocusableInTouchMode(true);
            switch (setting.getType()) {
                case TEXTO: {
                    // Label
                    TextView label = new TextView(this);
                    label.setText(getString(es.ucm.fdi.pad.collabup.R.string.setting_label, setting.getName(), setting.isRequired() ? "*" : ""));
                    LinearLayout.LayoutParams lpLabel = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    label.setLayoutParams(lpLabel);
                    group.addView(label);

                    // EditText para texto
                    EditText et = new EditText(this);
                    et.setInputType(InputType.TYPE_CLASS_TEXT);
                    et.setHint(setting.getName());
                    et.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    // Si hay constraint de longitud, mostrarla en el hint
                    List<String> c = setting.getConstraints();
                    if (c != null && !c.isEmpty()) {
                        // Por ejemplo, si el primer constraint es un número máximo
                        String first = c.get(0);
                        try {
                            int max = Integer.parseInt(first);
                            et.setHint(setting.getName() + " (max " + max + " chars)");
                        } catch (NumberFormatException ignored) {
                            // no es un número: no hacemos nada
                        }
                    }

                    group.addView(et);
                    // Guardar referencia directa con clave predecible
                    settingWidgets.put(generateSettingId(setting.getName()) + "_widget", et);
                    break;
                }
                case NUMERO: {
                    // Label
                    TextView label = new TextView(this);
                    label.setText(getString(es.ucm.fdi.pad.collabup.R.string.setting_label, setting.getName(), setting.isRequired() ? "*" : ""));
                    label.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    group.addView(label);

                    // EditText numérico
                    EditText etNum = new EditText(this);
                    etNum.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    etNum.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    // Si existen constraints, interpretarlas como rango [min, max]
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
                    // Guardar referencia directa con clave predecible
                    settingWidgets.put(generateSettingId(setting.getName()) + "_widget", etNum);
                    break;
                }
                case LISTA_OPCIONES: {
                    // Label
                    TextView label = new TextView(this);
                    label.setText(getString(es.ucm.fdi.pad.collabup.R.string.setting_label, setting.getName(), setting.isRequired() ? "*" : ""));
                    label.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    group.addView(label);

                    // Spinner con las opciones tomadas de constraints
                    Spinner spinner = new Spinner(this);
                    // Añadir un pequeño margen superior entre el label y el spinner
                    int spacing = (int) (4 * getResources().getDisplayMetrics().density + 0.5f);
                    // Añadir también un margen inferior mayor para separar del siguiente elemento
                    int bottomSpacing = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
                    LinearLayout.LayoutParams spinnerLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    spinnerLp.topMargin = spacing;
                    spinnerLp.bottomMargin = bottomSpacing;
                    spinner.setLayoutParams(spinnerLp);

                    List<String> options = setting.getConstraints();
                    ArrayAdapter<String> adapter;
                    if (options == null || options.isEmpty()) {
                        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"(sin opciones)"});
                    } else {
                        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
                    }
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    group.addView(spinner);
                    // Guardar referencia directa con clave predecible
                    settingWidgets.put(generateSettingId(setting.getName()) + "_widget", spinner);
                    break;
                }
                case BOOLEANO: {
                    // CheckBox para booleano
                    CheckBox cb = new CheckBox(this);
                    cb.setText(setting.getName());
                    cb.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    group.addView(cb);
                    // Guardar referencia directa con clave predecible
                    settingWidgets.put(generateSettingId(setting.getName()) + "_widget", cb);
                    break;
                }
            }
            group.setId(generateSettingId(setting.getName()));
            containerView.addView(group);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_configure_new_collabview, menu);

        MenuItem createItem = menu.findItem(R.id.action_create);
        View actionView = createItem.getActionView();

        if (actionView != null) {
            TextView tv = actionView.findViewById(R.id.menu_create_text);
            tv.setOnClickListener(v -> {
                EditText nombre = findViewById(R.id.editTextNombre);
                String collabViewName = nombre.getText().toString().trim();
                if (collabViewName.isEmpty()) {
                    nombre.setError("El nombre no puede estar vacío");
                    return;
                }
                CollabView actualInstance = instance.build(collabId, null, collabViewName, getSettingsFromUI(), new ArrayList<>());
                actualInstance.crear(new OnOperationCallback() {
                    @Override
                    public void onSuccess() {
                        // CollabView añadido con éxito
                        Toast.makeText(ConfigurarNuevoCollabViewActivity.this, "CollabView añadido con éxito", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ConfigurarNuevoCollabViewActivity.this, "Error al añadir CollabView: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });


            });
        }

        return true;
    }

    private Map<String, Object> getSettingsFromUI() {
        Set<CollabViewSetting> settings = instance.getStaticCreationSettings();
        if (settings == null) {
            settings = Collections.emptySet();
        }

        Map<String, Object> settingsMap = new HashMap<>();

        for (CollabViewSetting setting : settings) {
            String widgetKey = generateSettingId(setting.getName()) + "_widget";
            View widget = settingWidgets.get(widgetKey);

            if (widget == null) {
                // Widget no encontrado, saltar
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

    /**
     * Genera un ID predecible para un setting basado en su nombre.
     */
    private int generateSettingId(String settingName) {
        String sanitized = settingName.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
        return ("setting_" + sanitized).hashCode();
    }
}
