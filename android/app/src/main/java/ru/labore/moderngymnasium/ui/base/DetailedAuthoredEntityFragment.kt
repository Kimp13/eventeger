package ru.labore.moderngymnasium.ui.base

import ru.labore.moderngymnasium.data.db.entities.AuthoredEntity

abstract class DetailedAuthoredEntityFragment(
    controls: Companion.ListElementFragmentControls,
    val item: AuthoredEntity
) : ListElementFragment(controls) {

}