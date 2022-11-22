package com.labmacc.tictactoev2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
val TAG = "Qtable"
val TAG2 = "Training"
val TAG3 = "Show"
val TAG4 ="Add"

val FREE = 0 //Position free
val USER = 1 //User ID
val AGENT = 2 //Agent ID
val FAIR = 3



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ChessView(this))
    }
}