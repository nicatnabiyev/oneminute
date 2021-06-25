package com.nicatnabiyev88.minute

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {

    private val handler = Handler()

    private var backgroundTheme: MediaPlayer? = null
    private var lose: MediaPlayer? = null
    var win: MediaPlayer? = null
    var record: MediaPlayer? = null

    private var rightAnswerCount = 0
    private var wrongAnswerCount = 0
    var correctAnswer = ""

    var oldIndexes = ArrayList<Int>()

    var recordAnswerCount = 0

    var gameBegin = false

    var smallerScreenSize = false

    private val timerGame = object: CountDownTimer(60000, 1){
        @SuppressLint("SetTextI18n")
        override fun onTick(v: Long) {
            start_activity_text_view_timer.text = "${v/1000+1}"
            start_activity_progressbar.progress = v.toInt()
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onFinish() {
            gameBegin = false
            stopMusic()
            start_activity_text_view_timer.text = "0"
            disableConstraintLayouts()
            if(rightAnswerCount>recordAnswerCount){
                record!!.start()
                val resultString = "Əla! Siz 1 dəqiqə ərzində $rightAnswerCount suala düzgün cavab verərək yeni rekord qırdınız!"
                dialogGameOver(
                    resultString,
                    resources.getDrawable(R.drawable.img_record),
                    rightAnswerCount
                )
            } else {
                if(rightAnswerCount>0){
                    win!!.start()
                    val resultString = "Təbriklər! Siz 1 dəqiqə ərzində $rightAnswerCount suala düzgün cavab verə bildiniz."
                    dialogGameOver(
                        resultString,
                        resources.getDrawable(R.drawable.img_win),
                        rightAnswerCount
                    )
                } else {
                    lose!!.start()
                    val resultString = "Təəssüf ki, siz 1 dəqiqə ərzində heç bir suala düzgün cavab verə bilmədiniz."
                    dialogGameOver(
                        resultString,
                        resources.getDrawable(R.drawable.img_lose),
                        recordAnswerCount
                    )
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun dialogGameOver(resultText: String, resultImage: Drawable, resultAnswer: Int) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        val inflater = this.layoutInflater

        var view = inflater.inflate(R.layout.end_game, null)

        if (smallerScreenSize){
            view = inflater.inflate(R.layout.end_game_small, null)
        }
        dialogBuilder.setView(view)

        val dialog = dialogBuilder.create()
        if(dialog.window != null){
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        dialog.setCancelable(false)
        val str = view.findViewById<TextView>(R.id.text_view_result_game)
        val img = view.findViewById<ImageView>(R.id.image_view_dialog)
        val record = view.findViewById<TextView>(R.id.record_text_view_dialog)

        str.text = resultText
        img.setImageDrawable(resultImage)

        if(resultAnswer>recordAnswerCount){
            recordAnswerCount = resultAnswer
            val sharedPreferences = this.getSharedPreferences("main_settings", MODE_PRIVATE)
            sharedPreferences.edit().putInt("userRecord", recordAnswerCount).apply()
        }

        record.text = "Rekord : $recordAnswerCount"

        dialog.show()

        val btnPlayAgain = view.findViewById<Button>(R.id.play_again_button_alert_dialog)
        btnPlayAgain.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            playAgain()
        })
    }

    private fun playAgain() {
        timerGame.start()
        gameBegin = true
        start_activity_text_view_right.text = "0"
        start_activity_text_view_wrong.text = "0"
        rightAnswerCount = 0
        wrongAnswerCount = 0
        resetBackgroundLayouts()
        goNextQuestion()

        stopLoseSound()
        stopWinSound()
        stopRecordSound()

        backgroundTheme!!.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = resources.configuration
        if(config.smallestScreenWidthDp <= 360){
            setContentView(R.layout.activity_start_small)
            smallerScreenSize = true;

        } else {
            setContentView(R.layout.activity_start)
        }

        backgroundTheme = MediaPlayer.create(applicationContext, R.raw.background_theme)
        backgroundTheme!!.setVolume(0.35f, 0.35f) //set volume takes two paramater
        backgroundTheme!!.start()

        lose = MediaPlayer.create(applicationContext, R.raw.lose)
        lose!!.setVolume(0.43f, 0.43f) //set volume takes two paramater

        win = MediaPlayer.create(applicationContext, R.raw.win)
        win!!.setVolume(0.4f, 0.4f) //set volume takes two paramater

        record = MediaPlayer.create(applicationContext, R.raw.record)
        record!!.setVolume(0.35f, 0.35f) //set volume takes two paramater

        val sharedPreferences = getSharedPreferences("main_settings", MODE_PRIVATE)
        recordAnswerCount = sharedPreferences.getInt("userRecord", 0)

        goNextQuestion()

        start_activity_text_view_right.text = rightAnswerCount.toString()
        start_activity_text_view_wrong.text = wrongAnswerCount.toString()

        timerGame.start()
        gameBegin = true

        //if clicked a variant
        constraintLayout_answer_a.setOnClickListener(View.OnClickListener {
            val answer = start_activity_text_view_answer_a.text.toString()
            controlAnswer(constraintLayout_answer_a, answer, correctAnswer)
        })
        //if clicked b variant
        constraintLayout_answer_b.setOnClickListener(View.OnClickListener {
            val answer = start_activity_text_view_answer_b.text.toString()
            controlAnswer(constraintLayout_answer_b, answer, correctAnswer)
        })
        //if clicked c variant
        constraintLayout_answer_c.setOnClickListener(View.OnClickListener {
            val answer = start_activity_text_view_answer_c.text.toString()
            controlAnswer(constraintLayout_answer_c, answer, correctAnswer)
        })

    }

    private fun goNextQuestion() {
        resetBackgroundLayouts()
        enableConstraintLayouts()

        var newIndex = (0 until QuizModel.quizList.size).random()
        while(newIndex in oldIndexes){
            newIndex = (0 until QuizModel.quizList.size).random()
        }
        oldIndexes.add(newIndex)

        Log.i(
            "myMessage",
            "Question index: " + newIndex
        ) //test line

        setUpQuiz(QuizModel.quizList[newIndex])

    }

    private fun setUpQuiz(quiz: QuizModel) {
        start_activity_text_view_question.text = quiz.question
        start_activity_text_view_answer_a.text = quiz.answerA
        start_activity_text_view_answer_b.text = quiz.answerB
        start_activity_text_view_answer_c.text = quiz.answerC
        correctAnswer = quiz.correctAnswer
    }

    private fun resetBackgroundLayouts() {
        constraintLayout_answer_a.background = ContextCompat.getDrawable(
            applicationContext,
            R.drawable.text_view_answers_bg
        )
        constraintLayout_answer_b.background = ContextCompat.getDrawable(
            applicationContext,
            R.drawable.text_view_answers_bg
        )
        constraintLayout_answer_c.background = ContextCompat.getDrawable(
            applicationContext,
            R.drawable.text_view_answers_bg
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun controlAnswer(layout: ConstraintLayout, answer: String, rightAnswerStr: String) {
        val success = MediaPlayer.create(applicationContext, R.raw.success)
        val fail = MediaPlayer.create(applicationContext, R.raw.fail)
        disableConstraintLayouts()
        if (answer.equals(rightAnswerStr)){
            success.start()
            layout.background = ContextCompat.getDrawable(
                applicationContext,
                R.drawable.right_answers_bg
            )
            rightAnswerCount++;
            start_activity_text_view_right.text = rightAnswerCount.toString()
            handler.postDelayed({
                goNextQuestion()
            }, 500)
        } else {
            layout.background = ContextCompat.getDrawable(
                applicationContext,
                R.drawable.wrong_answers_bg
            )
            wrongAnswerCount++;
            if (wrongAnswerCount!=3){
                fail.start()
                start_activity_text_view_wrong.text = wrongAnswerCount.toString()
                handler.postDelayed({
                    goNextQuestion()
                }, 500)
            } else {
                stopMusic()
                gameBegin = false
                lose!!.start()
                start_activity_text_view_wrong.text = wrongAnswerCount.toString()
                timerGame.cancel()
                val resultString = "Təəssüf ki, siz $wrongAnswerCount suala səhv cavab verdiniz və oyun bitdi.Üzülməyin, bir daha sınayın"
                dialogGameOver(
                    resultString,
                    resources.getDrawable(R.drawable.img_lose),
                    recordAnswerCount
                )
            }
        }
    }

    private fun disableConstraintLayouts(){
        constraintLayout_answer_a.isEnabled = false
        constraintLayout_answer_b.isEnabled = false
        constraintLayout_answer_c.isEnabled = false
    }

    private fun enableConstraintLayouts(){
        constraintLayout_answer_a.isEnabled = true
        constraintLayout_answer_b.isEnabled = true
        constraintLayout_answer_c.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        if (gameBegin){
            backgroundTheme!!.start()
        }
    }

    override fun onPause() {
        super.onPause()
        stopMusic()
        stopLoseSound()
        stopWinSound()
        stopRecordSound()
    }

    private fun stopMusic(){
        if (backgroundTheme!!.isPlaying) {
            backgroundTheme!!.pause()
            backgroundTheme!!.seekTo(0)
        }
    }

    private fun stopLoseSound(){
        if (lose!!.isPlaying) {
            lose!!.pause()
            lose!!.seekTo(0)
        }
    }

    private fun stopWinSound(){
        if (win!!.isPlaying) {
            win!!.pause()
            win!!.seekTo(0)
        }
    }

    private fun stopRecordSound(){
        if (record!!.isPlaying) {
            record!!.pause()
            record!!.seekTo(0)
        }
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