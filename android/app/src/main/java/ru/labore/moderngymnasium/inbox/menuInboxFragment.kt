package ru.labore.moderngymnasium.inbox

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.labore.moderngymnasium.R

class menuInboxFragment : Fragment() {

    companion object {
        fun newInstance() = menuInboxFragment()
    }

    private lateinit var viewModel: MenuInboxViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.menu_inbox_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MenuInboxViewModel::class.java)
        // TODO: Use the ViewModel
    }

}