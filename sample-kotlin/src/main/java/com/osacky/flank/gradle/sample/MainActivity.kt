package com.osacky.flank.gradle.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osacky.flank.gradle.sample.kotlin.R

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }
}
