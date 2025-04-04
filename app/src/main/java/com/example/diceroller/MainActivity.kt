/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.diceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diceroller.ui.theme.DiceRollerTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance() //Conexão com o Firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this) //Inicializando o Firebase

        setContent {
            DiceRollerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImcApp(db) //Passando o Firestore para o componente ImcApp
                }
            }
        }
    }
}


@Composable
fun ImcApp(db: FirebaseFirestore) {
    IMCCalculator(
        db = db, //Passando o Firestore para o componente IMCCalculator
        modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun IMCCalculator(db: FirebaseFirestore, modifier: Modifier = Modifier) {
    var weight by remember {mutableStateOf("")}
    var height by remember {mutableStateOf("")}
    var imcCategory by remember {mutableStateOf<String?>(null)}

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        TextField(
            value = weight,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it == '.' }) {
                    weight = newValue
                }
            },
            label = { Text("Peso (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = height,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it == '.' }) {
                    height = newValue
                }
            },
            label = { Text("Altura (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val imc = weight.toFloatOrNull()?.let { w ->
                height.toFloatOrNull()?.let { h ->
                    if (h != 0f) w / (h * h) else null
                }
            }
            imcCategory = imc?.let {
                when {
                    it < 18.5 -> "Magreza"
                    it < 25 -> "Normal"
                    it < 30 -> "Sobrepeso"
                    else -> "Obesidade"
            }
            } ?: "Dados inválidos"

            if(imc != null && imcCategory != "Dados inválidos"){
                val data = hashMapOf(
                    "peso" to weight,
                    "altura" to height,
                    "imc" to imc,
                    "categoria" to imcCategory
                )

                db.collection("imcResultados") //Nome da coleção
                    .add(data) //Salvar no documento "imcResultados"
                    .addOnSuccessListener {
                        println("IMC salvo com sucesso!")
                    }
                    .addOnFailureListener { e ->
                        println("Erro ao salvar IMC: $e")
                    }
            }
        }){
            Text("Calcular IMC")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imcCategory?.let { category ->
            Text("Categoria: $category",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

