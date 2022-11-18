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
     * Broadcast intent action sent by the download manager when a download completes.
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

        // Register "download completion" receiver
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

        // The receiver can receive broadcasts from other Apps.
        // ACTION_DOWNLOAD_COMPLETE = Broadcast intent action sent by the download manager when a download completes.
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

    }

    /**
     * override 03 - onPause()
     */
    override fun onPause() {
        super.onPause()

        // Unregister a previously registered BroadcastReceiver.
        unregisterReceiver(receiver)

    }

    /**
     * fun 01 - download()
     */
    private fun download() {

        // 1. Prepare Custom Query with Constraints to be executed
        val request = DownloadManager.Request(Uri.parse(filePath))

            // Title: LoadApp
            // Set the title of this download, to be displayed in notifications (if enabled).
            // If no title is given, a default one will be assigned based on the download filename,
            // once the download starts.
            .setTitle(getString(R.string.app_name))

            // Description:  (1) Description info (2) File Source Type (Glide / Retrofit / Udacity)
            // Set a description of this download, to be displayed in notifications (if enabled)
            .setDescription(String.format(getString(R.string.app_description), fileSourceType))

            // Constraint: Specify that to run this download, the device needs to be plugged in.
            .setRequiresCharging(false)

            // Constraint: Set whether this download may proceed over a metered network connection.
            .setAllowedOverMetered(true)

            // Constraint: Set whether this download may proceed over a roaming connection.
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

        // We are checking if the device running the app has Android SDK 26 or up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Create the notification channel based on:
            // 1. CHANNEL_ID = "channelId"
            // 2. CHANNEL_NAME = "notification_channel"
            // 3. IMPORTANCE_DEFAULT = 3 (Stored in native NotificationManger.class)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Sets whether notification posted to this channel should vibrate.
            channel.enableVibration(true)

            // Sets whether notifications posted to this channel should display notification lights,
            // on devices that support that feature.
            channel.enableLights(true)

            // Sets the user visible description of this channel to "Loading Completed"
            channel.description = getString(R.string.notification_content)

            // We are trying to get an instance of NotificationManager by calling getSystemService()
            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )

            // We are passing the channel object with following configuration:
            // 1. Signature: Channel ID, Name and Importance Level
            // 2. Enabled: Device Vibration, Notification Lights
            // 3. Description: Loading Completed
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

        // We are passing the channel ID to the notification builder as a parameter
        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )

            // Notification icon: @drawable/ic_assistant.xml
            .setSmallIcon(R.drawable.ic_assistant)

            // Notification Title: Udacity: Android Kotlin Nanodegree
            .setContentTitle(applicationContext.getString(R.string.notification_title))

            // Notification Content: Loading Completed
            .setContentText(getString(R.string.notification_content))

            // We are making sure that the notification message will be automatically canceled
            // when the user will click it in the panel.
            .setAutoCancel(true)

            // Notification CTA Button: Check the Status, it will navigate the user to the DetailActivity
            .addAction(
                R.drawable.abc_vector_test,
                getString(R.string.notification_detail_button),
                pendingIntent
            )


        // We are getting an instance of the NotificationManager
        notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        // This final step will initialize our Notification Message with customized parameters.
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
