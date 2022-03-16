package io.aj.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var stocksButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Tauk Demo"


        stocksButton = findViewById<View>(R.id.stocksButton) as Button
        stocksButton!!.setOnClickListener { openStocksActivity() }


    }

    private fun openStocksActivity() {
        val intent = Intent(this, StocksActivity::class.java)
        startActivity(intent)
    }
}