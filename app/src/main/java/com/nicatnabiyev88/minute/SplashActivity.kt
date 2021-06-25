package com.nicatnabiyev88.minute

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private fun ifHasInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
            .isConnected
    }

    var smallerScreenSize = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val config = resources.configuration
        if(config.smallestScreenWidthDp <= 360){
            smallerScreenSize = true
        }

        //animations
        val topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation)
        val bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation)

        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        if (ifHasInternet(this)){
            var splashThread = object : Thread() {
                override fun run() {
                    try {
                        synchronized(this) {
                            splash_activity_icon.animation = bottomAnim
                            splash_activity_up_img.animation = bottomAnim
                            splash_activity_one_minute.animation = topAnim
                            splash_activity_down_img.animation = topAnim
                            sleep(2000)
                        }
                    } catch (e: Exception){
                        println(e.localizedMessage)
                    } finally {
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
                        finish()
                    }
                }
            };
            splashThread.start()
        } else {
            val dialogBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
            val inflater = this.layoutInflater
            var view = inflater.inflate(R.layout.no_wifi_conn, null)
            if (smallerScreenSize){
                view = inflater.inflate(R.layout.no_wifi_conn_small, null)

            }
            dialogBuilder.setView(view)

            val dialog = dialogBuilder.create()
            if(dialog.window != null){
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.setCancelable(false)
            dialog.show()

            val btnPlayAgain = view.findViewById<Button>(R.id.no_wifi_btn)
            btnPlayAgain.setOnClickListener(View.OnClickListener {
                val pid = Process.myPid()
                Process.killProcess(pid)
                dialog.dismiss()
            })
        }
    }
}