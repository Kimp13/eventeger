package ru.labore.eventeger.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.labore.eventeger.data.db.entities.AuthoredEntity

const val ITEM_KEY = "item"

abstract class DetailedAuthoredEntityFragment() : BaseFragment() {
    lateinit var item: AuthoredEntity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        item = arguments?.getParcelable(ITEM_KEY)!!
    }
}