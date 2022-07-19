package com.firelord.qualitywebs

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.firelord.qualitywebs.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // init firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // handle onClickButton
        binding.btLogout.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }
    }

    private fun checkUser(){
        //check user is logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser==null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}