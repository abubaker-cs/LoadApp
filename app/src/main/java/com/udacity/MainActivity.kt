package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.udacity.Constants.KEY_FILENAME
import com.udacity.Constants.KEY_STATUS
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding
import com.udacity.utils.ButtonState


class MainActivity : AppCompatActivity() {

    // Binding
    private lateinit var _binding: ActivityMainBinding
    private lateinit var _bindingDetailContent: ContentMainBinding

    //
    private var downloadID: Long = 0

    //
    private lateinit var notificationManager: NotificationManager

    //
    private lateinit var pendingIntent: PendingIntent

    //
    private lateinit var fileName: String

    //
    private lateinit var url: String

    /**
     * override 01 - onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate Layout: @layout/activity_main.xml
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        _bindingDetailContent = _binding.contentMain

        //
        _binding.lifecycleOwner = this

        setSupportActionBar(_binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        _bindingDetailContent.customButton.setOnClickListener {
            if (::url.isInitialized) {

                // Custom Button State: Loading
                _bindingDetailContent.customButton.buttonState = ButtonState.Loading
                download()

            } else
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.select_from_list),
                    Toast.LENGTH_SHORT
                ).show()
        }

        // Radio Button : Clicked ?
        _binding.contentMain.radioGroup.setOnCheckedChangeListener { _, index ->
            when (index) {
                R.id.radio_glide -> {
                    url = URL_GLIDE
                    fileName = getString(R.string.source_glide)
                }

                R.id.radio_retrofit -> {
                    url = URL_RETROFIT
                    fileName = getString(R.string.source_retrofit)
                }
                R.id.radio_load_app -> {
                    url = URL_UDACITY
                    fileName = getString(R.string.source_udacity)
                }

            }

        }

    }

    /**
     * override 02 - onResume()
     */
    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    /**
     * override 03 - onPause()
     */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    /**
     * Receiver
     */
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (id == -1L)
                return

            id?.let { intentId ->
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query()

                query.setFilterById(intentId)

                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {

                    val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    val downloadStatus =
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(index))
                            getString(R.string.success_status)
                        else
                            getString(R.string.failed_status)

                    sendNotifications(downloadStatus)

                    // Custom Button State: Completed
                    _bindingDetailContent.customButton.buttonState = ButtonState.Completed

                }

            }

        }
    }

    /**
     * fun 01 - download()
     */
    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL_UDACITY))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    /**
     * fun 02 - sendNotifications()
     */
    private fun sendNotifications(status: String) {

        createChannel()

        intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra(KEY_STATUS, status)
        intent.putExtra(KEY_FILENAME, fileName)

        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(getString(R.string.loading_completed))
            .setAutoCancel(true)
            .addAction(
                R.drawable.abc_vector_test,
                getString(R.string.see_result),
                pendingIntent
            )

        notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * fun 03 - createChannel()
     */
    private fun createChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.enableVibration(true)
            channel.enableLights(true)

            channel.description = getString(R.string.loading_completed)
            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)

        }
    }

    /**
     * Constants
     */
    companion object {

        /**
         * URLs of the files to be downloaded
         */
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val URL_UDACITY =
            "https://codeload.github.com/udacity/nd940-c3-advanced-android-programming-project-starter/zip/refs/heads/master"

        /**
         * Channel
         */
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "notification_channel"

    }

}
