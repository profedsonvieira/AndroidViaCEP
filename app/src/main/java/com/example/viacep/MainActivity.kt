package com.example.viacep

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val viewModel: com.example.viacep.ui.ViaCepViewModel by viewModels()

    private lateinit var etCep: TextInputEditText  // Mudado para TextInputEditText
    private lateinit var btnBuscar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultado: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        setupCepMask()
        observeState()
    }

    private fun initViews() {
        etCep = findViewById(R.id.etCep)
        btnBuscar = findViewById(R.id.btnBuscar)
        progressBar = findViewById(R.id.progressBar)
        tvResultado = findViewById(R.id.tvResultado)
    }

    private fun setupClickListeners() {
        btnBuscar.setOnClickListener {
            val cep = etCep.text.toString().trim()
            if (isValidCep(cep)) {
                viewModel.buscarEndereco(cep)
            } else {
                tvResultado.text = "❌ CEP inválido! Digite 8 dígitos."
            }
        }
    }

    private fun setupCepMask() {
        etCep.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true

                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                when {
                    cleanString.length <= 5 -> {
                        etCep.setText(cleanString)
                        etCep.setSelection(cleanString.length)
                    }
                    cleanString.length <= 8 -> {
                        val masked = "${cleanString.substring(0, 5)}-${cleanString.substring(5)}"
                        etCep.setText(masked)
                        etCep.setSelection(masked.length)
                    }
                    else -> {
                        val masked = "${cleanString.substring(0, 5)}-${cleanString.substring(5, 8)}"
                        etCep.setText(masked)
                        etCep.setSelection(masked.length)
                    }
                }

                isUpdating = false
            }
        })
    }

    private fun observeState() {
        lifecycleScope.launchWhenStarted {
            viewModel.enderecoState.collect { state ->
                when (state) {
                    is com.example.viacep.ui.EnderecoState.Idle -> {
                        hideLoading()
                    }
                    is com.example.viacep.ui.EnderecoState.Loading -> {
                        showLoading()
                    }
                    is com.example.viacep.ui.EnderecoState.Success -> {
                        hideLoading()
                        displayEndereco(state.endereco)
                    }
                    is com.example.viacep.ui.EnderecoState.Error -> {
                        hideLoading()
                        tvResultado.text = "❌ ${state.message}"
                    }
                }
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = ProgressBar.VISIBLE
        btnBuscar.isEnabled = false
        btnBuscar.text = "Buscando..."
    }

    private fun hideLoading() {
        progressBar.visibility = ProgressBar.GONE
        btnBuscar.isEnabled = true
        btnBuscar.text = "Buscar Endereço"
    }

    private fun displayEndereco(endereco: com.example.viacep.data.model.Endereco) {
        val resultado = """
            📍 Endereço Encontrado
            
            🏷️ CEP: ${endereco.cep}
            🏠 Logradouro: ${endereco.logradouro}
            📋 Complemento: ${if (endereco.complemento.isNotEmpty()) endereco.complemento else "Nenhum"}
            🏘️ Bairro: ${endereco.bairro}
            🏙️ Cidade: ${endereco.localidade}
            🏳️ UF: ${endereco.uf}
            🔢 DDD: ${endereco.ddd}
        """.trimIndent()

        tvResultado.text = resultado
    }

    private fun isValidCep(cep: String): Boolean {
        val cleanCep = cep.replace("[-\\s]".toRegex(), "")
        return cleanCep.length == 8 && cleanCep.all { it.isDigit() }
    }
}