package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityDetailBinding
    private lateinit var _bindingDetailContent: ContentDetailBinding

    private var name = ""
    private var status = ""

    /**
     * onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate Layout: @layout/activity_main.xml
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        _bindingDetailContent = _binding.detailContent

        _binding.lifecycleOwner = this

        setSupportActionBar(_binding.toolbar)

        //

    }

}
