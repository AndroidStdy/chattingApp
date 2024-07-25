package fastcampus.part2.chattingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fastcampus.part2.chattingapp.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}