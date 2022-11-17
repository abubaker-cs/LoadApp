package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.Constants.KEY_FILENAME
import com.udacity.Constants.KEY_STATUS
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding

class DetailActivity : AppCompatActivity() {

    // We are defining few variables which will be later on used to make references to the views
    private lateinit var _binding: ActivityDetailBinding
    private lateinit var _bindingDetailContent: ContentDetailBinding

    /**
     * onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate Layout: @layout/activity_main.xml and bind @layout/content_main.xml
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        _bindingDetailContent = _binding.detailContent

        // Specify the current activity as the lifecycle owner.
        _binding.lifecycleOwner = this

        /**
         * This will allow us to hide the < Back button from the activity_main.xml
         * Reference: https://developer.android.com/guide/navigation/navigation-ui#appbarconfiguration
         */
        setSupportActionBar(_binding.toolbar)

        // Bind received data to the fileName and status TextViews
        _bindingDetailContent.fileName.text = intent.getStringExtra(KEY_FILENAME)
        _bindingDetailContent.status.text = intent.getStringExtra(KEY_STATUS)


        // This will updated text color of @+id/status to indicate failure/success
        if (intent.getStringExtra(KEY_STATUS) == "Failed") {

            // If the received value of KEY_STATUS is Fail then set the text color to RED,
            _bindingDetailContent.status.setTextColor(Color.RED)

        } else {

            // Otherwise the text color for @+id/status should be GREEN to indicate success
            _bindingDetailContent.status.setTextColor(Color.GREEN)

        }

        // onClickEvent: OK Button
        _bindingDetailContent.btnOk.setOnClickListener {

            // This will close the detail activity, and navigate the user back to the main activity.
            finish()

        }

    }

}
