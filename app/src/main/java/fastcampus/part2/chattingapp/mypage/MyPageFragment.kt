package fastcampus.part2.chattingapp.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import fastcampus.part2.chattingapp.LoginActivity
import fastcampus.part2.chattingapp.R
import fastcampus.part2.chattingapp.databinding.FragmentMypageBinding

class MyPageFragment: Fragment(R.layout.fragment_mypage) {
    private lateinit var binding : FragmentMypageBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)

        binding.btnApply.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val description = binding.etDescription.text.toString()

            if(username.isEmpty() || description.isEmpty()){
                Toast.makeText(context,"유저이름은 빈 값으로 두실 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //todo 파이어베이스 raltime database update

        }
        binding.btnSignOut.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(context,LoginActivity::class.java))
            activity?.finish()

        }

    }
}