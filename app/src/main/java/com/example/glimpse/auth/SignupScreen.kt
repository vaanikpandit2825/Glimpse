package com.example.glimpse.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SignupScreen(){
    var email by remember{mutableStateOf("")}
    var password by remember{mutableStateOf("")}
    val viewModel: AuthViewModel=viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
             verticalArrangement = Arrangement.Center
    )
    {
        Text(
            text="Create account",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Email")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick =
                {
                    viewModel.signUp(
                        email = email,
                        password = password,
                        onSuccess = {
                            println("SignUp Successfull")
                        },
                        onFailure = {
                            println(it)
                        }
                    )
            },
            modifier = Modifier.fillMaxWidth()
        )
        {
            Text("Create Account")
        }
            }

    }
