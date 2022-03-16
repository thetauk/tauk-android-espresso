/*
 * MIT License
 *
 * Copyright (c) 2022 Tauk, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.aj.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL


class StocksActivity : AppCompatActivity() {
    private var fetchResultsButton: Button? = null
    private var resultsViewLayout: LinearLayout? = null
    private var symbolDropdown: Spinner? = null
    private var openText: TextView? = null
    private var lowText: TextView? = null
    private var highText: TextView? = null
    private var currentText: TextView? = null
    private var progressBar: ProgressBar? = null

    //    private val ACCESS_KEY: String = "5fa9883e01d4b44664c8a9543141cb8d"
    private val ACCESS_KEY: String = "c8jviu2ad3i8fk1k5c80"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stocks)
        title = "Stocks"

        // Setup Dropdown spinner
        symbolDropdown = findViewById<View>(R.id.symbolSpinner) as Spinner
        var items = arrayOf("Apple (AAPL)", "Tesla (TSLA)", "Amazon (AMZN)", "Alphabet (GOOGL)")
        symbolDropdown?.adapter =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items)

        progressBar = findViewById(R.id.progressBar)
        fetchResultsButton = findViewById(R.id.fetchResultsButton)
        fetchResultsButton!!.setOnClickListener { fetchResults() }

        resultsViewLayout = findViewById(R.id.resultsView)

        openText = findViewById(R.id.openText)
        lowText = findViewById(R.id.lowText)
        highText = findViewById(R.id.highText)
        currentText = findViewById(R.id.currentText)
    }

    private fun fetchResults() {
        val symbol: String = symbolDropdown?.selectedItem.toString().split(" (")[1].replace(")", "")
        val endpoint = "https://finnhub.io/api/v1/quote"
        val url = URL("$endpoint?token=$ACCESS_KEY&symbol=$symbol")
        Log.i("TaukDemo", "Fetching stocks symbol from: $url")

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val context = this

        resultsViewLayout?.visibility = View.GONE
        progressBar?.visibility = View.VISIBLE


        CountingIdlingResourceSingleton.increment()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                this@StocksActivity.runOnUiThread {
                    progressBar?.visibility = View.GONE
                }
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        val responseData: String = response.body!!.string()
                        val jsonObject = JSONObject(responseData)
                        val open = jsonObject.getDouble("o")
                        val low = jsonObject.getDouble("l")
                        val high = jsonObject.getDouble("h")
                        val current = jsonObject.getDouble("c")
                        Log.i("TaukDemo", "$responseData")
                        this@StocksActivity.runOnUiThread {
                            resultsViewLayout?.visibility = View.VISIBLE
                            openText?.text = "$open"
                            lowText?.text = "$low"
                            highText?.text = "$high"
                            currentText?.text = "$current"
                        }
                    }
                    CountingIdlingResourceSingleton.decrement()
                }
            }
        })


    }
}