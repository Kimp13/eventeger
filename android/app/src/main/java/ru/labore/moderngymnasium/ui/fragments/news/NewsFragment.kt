package ru.labore.moderngymnasium.ui.fragments.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.ui.base.ListElementFragment

class NewsFragment(
    controls: Companion.ListElementFragmentControls
) : ListElementFragment(controls) {
    override val viewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}