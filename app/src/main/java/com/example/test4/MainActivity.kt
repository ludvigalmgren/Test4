package com.example.test4

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log.d
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.util.Timer
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

var myScores= mutableListOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

var allScores= mutableListOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

var count = 1
var countScores = 0

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

        //Zero count and countscore
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val hour = c[Calendar.HOUR_OF_DAY]
                val minute = c[Calendar.MINUTE]
                //At 00.00 reset count and countScores
                d("Ludvig","hour: $hour")
                d("Ludvig","minute: $minute")
                if(hour == 0 && minute == 0){
                    count=1
                    countScores=0
                }
            }
        },0, 60000)



        //Take value from database when a new value appears
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var databaseScore = dataSnapshot.value
                val score = databaseScore.toString()
                d("Ludvig","score: $score")
                if(score.isNullOrEmpty()) {
                }else{
                    val score2 = score.toInt()
                    d("Ludvig","databasevalue: $score2")
                    allScores[day - 1] += score2
                    val averageScore = allScores[day-1].toDouble()/count.toDouble()
                    count += 1
                    d("Ludvig","count: $count")
                    d("Ludvig","sumt: $allScores")
                    viewAverage.text = averageScore.toString()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
     //           // handle error
            }
        }

        d("Ludvig","day: $day")
        //Pick out the highest score
        var highScore = myScores.max()
        viewHighScore.text = highScore.toString()
        var averageScore = allScores.max()

        //Display the image of the leader
        val highScoreIndex = allScores.indexOf(averageScore)
        if (highScore != null && highScore.toInt() == 0){
            imageHighScoreSquirrel.setImageResource(R.drawable.icon)
        }else {
            imageHighScoreSquirrel.setImageResource(allPics[highScoreIndex])
        }

    }
}