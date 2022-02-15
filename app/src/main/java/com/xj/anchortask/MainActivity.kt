package com.xj.anchortask

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xj.anchortask.anchorTask.AnchorTaskTestActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var btn_anchortask = findViewById<TextView>(R.id.btn_anchortask)
        btn_anchortask.setOnClickListener {
            startActivity(Intent(this@MainActivity, AnchorTaskTestActivity::class.java))
        }
    }
}