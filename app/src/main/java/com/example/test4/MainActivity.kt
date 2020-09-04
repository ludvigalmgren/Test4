package com.example.test4

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

var allAverages= mutableListOf<Double>(
    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
)

var myScores = mutableListOf<Int>(
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
)

var dailySum = mutableListOf<Int>(
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
)

var dailyCount = mutableListOf<Int>(
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
)

var mAuth: FirebaseAuth? = null
var mUser: FirebaseUser? = null

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initiate authentication of user if not already done
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth!!.currentUser
        if(mUser == null){
            startActivity(Intent(this, login::class.java))
        }


        //Update the picture and the text fields
        val maxAverage = allAverages.max()
        val highScoreIndex = allAverages.indexOf(maxAverage)
        viewAverage.text = allAverages[highScoreIndex].toString().take(3)
        viewYourScore.text = myScores[highScoreIndex].toString()

        //Show icon if no favourite
        if (maxAverage == 0.0){
            imageHighScoreSquirrel.setImageResource(R.drawable.icon)
            //Else get favourite from database
        }else {
            val address1 = "SquirrelPictures/s"
            val address2 = address1.plus("$highScoreIndex")
            val address3 = address2.plus(".jpg")
            val imageRef = FirebaseStorage.getInstance().getReference(address3)
            imageRef.downloadUrl.addOnSuccessListener { Uri ->

                val imageURL = Uri.toString()
                val imageView = findViewById<ImageView>(R.id.imageHighScoreSquirrel)

                Glide.with(this/*context*/)
                    .load(imageURL)
                    .into(imageView)
            }
        }

        //Getting the device ID
        val deviceAppUID: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        //Start add product activity when click button
        seeTodaysButton.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        //Start add squirrel activity when click button
        submitSquirrelButton.setOnClickListener {
            startActivity(Intent(this, AddSquirrelActivity::class.java))
        }

        //Start assess squirrel activity when click button
        assessSquirrelButton.setOnClickListener {
            startActivity(Intent(this, AssessSquirrelActivity::class.java))
        }

        //Setup calendar and get today's date
        val c = Calendar.getInstance()
        val day = c[Calendar.DAY_OF_MONTH]

        //Take values from database for entire month
        for (i in 1..31) {
            //Initiate Firebase database reference
            val ref = FirebaseDatabase.getInstance().getReference("scoring").child("$i")
            //Take value from database when a new value appears
            val menuListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (dSnapshot in dataSnapshot.children) {
                        val username = dSnapshot.key
                        //If day i is empty
                        if(username == null){
                            myScores[i - 1] = 0
                            dailyCount[i - 1] = 0
                            dailySum[i - 1] = 0
                            //If there are scores for day i
                        }else{
                            val ref2 = ref.child("$username").child("Score")
                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    // Get score instance and write to lists
                                    val scoreInstance = dataSnapshot.value.toString().toInt()
                                    dailySum[i - 1] += scoreInstance
                                    dailyCount[i - 1] += 1
                                    //If score instance is from this user
                                    if(username == deviceAppUID){
                                        myScores[i - 1] = scoreInstance
                                                }
                                    }
                                            override fun onCancelled(databaseError: DatabaseError) {
                                            }
                                    }
                                        ref2.addValueEventListener(postListener)
                            }
                    }
                }
                                override fun onCancelled(databaseError: DatabaseError) {}
            }
                    ref.addListenerForSingleValueEvent(menuListener)
                    val avg = dailySum[i - 1].toDouble() / dailyCount[i - 1].toDouble()
                        if (avg > 0 && avg < 11 ) {
                            allAverages[i - 1] = avg
                        }
        }
    }
}