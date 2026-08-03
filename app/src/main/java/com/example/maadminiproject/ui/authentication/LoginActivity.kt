package com.example.maadminiproject.ui.authentication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.maadminiproject.MainActivity
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSignUpLink()

        binding.btnLogin.setOnClickListener {
            // Placeholder login logic
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupSignUpLink() {
        val fullText = "New here? Create an account"
        val spannableString = SpannableString(fullText)
        
        val startIndex = fullText.indexOf("Create an account")
        val endIndex = startIndex + "Create an account".length

        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                    startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.secondary_blue)
                }
            }
            
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.secondary_blue)), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            binding.tvSignUp.text = spannableString
            binding.tvSignUp.movementMethod = LinkMovementMethod.getInstance()
            binding.tvSignUp.highlightColor = Color.TRANSPARENT
        }
    }
}
