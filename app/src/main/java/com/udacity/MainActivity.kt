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

    // We are defining few variables which will be later on used to make references to the views
    private lateinit var _binding: ActivityMainBinding
    private lateinit var _bindingContent: ContentMainBinding

    // This reference will be used inside the download() function to execute the download query
    // through downloadManager.enqueue()
    private var downloadReference: Long = 0

    // fileSourceType: It will be used as a reference of the source type from where the user’s
    // selected file will be downloaded, i.e. Glide, Retrofit or Udacity (Github Account)
    private lateinit var fileSourceType: String

    // This will store the actual URL of the file to be downloaded, the information about the
    // urls is stored in the companion object in following variables:
    // 1. URL_GLIDE
    // 2. URL_RETROFIT
    // 3. URL_UDACITY
    private lateinit var filePath: String

    // This variable will be used to create a NotificationManager to inform the user that something
    // has happened in the background.
    private lateinit var notificationManager: NotificationManager

    // This reference will be used inside the download() function to execute the download query
    // through DownloadManager
    // *****
    // Since we want the user to navigate from the Notification Message to the DetailedActivity,
    // that’s why we will be using pendingIntent, which will help us to:
    private lateinit var pendingIntent: PendingIntent

    /**
     * BroadcastReceiver
     */
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            //
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            //
            if (id == -1L)
                return

            //
            id?.let { intentId ->

                //
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

                //
                val query = DownloadManager.Query()

                //
                query.setFilterById(intentId)

                //
                val cursor = downloadManager.query(query)

                //
                if (cursor.moveToFirst()) {

                    //
                    val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    //
                    val downloadStatus =
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(index))
                            getString(R.string.success_status)
                        else
                            getString(R.string.failed_status)

                    //
                    sendNotifications(downloadStatus)

                    // Custom Button State: Completed
                    _bindingContent.customButton.buttonState = ButtonState.Completed

                }

            }

        }
    }

    /**
     * override 01 - onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Inflate Layout: @layout/activity_main.xml and bind @layout/content_main.xml
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        _bindingContent = _binding.contentMain

        // Specify the current activity as the lifecycle owner.
        _binding.lifecycleOwner = this

        // This will enable the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(_binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // onClickEvent: Download (Button)
        _bindingContent.customButton.setOnClickListener {
            if (::filePath.isInitialized) {

                // Custom Button State: Loading
                _bindingContent.customButton.buttonState = ButtonState.Loading
                download()

            } else
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.select_from_list),
                    Toast.LENGTH_SHORT
                ).show()
        }

        // onClickEvent: File Source (Radio Group)
        _binding.contentMain.radioGroup.setOnCheckedChangeListener { _, index ->

            when (index) {

                // Glide
                R.id.radio_glide -> {
                    filePath = URL_GLIDE
                    fileSourceType = getString(R.string.source_glide)
                }

                // Retrofit
                R.id.radio_retrofit -> {
                    filePath = URL_RETROFIT
                    fileSourceType = getString(R.string.source_retrofit)
                }

                // LoadApp (Udacity)
                R.id.radio_load_app -> {
                    filePath = URL_UDACITY
                    fileSourceType = getString(R.string.source_udacity)
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
     * fun 01 - download()
     */
    private fun download() {

        // 1. Prepare Custom Query with Constraints to be executed
        val request = DownloadManager.Request(Uri.parse(filePath))
            .setTitle(getString(R.string.app_name))
            .setDescription(String.format(getString(R.string.app_description), fileSourceType))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        // 2. Execute the Query: Enqueue puts the download request in the queue.
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadReference = downloadManager.enqueue(request)
    }

    /**
     * fun 02 - sendNotifications()
     */
    private fun sendNotifications(downloadStatus: String) {

        /**
         * Create Notification Channel
         */
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

        /**
         * Intent Configuration: This will forward details to the DetailedActivity:
         * 1. Download Status of the File
         * 2. Name of the SOURCE
         */
        intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra(KEY_STATUS, downloadStatus)
        intent.putExtra(KEY_FILENAME, fileSourceType)

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
         * Notification Message (Parameters)
         */
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "notification_channel"

    }

}
