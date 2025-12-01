package es.ucm.fdi.pad.collabup.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.Map;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Variables UI
    private Button btnIrLogin;
    private Button btnIrSingup;
    private Button btnGoogleMain; // El nuevo botón

    // Firebase y Google
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // Launcher para capturar el resultado de Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // El usuario seleccionó una cuenta correctamente
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        // Ahora nos autenticamos en Firebase con esa cuenta
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // Error común: SHA-1 incorrecto o falta de internet
                        Toast.makeText(this, "Fallo Google: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
                        Log.w("GoogleLogin", "Google sign in failed code=" + e.getStatusCode(), e);
                    }
                } else {
                    // El usuario canceló el diálogo o ocurrió un error silencioso
                    Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        btnIrLogin = findViewById(R.id.btnIrLogin);
        btnIrSingup = findViewById(R.id.btnIrSingup);
        btnGoogleMain = findViewById(R.id.btnGoogleMain);

        // Listeners
        btnIrLogin.setOnClickListener(this);
        btnIrSingup.setOnClickListener(this);
        btnGoogleMain.setOnClickListener(this); // Conectar el botón

        // Inicializo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configuramos Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Si ya hay usuario, entrar directo
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            goToApp();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnIrSingup) {
            startActivity(new Intent(this, SingupController.class));
        } else if (id == R.id.btnIrLogin) {
            startActivity(new Intent(this, LoginController.class));
        } else if (id == R.id.btnGoogleMain) {
            // Lógica del botón Google
            signInGoogle();
        }
    }

    // --- Lógica Google ---

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login exitoso en Firebase
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Comprobamos si hay que crear su perfil en Firestore
                            checkAndCreateUserInFirestore(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Error de autenticación con Firebase.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkAndCreateUserInFirestore(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return;

        Usuario usuarioDAO = new Usuario();
        usuarioDAO.obtener(firebaseUser.getUid(), new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                // El usuario ya existe en Firestore -> Entrar
                goToApp();
            }

            @Override
            public void onFailure(Exception e) {
                // El usuario no existe en Firestore -> Crearlo
                crearUsuarioDesdeGoogle(firebaseUser);
            }
        });
    }

    private void crearUsuarioDesdeGoogle(FirebaseUser fUser) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUID(fUser.getUid());
        nuevoUsuario.setEmail(fUser.getEmail());
        nuevoUsuario.setNombre(fUser.getDisplayName());

        // Se crea un usuario base desde el email
        String baseUser = "user";
        if (fUser.getEmail() != null) {
            baseUser = fUser.getEmail().split("@")[0];
        }
        nuevoUsuario.setUsuario(baseUser);

        // Foto de Google
        if (fUser.getPhotoUrl() != null) {
            nuevoUsuario.setUrlFoto(fUser.getPhotoUrl().toString());
        }

        nuevoUsuario.crear(new OnOperationCallback() {
            @Override
            public void onSuccess() {
                // Si hay foto, actualizamos para guardarla (ya que 'crear' solo guarda básicos)
                if (nuevoUsuario.getUrlFoto() != null) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("urlFoto", nuevoUsuario.getUrlFoto());
                    nuevoUsuario.actualizarCampos(updates, new OnOperationCallback() {
                        @Override public void onSuccess() { goToApp(); }
                        @Override public void onFailure(Exception e) { goToApp(); }
                    });
                } else {
                    goToApp();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Error al crear perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToApp() {
        startActivity(new Intent(this, AppController.class));
        finish();
    }
}