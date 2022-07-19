package com.firelord.qualitywebs

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.firelord.qualitywebs.databinding.ActivityLoginBinding
import com.firelord.qualitywebs.util.Constants.Companion.RC_SIGN_IN
import com.firelord.qualitywebs.util.Constants.Companion.TAG
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    // FireBase Auth
    private lateinit var firebaseAuth : FirebaseAuth
    private var email = ""
    private var password = ""

    // Google Auth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)

        // config progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Logging in..")
        progressDialog.setCanceledOnTouchOutside(false)

        // call google
        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOption)

        // init firebaseauth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // Google Sign in button
        binding.btGoogle.setOnClickListener {
            Log.d(TAG,"onCreate: begin google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        // handle click, open sign up page
        binding.tvNoAccount.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
            finish()
        }

        // handle click, begin login
        binding.btLogin.setOnClickListener{
            //before logging in, validate data
            validateData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Request returned from launching the intent
        if (requestCode == RC_SIGN_IN){
            Log.d(TAG,"onActivityResult: Google sign in intent")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign in success
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }
            catch (e:Exception){
                // failed google signin
                Log.d(TAG,"onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG,"firebaseAuthWithGoogleAccount: begin firebase auth with google")
        val credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                //login success
                Log.d(TAG,"firebaseAuthWithGoogleAccount: LoggedIn")

                // Get logged in user
                val firebaseUser = firebaseAuth.currentUser

                // get user id
                val uid = firebaseUser!!.uid
                val email = firebaseUser!!.email

                Log.d(TAG,"firebaseAuthWithGoogleAccount: uid: ${uid}")
                Log.d(TAG,"firebaseAuthWithGoogleAccount: email: ${email}")

                //check if user is new or existing
                if (authResult.additionalUserInfo!!.isNewUser){
                    //user is new - account created
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Account created ...\n$email")
                    Toast.makeText(this,"Account created $email", Toast.LENGTH_SHORT).show()
                } else {
                    // existing user - logged in
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Existing user \n$email")
                    Toast.makeText(this, "Logged in as $email", Toast.LENGTH_SHORT).show()
                }
                // open MainActivity
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.d(TAG,"firebaseAuthWithGoogleAccount: Login failed due to ${e.message}")
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateData() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPass.text.toString().trim()

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email format
            binding.etEmail.error = "Invalid email format"
        }
        else if (TextUtils.isEmpty(password)){
            // no password entered
            binding.etPass.error = "please enter password"
        }
        else {
            // data is validated, begin login
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        // show progress
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                //login success
                progressDialog.dismiss()

                //get user info
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(this, "Logged in as $email", Toast.LENGTH_SHORT).show()

                // open mainActivity
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // login failed
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun checkUser(){
        val  firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!=null){
            // user is already logged in
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}