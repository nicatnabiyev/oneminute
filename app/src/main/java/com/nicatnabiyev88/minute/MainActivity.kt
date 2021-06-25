package com.nicatnabiyev88.minute

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("Quiz")

    var mainTheme: MediaPlayer? = null

    var smallerScreenSize = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = resources.configuration
        if(config.smallestScreenWidthDp <= 360){
            setContentView(R.layout.activity_main_small)
            smallerScreenSize = true
        } else {
            setContentView(R.layout.activity_main)
        }

        Log.i("myMessage", "width: "+config.smallestScreenWidthDp)
        Log.i("myMessage", "heigh: "+config.screenHeightDp)

        window.navigationBarColor = resources.getColor(R.color.background)

        mainTheme = MediaPlayer.create(applicationContext, R.raw.main_theme)
        mainTheme!!.setVolume(0.5f, 0.5f) //set volume takes two paramater

        back_button_alert_dialog.isEnabled = false
        back_button_alert_dialog.text = resources.getString(R.string.loading)

        val quiz1 = QuizModel("Yerin peyki hansıdır?", "günəş", "ay", "ulduz", "ay")
        val quiz2 = QuizModel(
            "İtalyanın paytaxtı hansı şəhərdir?",
            "Roma",
            "Paris",
            "Vyana",
            "Roma"
        )

        QuizModel.quizList.add(quiz1)
        QuizModel.quizList.add(quiz2)

        /** add question to db
        val newQuiz = QuizModel("\"Sular üzərində cənnət\" adlanan yer haradadır?",
        "Venesiyada",
        "İspaniya",
        "Zellandiya",
        "Venesiyada")
        myRef.push().setValue(newQuiz)
        */

        //read from DB
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val question = ds.child("question").value.toString()
                    val answerA = ds.child("answerA").value.toString()
                    val answerB = ds.child("answerB").value.toString()
                    val answerC = ds.child("answerC").value.toString()
                    val correctAnswer = ds.child("correctAnswer").value.toString()

                    /** find question key (from database)
                    if(question.contains("hanı", ignoreCase = true)){
                    Log.i(
                    "myMessage",
                    ds.key.toString()
                    ) //test line
                    }
                    */

                    val quiz = QuizModel(question, answerA, answerB, answerC, correctAnswer)
                    QuizModel.quizList.add(quiz)
                }
                Log.i(
                    "myMessage",
                    QuizModel.quizList[QuizModel.quizList.size - 1].question + ": " + QuizModel.quizList.size
                ) //test line
                back_button_alert_dialog.text = resources.getString(R.string.start)
                back_button_alert_dialog.isEnabled = true
                mainTheme!!.start()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })


        back_button_alert_dialog.setOnClickListener(View.OnClickListener {
            startActivity(Intent(applicationContext, StartActivity::class.java))
            overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
            finish()
        })

        main_activity_text_view_info.setOnClickListener(View.OnClickListener {
            val dialogBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            val inflater = this.layoutInflater
            var view = inflater.inflate(R.layout.game_about, null)
            if (smallerScreenSize){
                view = inflater.inflate(R.layout.game_about_small, null)
            }
            dialogBuilder.setView(view)

            val dialog = dialogBuilder.create()
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()

            val btnBack = view.findViewById<Button>(R.id.back_button_alert_dialog)
            btnBack.setOnClickListener(View.OnClickListener {
                dialog.dismiss()
            })
        })
    }


    override fun onResume() {
        super.onResume()
        mainTheme!!.start()
    }

    override fun onPause() {
        super.onPause()
        mainTheme!!.pause()
    }

    var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true
        val message = resources.getString(R.string.preess_again_to_exit)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

}