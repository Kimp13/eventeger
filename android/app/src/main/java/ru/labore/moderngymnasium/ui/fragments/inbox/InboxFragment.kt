package ru.labore.moderngymnasium.ui.fragments.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_inbox.*
import kotlinx.coroutines.launch
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.AppRepository
import ru.labore.moderngymnasium.ui.activities.MainActivity
import ru.labore.moderngymnasium.ui.base.ListElementFragment

class InboxFragment(
    controls: Companion.ListElementFragmentControls
) : ListElementFragment(controls) {
    override val viewModel: InboxViewModel by viewModels()
    private var noAnnouncementsTextView: View? = null
    private var loading = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inbox, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        launch {
            bindUI()
        }

        inboxRecyclerView.apply {
            val viewManager = LinearLayoutManager(requireActivity())
            val divider = DividerItemDecoration(requireContext(), viewManager.orientation)

            divider.setDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.inbox_recycler_view_divider,
                    null
                )!!
            )

            addItemDecoration(divider)
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewModel.getAdapter(controls)

            scrollBy(0, savedInstanceState?.getInt("scrollY") ?: 0)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (layoutManager is LinearLayoutManager) {
                        val firstItemIndex = (layoutManager as LinearLayoutManager)
                            .findFirstCompletelyVisibleItemPosition()

                        viewModel.appRepository.unreadAnnouncements.let { list ->
                            if (firstItemIndex < list.size) {
                                for (i in list.size - 1 downTo firstItemIndex) {
                                    list.removeAt(i)
                                }

                                requireActivity().let {
                                    if (it is MainActivity) {
                                        it.updateInboxBadge(list.size)
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private suspend fun updateAnnouncements(
        forceFetch: AppRepository.Companion.UpdateParameters =
            AppRepository.Companion.UpdateParameters.DETERMINE,
        refresh: Boolean = false
    ) {
        viewModel.updateAnnouncements(
            requireActivity(),
            forceFetch,
            refresh
        )
    }

    private suspend fun refreshUI() {
        updateAnnouncements(
            AppRepository.Companion.UpdateParameters.UPDATE,
            true
        )

        inboxRefreshLayout.isRefreshing = false
    }

    private suspend fun bindUI() {
        if (viewModel.itemCount == 0)
            updateAnnouncements(
                AppRepository.Companion.UpdateParameters.DETERMINE,
                true
            )

        viewModel.loading = false

        inboxRefreshLayout.setOnRefreshListener {
            launch {
                refreshUI()

                if (viewModel.itemCount == 0) {
                    if (noAnnouncementsTextView == null) {
                        noAnnouncementsTextView = LayoutInflater
                            .from(context)
                            .inflate(
                                R.layout.inbox_no_announcements_textview,
                                inboxFragmentLayout
                            )
                    }
                } else if (noAnnouncementsTextView != null) {
                    for (i in 0 until inboxFragmentLayout.childCount)
                        if (
                            inboxFragmentLayout.getChildAt(i) == noAnnouncementsTextView
                        )
                            inboxFragmentLayout.removeViewAt(i)

                    noAnnouncementsTextView = null
                }
            }
        }

        if (viewModel.itemCount == 0) {
            noAnnouncementsTextView = LayoutInflater
                .from(context)
                .inflate(
                    R.layout.inbox_no_announcements_textview,
                    inboxFragmentLayout
                )
        }

        loading = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("scrollY", inboxRecyclerView.computeVerticalScrollOffset())

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        viewModel.appRepository.unreadAnnouncementsPushListener = null

        super.onDestroy()
    }
}