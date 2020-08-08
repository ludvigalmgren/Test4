package com.example.test4

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log.d
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

var myScores= mutableListOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

var allAverages= mutableListOf<Double>(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                                     0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

var countAllScores = 0
var countMyScores = 0

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Start add product activity when click button
        seeTodaysButton.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        //Setup calendar and get today's date
        val c = Calendar.getInstance()
        val day = c[Calendar.DAY_OF_MONTH]

        //Initiate Firebase database reference
        val ref = FirebaseDatabase.getInstance().getReference("scoring")

        //Do every minute
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val c2 = Calendar.getInstance()
                val hour = c2[Calendar.HOUR_OF_DAY]
                val minute = c2[Calendar.MINUTE]
                    //Zero countAllScores and countMyScores if midnight
                    if(hour == 0 && minute == 0){
                        countMyScores=0
                    }
                //Take value from database when a new value appears
                val menuListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Take value from database for entire month
                    for (i in 1..31) {
                        val databaseScore = dataSnapshot.child("$i").child("Sum").value
                        if (databaseScore != null) {
                            val score = databaseScore.toString()
                            val score2 = score.toDouble()
                            countAllScores =
                                dataSnapshot.child("$i").child("Count").value.toString().toInt()
                            allAverages[i - 1] = score2 / countAllScores.toDouble()
                        }
                    }
                    d("Ludvig", "allAverages: $allAverages")
            }
            override fun onCancelled(databaseError: DatabaseError) {
               // handle error
            }
        }
        ref.addListenerForSingleValueEvent(menuListener)
            }
        },0, 60000)

        //Update the picture and the text fields
        val maxAverage = allAverages.max()
        val highScoreIndex = allAverages.indexOf(maxAverage)
        imageHighScoreSquirrel.setImageResource(allPics[highScoreIndex])
        viewAverage.text = allAverages[highScoreIndex].toString().take(3)
        viewYourScore.text = myScores[highScoreIndex].toString()

        //Display icon if all scores are zero
        if(allAverages.max() == 0.0) {
           imageHighScoreSquirrel.setImageResource(R.drawable.icon)
        }

    }
}