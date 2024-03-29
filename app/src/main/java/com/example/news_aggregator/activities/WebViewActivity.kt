package com.example.news_aggregator.activities

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.news_aggregator.R
import kotlinx.android.synthetic.main.activity_web_view.*

/**
 * Activity for displaying the full article in a web view.
 * @property webView WebView
 */
class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    /**
     * initializing the activity and getting and displaying the url of the activity.
     * @param savedInstanceState Bundle?
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val toolbar = top_app_bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val url = intent.extras?.getString(getString(R.string.article_data_article_url))
        webView = web_view
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        webView.loadUrl(url)
    }
}