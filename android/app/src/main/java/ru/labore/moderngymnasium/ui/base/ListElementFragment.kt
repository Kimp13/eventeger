package ru.labore.moderngymnasium.ui.base

abstract class ListElementFragment(
    val controls: ListElementFragmentControls
) : BaseFragment() {
    companion object {
        data class ListElementFragmentControls(
            val push: (ListElementFragment) -> Unit,
            val finish: () -> Unit
        )
    }
}