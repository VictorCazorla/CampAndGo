package com.tfg.campandgo

import android.content.Intent
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.ui.theme.CampAndGoTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeGoogleSignIn()
        setContent {
            CampAndGoTheme { // Invoca el Theme
                NavigatorHub( // Invoca el @Composable administrador
                    onGoogleSignInClick = { startGoogleSignIn() }
                )
            }
        }
    }

    /**
     * Registro y login con google / correo + contraseña
     */
    private fun initializeGoogleSignIn() {
        auth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        val account = task.getResult(ApiException::class.java)
        account?.idToken?.let { firebaseAuthWithGoogle(it) }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                setContent { CampAndGoTheme { HomeActivity() } }
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

/**
 * Navegador entre pantallas
 * Administra las pantallas del inicio navegando entre ellas
 */
@Composable
fun NavigatorHub(onGoogleSignInClick: () -> Unit) {
    val navigator = rememberNavController() // Almacena en tiempo real los datos introducidos

    NavHost(navController = navigator, startDestination = "start") {
        composable("start") {
            StartScreen(
                onLoginClick = { navigator.navigate("login") },
                onGoogleSignInClick = onGoogleSignInClick,
                onRegisterClick = { navigator.navigate("register") }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginClick = { navigator.navigate("home") },
                onRegisterClick = { navigator.navigate("register") },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterClick = { navigator.navigate("login") },
                onBackToLoginClick = { navigator.popBackStack() }
            )
        }
        composable("home") { HomeActivity() } // Pantalla "home" ya definida en otro .kt
    }
}

/**
 * Estética de la pantalla de inicio
 * Funcionalidad de los clicks
 */
@Composable
fun StartScreen(
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to CampAndGo!",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Login")
        }

        Button(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Login with Google")
        }

        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register")
        }
    }
}

/**
 * Estética de la pantalla de login
 * Funcionalidad de los clicks
 */
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) } // Estado para la visibilidad de la contraseña
    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Please fill in all fields"
            } else {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onLoginClick()
                    } else {
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                errorMessage = "No account found with this email."
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                errorMessage = when ((task.exception as FirebaseAuthInvalidCredentialsException).errorCode) {

                                    "ERROR_INVALID_EMAIL" -> {
                                        "The email address is badly formatted."
                                    }

                                    "ERROR_WRONG_PASSWORD" -> {
                                        "Incorrect password. Please try again."
                                    }
                                    else -> {
                                        "Invalid credentials. Please check your email and password."
                                    }
                                }
                            }
                            else -> {
                                errorMessage = "Login failed. Please check your email and password."
                            }
                        }
                    }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onGoogleSignInClick, modifier = Modifier.fillMaxWidth()) {
            Text("Sign in with Google")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register")
        }
    }
}

/**
 * Estética de la pantalla de registro
 * Funcionalidad de los clicks
 */
@Composable
fun RegisterScreen(onRegisterClick: () -> Unit, onBackToLoginClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVerify by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) } // Estado para la visibilidad de la contraseña
    var isPasswordVerifyVisible by remember { mutableStateOf(false) } // Estado para la visibilidad de la confirmación de contraseña
    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = passwordVerify,
            onValueChange = { passwordVerify = it },
            label = { Text("Repeat password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVerifyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVerifyVisible = !isPasswordVerifyVisible }) {
                    Icon(
                        imageVector = if (isPasswordVerifyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVerifyVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(onClick = {
            if (email.isBlank() || password.isBlank() || passwordVerify.isBlank()) {
                errorMessage = "Please fill in all fields"
            } else if (password != passwordVerify) {
                errorMessage = "Passwords do not match"
            } else if (password.length > 10) {
                errorMessage = "Password must be under 10 characters"
            } else if (!password.contains(Regex("\\d+"))) {
                errorMessage = "Password must contain at least one digit."
            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onRegisterClick()
                    } else {
                        when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                errorMessage = when ((task.exception as FirebaseAuthInvalidCredentialsException).errorCode) {
                                    "ERROR_INVALID_EMAIL" -> {
                                        "The email address is badly formatted."
                                    }

                                    "ERROR_WRONG_PASSWORD" -> {
                                        "Incorrect password. Please try again."
                                    }
                                    else -> {
                                        "Invalid credentials. Please check your email and password."
                                    }
                                }
                            }
                            else -> {
                                errorMessage = "Registration failed. Please check the fields and try again."
                            }
                        }
                    }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBackToLoginClick) {
            Text("Already have an account? Login")
        }
    }
}