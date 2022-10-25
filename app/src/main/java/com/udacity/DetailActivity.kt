package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityDetailBinding
    private var name = ""
    private var status = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate Layout: @layout/activity_main.xml
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)

        //
        _binding.lifecycleOwner = this

        setSupportActionBar(_binding.toolbar)

    }

}
