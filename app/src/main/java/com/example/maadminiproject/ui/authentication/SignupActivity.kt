package com.example.maadminiproject.ui.authentication

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.maadminiproject.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.llLoginLink.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(binding.etPassword, isPasswordVisible)
        }

        binding.ivShowConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(binding.etConfirmPassword, isConfirmPasswordVisible)
        }

        binding.btnSignup.setOnClickListener {
            // TODO: Implement signup logic
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        editText.setSelection(editText.text.length)
    }
}
