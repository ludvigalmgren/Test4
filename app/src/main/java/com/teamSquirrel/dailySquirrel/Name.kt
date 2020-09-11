package com.teamSquirrel.dailySquirrel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.teamSquirrel.dailySquirrel.R
import kotlinx.android.synthetic.main.activity_name.*

class Name : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name)



        //Go back when Back button pushed
        buttonSubmitName.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }



    }
}