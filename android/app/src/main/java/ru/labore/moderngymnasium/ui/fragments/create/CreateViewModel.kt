package ru.labore.moderngymnasium.ui.fragments.create

import android.app.Activity
import android.app.Application
import org.threeten.bp.ZonedDateTime
import ru.labore.moderngymnasium.data.db.entities.ClassEntity
import ru.labore.moderngymnasium.data.db.entities.RoleEntity
import ru.labore.moderngymnasium.ui.base.BaseViewModel

class CreateViewModel(
    application: Application
) : BaseViewModel(application) {
    val checkedRoles: HashMap<Int, HashSet<Int>> = hashMapOf()

    suspend fun createAnnouncement(
        activity: Activity,
        text: String
    ) = makeRequest(
        activity,
        {
            appRepository.createAnnouncement(text, checkedRoles)
        }
    )

    suspend fun createAnnouncement(
        activity: Activity,
        text: String,
        beginsAt: ZonedDateTime?,
        endsAt: ZonedDateTime?
    ) = makeRequest(
        activity,
        {
            appRepository.createAnnouncement(text, checkedRoles, beginsAt, endsAt)
        }
    )

    suspend fun getRoles(activity: Activity): HashMap<Int, RoleEntity> {
        var retval: HashMap<Int, RoleEntity>? = null

        makeRequest(
            activity,
            {
                retval = appRepository.getKeyedRoles(
                    appRepository.announceMap.rolesIds
                )
            }
        )

        return retval ?: hashMapOf()
    }

    suspend fun getClasses(activity: Activity): HashMap<Int, ClassEntity> {
        var retval: HashMap<Int, ClassEntity>? = null

        makeRequest(
            activity,
            {
                retval = appRepository.getKeyedClasses(
                    appRepository.announceMap.classesIds
                )
            }
        )

        return retval ?: hashMapOf()
    }
}