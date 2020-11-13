package com.example.news_aggregator.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.news_aggregator.R
import com.example.news_aggregator.activities.ArticleActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.article_list_item.view.*
import kotlinx.android.synthetic.main.key_term_list_item.view.*

class KeyTermRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<String> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.key_term_list_item, parent, false)

        return KeyTermViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is KeyTermViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(keyTermList: List<String>) {
        items = keyTermList
    }

    class KeyTermViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val keyTermTextView: TextView = itemView.key_term_text_view
        private val deleteButton = itemView.delete_button
        var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        var ref: DatabaseReference = database.getReference("users/${mAuth.uid}/key_terms")

        fun bind(keyTerm: String) {
            keyTermTextView.text = keyTerm

            deleteButton.setOnClickListener {
                val keyTermListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val keyTerms = dataSnapshot.value
                        var itemKey = ""
                        if (keyTerms != null) {
                            for ((key, value) in keyTerms as HashMap<*, *>) {
                                if (value == keyTerm) {
                                    itemKey = key as String
                                }
                            }
                            ref.child(itemKey).removeValue()
                            Log.e("Item deleted", keyTerm)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                }
                ref.addListenerForSingleValueEvent(keyTermListener)
            }
        }
    }
}