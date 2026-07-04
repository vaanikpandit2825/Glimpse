package com.example.glimpse.auth

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(){

    var email by remember{mutableStateOf("")}
    var password by remember{mutableStateOf("")}
    var passwordVisible by remember{mutableStateOf(false)}


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Spacer(modifier=Modifier.height(80.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier=Modifier.height(8.dp))

            Text(
                text = "Sign in to continue to Glimpse",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier=Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = {Text("Email")},
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                modifier=Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true
            )

    }
}