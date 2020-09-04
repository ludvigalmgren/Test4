package com.example.test4
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.add_product.*
import java.util.*

class AddProductActivity: AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_product)

        //Initiate dialogue for max one vote
        val alertOnlyOneVote = AlertDialog.Builder(this)
        alertOnlyOneVote.setMessage("You can only vote once per day")
            .setNegativeButton("Ok!", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        //Initiate dialogue for max 10 and min 1
        val alertMaxTen = AlertDialog.Builder(this)
        alertMaxTen.setMessage("Score must be 1-10")
            .setNegativeButton("Ok!", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        //Get the date
        val c = Calendar.getInstance()
        val day = c[Calendar.DAY_OF_MONTH]
        val month = c[Calendar.MONTH]
        val months = mutableListOf<String>("Jan", "Feb","March","April","May","June","July","Aug","Sept","Oct","Nov","Dec")
        val dayAndMonth = months[month].plus(" ").plus(day.toString())

        //Getting the device ID
        val deviceAppUID: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        //Initiate Firebase database reference
        val ref = FirebaseDatabase.getInstance().getReference("scoring")

        //Display the date in the top
        displayDailyDate.text =dayAndMonth

        //Set today's picture
        val address1 = "SquirrelPictures/s"
        val dayIndex = day - 1
        val address2 = address1.plus("$dayIndex")
        val address3 = address2.plus(".jpg")
        val imageRef = FirebaseStorage.getInstance().getReference(address3)
        imageRef.downloadUrl.addOnSuccessListener { Uri ->

            val imageURL = Uri.toString()
            val imageView = findViewById<ImageView>(R.id.imageViewSquirrel)

            Glide.with(this/*context*/)
                .load(imageURL)
                .into(imageView)
        }

        //Display your score
        textViewYourScoreAddProduct.text = myScores[day-1].toString()

        //Display average score
        textViewAverageAddProduct.text = allAverages[day-1].toString().take(3)

        //Store the value submitted when submit button pushed
        addRatingSubmitButton.setOnClickListener {
            val hej = cuteness.text.toString()
            if(hej.isNotEmpty()) {
                val hej2 = hej.toInt()
                if (myScores[day-1] > 0) {
                    alertOnlyOneVote.show()
                } else if (hej2 > 10 || hej2 < 1 ) {
                    alertMaxTen.show()
                } else {
                    //Get day
                    val day = c[Calendar.DAY_OF_MONTH]
                    //Write value to database
                    ref.child(day.toString()).child(deviceAppUID).child("Score").setValue(hej2)
                    //Start Main Activity
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