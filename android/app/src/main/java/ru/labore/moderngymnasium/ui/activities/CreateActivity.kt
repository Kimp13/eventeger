package ru.labore.moderngymnasium.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.coroutines.launch
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.db.entities.ClassEntity
import ru.labore.moderngymnasium.data.db.entities.RoleEntity
import ru.labore.moderngymnasium.ui.base.BaseActivity
import ru.labore.moderngymnasium.ui.views.LabelledCheckbox
import ru.labore.moderngymnasium.ui.views.ParentCheckbox
import ru.labore.moderngymnasium.utils.hideKeyboard
import java.util.*

class CreateActivity: BaseActivity() {
    private val checkedRoles: HashMap<Int, HashSet<Int>> = hashMapOf()
    private var uiLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        createFragmentLayout.setOnClickListener { hideKeyboard() }
        createAnnouncementRoleChoose.setOnClickListener { hideKeyboard() }
        createAnnouncementSubmitButton.setOnClickListener { createAnnouncement() }
        createAnnouncementBackButton.setOnClickListener {
            finish()
        }

        loadUI()
    }

    private fun createAnnouncement() {
        if (checkedRoles.keys.size == 0) {
            Toast.makeText(
                this,
                getString(R.string.choose_recipient_role),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val text = createAnnouncementEditText
                .text
                .toString()
                .trim()

            if (text.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.enter_announcement_text),
                    Toast.LENGTH_SHORT
                ).show()
            } else launch {
                try {
                    createAnnouncementSubmitButton.visibility = View.GONE
                    createAnnouncementSubmitProgress.visibility = View.VISIBLE

                    repository.createAnnouncement(text, checkedRoles)

                    createAnnouncementEditText.setText("")
                } catch (e: Exception) {
                }

                createAnnouncementSubmitButton.visibility = View.VISIBLE
                createAnnouncementSubmitProgress.visibility = View.GONE
            }
        }
    }

    private fun childCheckedChangeHandler(isChecked: Boolean, roleId: Int, classId: Int) {
        if (!checkedRoles.containsKey(roleId))
            checkedRoles[roleId] = hashSetOf()

        if (isChecked)
            checkedRoles[roleId]!!.add(classId)
        else
            checkedRoles[roleId]!!.remove(classId)

        println(checkedRoles.toString())
    }

    private fun loadUI() = launch {
        var roles = HashMap<Int, RoleEntity>()
        var classes = HashMap<Int, ClassEntity>()

        makeRequest({
            roles = repository.getKeyedRoles(
                repository.announceMap.rolesIds
            )

            classes = repository.getKeyedClasses(
                repository.announceMap.classesIds
            )
        }).join()

        repository.announceMap.entries.forEach { roleMap ->
            val role = roles[roleMap.key]
            val checkboxLayout: View

            if (role != null) {
                if (roleMap.value.size == 1) {
                    val onlyClass = classes[roleMap.value[0]]!!

                    checkboxLayout = LabelledCheckbox(
                        this@CreateActivity,
                        "${role.name}, ${
                            onlyClass.grade
                        }${
                            onlyClass.letter
                        } класс"
                    )

                    checkboxLayout.outerCheckedChangeHandler = { checked ->
                        childCheckedChangeHandler(
                            checked,
                            role.id,
                            roleMap.value[0]
                        )
                    }
                } else {
                    val gradedClasses = HashMap<Int, ArrayList<Int>>()

                    roleMap.value.contents.forEach { classId ->
                        val classEntity = classes[classId]

                        if (classEntity != null) {
                            if (gradedClasses.containsKey(classEntity.grade)) {
                                gradedClasses[classEntity.grade]!!.add(classId)
                            } else {
                                gradedClasses[classEntity.grade] = arrayListOf(classId)
                            }
                        }
                    }

                    checkboxLayout = ParentCheckbox(
                        this@CreateActivity,
                        "Роль: ${role.name}"
                    )

                    gradedClasses.entries.forEach {
                        val childCheckbox: View

                        if (it.value.size == 1) {
                            val onlyClass = classes[it.value[0]]!!

                            childCheckbox = LabelledCheckbox(
                                this@CreateActivity,
                                "${
                                    onlyClass.grade
                                }${
                                    onlyClass.letter
                                } класс"
                            )

                            childCheckbox.outerCheckedChangeHandler = { checked ->
                                childCheckedChangeHandler(
                                    checked,
                                    roleMap.key,
                                    onlyClass.id
                                )
                            }
                        } else {
                            childCheckbox = ParentCheckbox(
                                this@CreateActivity,
                                "${it.key}-я параллель"
                            )

                            it.value.forEach { classId ->
                                val leafCheckbox = LabelledCheckbox(
                                    this@CreateActivity,
                                    "${classes[classId]!!.letter} класс"
                                )

                                leafCheckbox.outerCheckedChangeHandler = { checked ->
                                    childCheckedChangeHandler(
                                        checked,
                                        roleMap.key,
                                        classId
                                    )
                                }

                                childCheckbox.checkboxLayout.addView(leafCheckbox)
                            }
                        }

                        checkboxLayout.checkboxLayout.addView(childCheckbox)
                    }
                }

                createAnnouncementRoleChoose.addView(checkboxLayout)
            }
        }

    }
}