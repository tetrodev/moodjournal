package com.example.moodjournal.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodjournal.R
import com.example.moodjournal.data.Article
import com.example.moodjournal.data.ArticleRepository

class ArticlesFragment : Fragment() {

    private lateinit var articleRepository: ArticleRepository
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_articles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articleRepository = ArticleRepository()
        recyclerView = view.findViewById(R.id.articlesRecycler)

        val articles = articleRepository.getAllArticles()
        val adapter = ArticleAdapter(articles) { article ->
            showArticleDialog(article)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun showArticleDialog(article: Article) {
        AlertDialog.Builder(requireContext())
            .setTitle(article.title)
            .setMessage(article.content)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    class ArticleAdapter(
        private val articles: List<Article>,
        private val onClick: (Article) -> Unit
    ) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_article, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val article = articles[position]
            holder.bind(article)
            holder.itemView.setOnClickListener { onClick(article) }
        }

        override fun getItemCount(): Int = articles.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleText: TextView = itemView.findViewById(R.id.articleTitle)
            private val authorText: TextView = itemView.findViewById(R.id.articleAuthor)

            fun bind(article: Article) {
                titleText.text = article.title
                authorText.text = article.author
            }
        }
    }
}