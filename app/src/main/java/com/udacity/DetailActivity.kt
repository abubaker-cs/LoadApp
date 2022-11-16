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

    //
    private lateinit var _binding: ActivityDetailBinding

    //
    private lateinit var _bindingDetailContent: ContentDetailBinding

    /**
     * onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate Layout: @layout/activity_main.xml
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        _bindingDetailContent = _binding.detailContent

        //
        _binding.lifecycleOwner = this

        //
        setSupportActionBar(_binding.toolbar)

        //
        _bindingDetailContent.fileName.text = intent.getStringExtra(KEY_FILENAME)
        _bindingDetailContent.status.text = intent.getStringExtra(KEY_STATUS)

        //
        if (intent.getStringExtra(KEY_STATUS) == "Failed") {

            //
            _bindingDetailContent.status.setTextColor(Color.RED)

        } else {

            //
            _bindingDetailContent.status.setTextColor(Color.GREEN)

        }

        //
        _bindingDetailContent.btnOk.setOnClickListener {

            //
            finish()

        }

    }

}
