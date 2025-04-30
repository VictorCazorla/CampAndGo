package com.tfg.campandgo

import com.tfg.campandgo.ui.screen.HomeScreen
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.ui.navigation.Routes
import com.tfg.campandgo.ui.screen.AddCamperSiteScreen
import com.tfg.campandgo.ui.screen.CamperSiteScreen
import com.tfg.campandgo.ui.screen.LoginScreen
import com.tfg.campandgo.ui.screen.RegisterScreen
import com.tfg.campandgo.ui.screen.StartScreen
import com.tfg.campandgo.ui.theme.CampAndGoTheme

/**
 * Actividad principal de la aplicación CampAndGo.
 * Gestiona la navegación entre pantallas, la autenticación de Google y la inicialización de servicios.
 */
class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth // Instancia de Firebase Authentication
    private lateinit var googleSignInClient: GoogleSignInClient // Cliente de inicio de sesión de Google

    private var isUserLoggedIn by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeGoogleSignIn()

        FirebaseApp.initializeApp(this)

        // Inicializa el servicio Places si no está ya inicializado
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "YOUR_API_KEY")
        }

        // Establece el contenido de la actividad utilizando Compose
        setContent {
            CampAndGoTheme {
                NavigatorHub(
                    isUserLoggedIn = isUserLoggedIn,
                    onGoogleSignInClick = { startGoogleSignIn() }
                )
            }
        }
    }

    /**
     * Configura el inicio de sesión de Google con las opciones predeterminadas.
     */
    private fun initializeGoogleSignIn() {
        auth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Solicita el ID token
            .requestEmail() // Solicita el correo electrónico del usuario
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * Inicia el proceso de inicio de sesión de Google.
     */
    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    /**
     * Maneja el resultado del inicio de sesión de Google y realiza la autenticación con Firebase.
     *
     * @param task Resultado del intento de inicio de sesión de Google.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        val account = task.getResult(ApiException::class.java)
        account?.idToken?.let { firebaseAuthWithGoogle(it) }
    }

    /**
     * Autentica al usuario con Firebase utilizando el token de Google.
     *
     * @param idToken Token de Google proporcionado tras el inicio de sesión.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                isUserLoggedIn = true
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001 // Código de solicitud para el inicio de sesión de Google
    }
}

/**
 * Composable que gestiona la navegación entre las pantallas principales de la aplicación.
 *
 * @param onGoogleSignInClick Callback que se ejecuta al seleccionar la opción de inicio de sesión con Google.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigatorHub(
    isUserLoggedIn: Boolean,
    onGoogleSignInClick: () -> Unit
) {
    val navigator = rememberNavController()

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            navigator.navigate(Routes.HOME) {
                popUpTo(0) // Limpia el back stack completo
            }
        }
    }

    NavHost(navController = navigator, startDestination = Routes.START) {
        composable(Routes.START) {
            StartScreen(
                onLoginClick = { navigator.navigate(Routes.LOGIN) },
                onGoogleSignInClick = onGoogleSignInClick,
                onRegisterClick = { navigator.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = { navigator.navigate(Routes.HOME) },
                onRegisterClick = { navigator.navigate(Routes.REGISTER) },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterClick = { navigator.navigate(Routes.LOGIN) },
                onBackToLoginClick = { navigator.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.HOME) { HomeScreen(navigator = navigator) }
        composable(
            route = Routes.CAMPER_SITE,
            arguments = listOf(
                navArgument("camperSiteID") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val camperSiteID = backStackEntry.arguments?.getString("camperSiteID") ?: ""

            CamperSiteScreen(
                camperSiteID = camperSiteID,
                onBackClick = { /* Lógica para volver atrás */ },
                onBookClick = { /* Lógica para reservar */ })
        }
        composable(
            route = Routes.ADD_CAMPER_SITE,
            arguments = listOf(
                navArgument("latitude") { type = NavType.StringType },
                navArgument("longitude") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0

            AddCamperSiteScreen(latitude = latitude, longitude = longitude, navigator = navigator)
        }
    }
}

/**
 * Composable para un botón personalizado.
 *
 * @param text Texto que se mostrará en el botón.
 * @param onClick Acción que se ejecutará al presionar el botón.
 * @param modifier Modificador opcional para personalizar la apariencia del botón.
 * @param backgroundColor Color de fondo del botón.
 * @param contentColor Color del texto y contenido del botón.
 * @param icon Composable opcional que representa un ícono a la izquierda del texto.
 */
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 16.sp)
        }
    }
}
