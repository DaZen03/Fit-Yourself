package com.example.appfitness.ui

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appfitness.MainActivity
import com.example.appfitness.data.local.AppDatabase
import com.example.appfitness.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity: AppCompatActivity() {
    private lateinit var  binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.irARegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }


        val db = AppDatabase.obtenerBaseDatos(this)
        val usuarioDao = db.usuarioDao()

        binding.enviarDatos.setOnClickListener {
            val email = binding.ingresarCorreo.text.toString().trim()
            val contrasena = binding.ingresarContrasena.text.toString().trim()

            if(email.isEmpty() || contrasena.isEmpty()){
                Toast.makeText(this, "Llenas todos los campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO){
                val usuarioEncontrado = usuarioDao.buscarCorreo(email)

                withContext(Dispatchers.Main){
                    if (usuarioEncontrado != null){
                        if (usuarioEncontrado.contrasena == contrasena){
                            Toast.makeText(this@LoginActivity,
                                "Bienvenido ${usuarioEncontrado.usuario}", Toast.LENGTH_LONG).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

        }
    }

}
