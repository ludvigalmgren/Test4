package com.example.test4

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log.d
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_assess_squirrel.*
import kotlinx.android.synthetic.main.add_product.*

val pictureList = mutableListOf<String>("")

class AssessSquirrelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assess_squirrel)

        //Initiate dialogue for thank you
        val thankYouForAssessment = AlertDialog.Builder(this)
        thankYouForAssessment.setMessage("Thank you for your assessment!")
            .setNegativeButton("Ok!", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        val ref = FirebaseDatabase.getInstance().getReference("NewPictures")

        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
        for (dSnapshot in dataSnapshot.children) {
            val pictureName = dSnapshot.key
               val ref2 = ref.child("$pictureName")
                val postListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        pictureList[0] = dataSnapshot.value.toString()
                        d("Ludvig","Picture instance: $pictureList")
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                }
                ref2.addValueEventListener(postListener)
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {}
    }
    ref.addListenerForSingleValueEvent(menuListener)

        //Set new picture
        val address1 = "NewPictures/"
        val address2 = pictureList[0]
        val address3 = address1.plus(address2)
        val imageRef = FirebaseStorage.getInstance().getReference(address3)
        imageRef.downloadUrl.addOnSuccessListener { Uri ->

            val imageURL = Uri.toString()
            val imageView = findViewById<ImageView>(R.id.imageViewAssess)

            Glide.with(this/*context*/)
                .load(imageURL)
                .into(imageView)
        }

        //Initiate Firebase database reference
        val ref2 = FirebaseDatabase.getInstance().getReference("assessments")

        //Getting the device ID
        val deviceAppUID: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        //Write to database when submit button clicked
        buttonSubmitAssess.setOnClickListener {

            //Write value to database
            ref2.child(pictureList[0]).child(deviceAppUID).child("IsSquirrel").setValue(switch1.isChecked)
            ref2.child(pictureList[0]).child(deviceAppUID).child("IsAlive").setValue(switch2.isChecked)
            ref2.child(pictureList[0]).child(deviceAppUID).child("IsFree").setValue(switch3.isChecked)
            ref2.child(pictureList[0]).child(deviceAppUID).child("IsTakenByUser").setValue(switch4.isChecked)
            ref2.child(pictureList[0]).child(deviceAppUID).child("IsDisturbing").setValue(switch5.isChecked)
            thankYouForAssessment.show()

        }

        //Go back when Back button pushed
        buttonBackAssess.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }
}