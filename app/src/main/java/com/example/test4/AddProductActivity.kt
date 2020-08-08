package com.example.test4
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_product.*
import java.util.*

var allPics= mutableListOf(R.drawable.s01, R.drawable.s02,R.drawable.s03,R.drawable.s04,R.drawable.s05,R.drawable.s06
    ,R.drawable.s07,R.drawable.s08,R.drawable.s09,R.drawable.s10,R.drawable.s11,R.drawable.s12,R.drawable.s13
    ,R.drawable.s14,R.drawable.s15,R.drawable.s16,R.drawable.s17,R.drawable.s18,R.drawable.s19,R.drawable.s20
    ,R.drawable.s21,R.drawable.s22,R.drawable.s23,R.drawable.s24,R.drawable.s25,R.drawable.s26,R.drawable.s27
    ,R.drawable.s28,R.drawable.s29,R.drawable.s30,R.drawable.s31)

class AddProductActivity: AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_product)

        //Initiate dialogue for only one vote
        val alertOnlyOneVote = AlertDialog.Builder(this)
        alertOnlyOneVote.setMessage("You can only vote once per day")
            .setNegativeButton("Ok!", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        //Initiate dialogue for max 10
        val alertMaxTen = AlertDialog.Builder(this)
        alertMaxTen.setMessage("Score must be 1-10")
            .setNegativeButton("Ok!", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        //Get the date
        val c = Calendar.getInstance()
        val day = c[Calendar.DAY_OF_MONTH]
        val month = c[Calendar.MONTH]+1
        val dayAndMonth = day.toString().plus("/").plus(month.toString())

        //Initiate Firebase database reference
        val ref = FirebaseDatabase.getInstance().getReference("scoring")

        //Display the date in the top
        displayDailyDate.text =dayAndMonth

        //Set today's picture
        imageViewSquirrel.setImageResource(allPics[day-1])

        //Display your score
        textViewYourScoreAddProduct.text = myScores[day-1].toString()

        //Display average score
        textViewAverageAddProduct.text = allAverages[day-1].toString().take(3)

        //Store the value submitted when submit button pushed
        addRatingSubmitButton.setOnClickListener {
            val hej = cuteness.text.toString()
            if(hej.isNotEmpty()) {
                val hej2 = hej.toInt()
                if (countMyScores >= 1) {
                    alertOnlyOneVote.show()
                } else if (hej2 > 10 || hej2 < 1 ) {
                    alertMaxTen.show()
                } else {
                    //Get time
                    val day = c[Calendar.DAY_OF_MONTH]
                    //Write value to database
                    val updateScore = allAverages[day-1] * countAllScores + hej2
                    ref.child(day.toString()).child("Sum").setValue(updateScore)
                    countAllScores += 1
                    countMyScores += 1
                    //Write count to database
                    ref.child(day.toString()).child("Count").setValue(countAllScores)
                    //Write value to myScores
                    myScores[day - 1] = hej.toInt()
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        }
        //Go back when Back button pushed
        buttonBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

}