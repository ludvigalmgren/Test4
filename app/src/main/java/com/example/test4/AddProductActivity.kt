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

        //Initiate database reference
        val ref = FirebaseDatabase.getInstance().getReference("scoring")

        //Initiate dialogue
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("You can only vote once per day")
            .setNegativeButton("Ok!", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
                startActivity(Intent(this, MainActivity::class.java))
            })

        //Get the date and the time
        val c = Calendar.getInstance()
        val day = c[Calendar.DAY_OF_MONTH]
        val month = c[Calendar.MONTH]+1
        val dayAndMonth = day.toString().plus("/").plus(month.toString())
        val hour = c[Calendar.HOUR_OF_DAY]
        val minute = c[Calendar.MINUTE]
        val hourMinute = hour.toString().plus(":").plus(minute.toString())

        //Display the date in the top
        displayDailyDate.text =dayAndMonth


        //Set today's picture
        imageViewSquirrel.setImageResource(allPics[day-1])

        //Store the value submitted as a shared preference
        addRatingSubmitButton.setOnClickListener {
            if (countScores >= 1 ){
                alertDialogBuilder.show()
            }else {
                    countScores += 1
                    ref.child(hourMinute).setValue(cuteness.text.toString())
                    val hej = cuteness.text.toString()
                    myScores[day - 1] = hej.toInt()
                Log.d("Ludvig", "countScores: $countScores")
                startActivity(Intent(this, MainActivity::class.java))
            }
        }



    }



}