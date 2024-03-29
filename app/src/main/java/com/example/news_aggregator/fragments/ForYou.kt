package com.example.news_aggregator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news_aggregator.R
import com.example.news_aggregator.adapters.ArticleRecyclerAdapter
import com.example.news_aggregator.interfaces.TopSpacingItemDecoration
import com.example.news_aggregator.utils.NewsAPI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_for_you.*

/**
 * For you fragment to display articles based on the key terms.
 * @property articleAdapter ArticleRecyclerAdapter
 * @property mAuth FirebaseAuth
 * @property database FirebaseFirestore
 * @property ref DocumentReference
 */
class ForYou : Fragment() {
    private lateinit var articleAdapter: ArticleRecyclerAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var ref: DocumentReference

    /**
     * On create function.
     * @param savedInstanceState Bundle?
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
    }

    /**
     * Inflate the fragment.
     * @param inflater LayoutInflater
     * @param container ViewGroup?
     * @param savedInstanceState Bundle?
     * @return View?
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_for_you, container, false)
    }
    companion object {
        fun newInstance() = ForYou().apply {
            arguments = Bundle().apply {
            }
        }
    }

    /**
     * attach the adapter to the recycle view.
     * @param view View
     * @param savedInstanceState Bundle?
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view_for_you.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingItemDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingItemDecoration)
            articleAdapter = ArticleRecyclerAdapter()
            adapter = articleAdapter
        }
    }

    /**
     * Calls addDataSet every time the fragment is shown.
     */
    override fun onResume() {
        super.onResume()
        addDataSet()
    }

    /**
     * Adds articles to the adapter to be displayed.
     */
    private fun addDataSet() {
        //If the user isn't logged in just display top headline articles.
        if (mAuth.currentUser == null) {
            view?.let {
                context?.let { it1 ->
                    NewsAPI.getArticles(
                        getString(R.string.news_api_top_headlines),
                        getString(R.string.news_api_country),
                        getString(R.string.news_api_gb),
                        getString(R.string.news_api_published_at),
                        false,
                        it1
                    ) { list ->
                        if (list.size > 0) {
                            articleAdapter.submitList(list)
                            activity?.runOnUiThread {
                                articleAdapter.notifyDataSetChanged()
                            }
                        } else {
                            view?.let {
                                Snackbar.make(
                                    it,
                                    getString(R.string.snackbar_error_getting_articles),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        } else {
            var parameters = ""
            ref = database.collection(getString(R.string.firestore_users))
                .document(mAuth.uid.toString())
            //Get the users key terms.
            ref.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val keyTerms =
                        document.data?.get(getString(R.string.firestore_key_terms)) as ArrayList<*>
                    for (term in keyTerms) {
                        parameters += "$term OR "
                    }
                    parameters = parameters.dropLast(4)
                    //If there are no key terms display top headlines.
                    if (parameters == "") {
                        view?.let {
                            context?.let { it1 ->
                                NewsAPI.getArticles(
                                    getString(R.string.news_api_top_headlines),
                                    getString(R.string.news_api_country),
                                    getString(R.string.news_api_gb),
                                    getString(R.string.news_api_published_at),
                                    false,
                                    it1
                                ) { list ->
                                    if (list.size > 0) {
                                        articleAdapter.submitList(list)
                                        activity?.runOnUiThread {
                                            articleAdapter.notifyDataSetChanged()
                                        }
                                    } else {
                                        view?.let {
                                            Snackbar.make(
                                                it,
                                                getString(R.string.snackbar_error_getting_articles),
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        //Display articles based on the key terms.
                        view?.let {
                            context?.let { it1 ->
                                NewsAPI.getArticles(
                                    getString(R.string.news_api_everything),
                                    getString(R.string.news_api_q),
                                    parameters,
                                    getString(R.string.news_api_relevancy),
                                    false,
                                    it1
                                ) { list ->
                                    if (list.size > 0) {
                                        articleAdapter.submitList(list)
                                        activity?.runOnUiThread {
                                            articleAdapter.notifyDataSetChanged()
                                        }
                                    } else {
                                        articleAdapter.submitList(list)
                                        activity?.runOnUiThread {
                                            articleAdapter.notifyDataSetChanged()
                                        }
                                        view?.let {
                                            Snackbar.make(
                                                it,
                                                getString(R.string.snackbar_no_articles),
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    view?.let {
                        Snackbar.make(
                            it,
                            getString(R.string.snackbar_cannot_get_key_term),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }.addOnFailureListener {
                view?.let {
                    Snackbar.make(
                        it,
                        getString(R.string.snackbar_cannot_get_key_term),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}