package com.example.news_aggregator.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.example.news_aggregator.R
import com.example.news_aggregator.receivers.NotificationReceiver
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_notifications.*
import kotlinx.android.synthetic.main.content_main.top_app_bar

/**
 * Notification activity for choosing when to display notifications.
 * @property mAuth FirebaseAuth
 * @property database FirebaseFirestore
 */
class NotificationActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    /**
     * Initialize the activity.
     * @param savedInstanceState Bundle?
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        val toolbar = top_app_bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        (notification_menu.editText as? AutoCompleteTextView)?.inputType = EditorInfo.TYPE_NULL
        getNotificationDuration()
        //List of items to choose for how often to get notifications.
        val items = listOf(
            getString(R.string.never),
            getString(R.string._6_hours),
            getString(R.string._12_hours),
            getString(R.string._24_hours)
        )
        val adapter = ArrayAdapter(this.applicationContext, R.layout.navigation_list_item, items)
        (notification_menu.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        val hourInMillis: Long = 60000 * 60
        val button = notification_button
        //On click listener for saving how often to get notifications.
        button.setOnClickListener {
            when (filled_exposed_dropdown.text.toString()) {
                getString(R.string._6_hours) -> {
                    cancelAlarmManager()
                    addSavedTimeToAccount(hourInMillis * 6, 6)
                }
                getString(R.string._12_hours) -> {
                    cancelAlarmManager()
                    addSavedTimeToAccount(hourInMillis * 12, 12)
                }
                getString(R.string._24_hours) -> {
                    cancelAlarmManager()
                    addSavedTimeToAccount(hourInMillis * 24, 24)
                }
                getString(R.string.never) -> {
                    cancelAlarmManager()
                    addSavedTimeToAccount(0, 0)
                }
            }
        }
    }

    /**
     * start the alarm manager for getting notifications.
     * @param time Long
     */
    private fun startAlarmManager(time: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val alarmTime = System.currentTimeMillis()
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + time, pendingIntent)
    }

    /**
     * cancel the alarm manager for getting notifications.
     */
    private fun cancelAlarmManager() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0)
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Add when the user gets notifications to FireStore.
     * @param time Long
     * @param duration Int
     */
    private fun addSavedTimeToAccount(time: Long, duration: Int) {
        val ref =
            database.collection(getString(R.string.firestore_users)).document(mAuth.uid.toString())
        ref.update(getString(R.string.firestore_duration), duration)
            .addOnSuccessListener {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.snackbar_notifications_settings_saved),
                    Snackbar.LENGTH_LONG
                ).show()
                if (duration != 0) {
                    startAlarmManager(time)
                }
            }.addOnFailureListener {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.snackbar_change_notification),
                        Snackbar.LENGTH_LONG
                    ).show()
            }
    }

    /**
     * Get how often the user should get notifications to display in the dropdown menu when the
     * activity is opened.
     */
    private fun getNotificationDuration() {
        val ref =
            database.collection(getString(R.string.firestore_users)).document(mAuth.uid.toString())
        var durationPosition = 0
        ref.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot?.get(getString(R.string.firestore_duration)) != null) {
                    when (snapshot.get(getString(R.string.firestore_duration)) as Long) {
                        6L -> {
                            durationPosition = 1
                        }
                        12L -> {
                            durationPosition = 2
                        }
                        24L -> {
                            durationPosition = 3
                        }
                        0L -> {
                            durationPosition = 0
                        }
                    }
                }
                filled_exposed_dropdown.setText(
                    filled_exposed_dropdown.adapter.getItem(
                        durationPosition
                    ).toString(), false
                )
            }
    }
}