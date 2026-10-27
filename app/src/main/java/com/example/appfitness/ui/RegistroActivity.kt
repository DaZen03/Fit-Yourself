package com.example.appfitness.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appfitness.data.local.AppDatabase
import com.example.appfitness.data.local.UsuarioEntity
import com.example.appfitness.databinding.ActivityRegistroBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.obtenerBaseDatos(this)
        val usuarioDao = db.usuarioDao()

        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.ingresarNombre.text.toString().trim()
            val email = binding.ingresarCorreo.text.toString().trim()
            val contrasena = binding.ingresarContrasena.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            lifecycleScope.launch(Dispatchers.IO) {
                val usuarioExistente = usuarioDao.buscarCorreo(email)

                withContext(Dispatchers.Main) {
                    if (usuarioExistente != null) {
                        Toast.makeText(this@RegistroActivity, "El correo ya está registrado", Toast.LENGTH_SHORT).show()
                    } else {

                        lifecycleScope.launch(Dispatchers.IO) {
                            usuarioDao.insertarUsuario(
                                UsuarioEntity(
                                    usuario = nombre,
                                    email = email,
                                    contrasena = contrasena
                                )
                            )

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@RegistroActivity, "¡Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }


        binding.tvVolverLogin.setOnClickListener {
            finish()
        }
    }
}