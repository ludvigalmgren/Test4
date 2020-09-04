package com.example.test4

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log.d
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.util.HttpUtils.parse
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_squirrel.*
import java.net.URI

val uriList = mutableListOf<Uri?>(null)

class AddSquirrelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_squirrel)

        progressBar.setVisibility(View.INVISIBLE)

        //Initiate dialogue for successfully upload
        val uploadSuccessful = AlertDialog.Builder(this)
        uploadSuccessful.setMessage("Picture uploaded successfully!")
            .setNegativeButton("Ok!", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        //Initiate dialogue for error upload
        val uploadError = AlertDialog.Builder(this)
        uploadError.setMessage("Something went wrong!")
            .setNegativeButton("Ok!", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        //Take picture from gallery to imageViewer
        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageViewAddSquirrel.setImageURI(uri)
            uriList[0]=uri
        }
        getContent.launch("image/*")

        //Function that writes file to database
        fun uploadImageToFirebaseStorage(imagecap: Uri) {

            if (imagecap == null) {
                d("Ludvig","imagecap is null")
            }

            val filename = "NewPictures/" + editTextTextPersonName.text.toString() + "_" + editTextLocation.text.toString()
            val ref = FirebaseStorage.getInstance().getReference(filename)


            ref.putFile(imagecap!!)
                .addOnSuccessListener {
                    uploadSuccessful.show()
                    progressBar.setVisibility(View.INVISIBLE)
                }
                .addOnFailureListener {
                    uploadError.show()
                    progressBar.setVisibility(View.INVISIBLE)
                }
        }

        //Write to Firebase when Submit button pushed
        buttonSubmitSquirrel.setOnClickListener {
            progressBar.setVisibility(View.VISIBLE)
                val picUri = Uri.parse(uriList[0].toString())
                uploadImageToFirebaseStorage(picUri)
                val ref2 = FirebaseDatabase.getInstance().getReference("NewPictures")
                val pictureName = editTextTextPersonName.text.toString() + "_" + editTextLocation.text.toString()
                ref2.child(pictureName).setValue(pictureName)
        }

        //Go back when Back button pushed
        buttonBackAddSquirrel.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
