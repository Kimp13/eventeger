package ru.labore.eventeger.ui.fragments.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import ru.labore.eventeger.R
import ru.labore.eventeger.ui.base.BaseFragment

class NewsFragment : BaseFragment() {
    override val viewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }
}