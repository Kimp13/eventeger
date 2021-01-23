package ru.labore.moderngymnasium.ui.activities

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.ui.base.BaseActivity
import ru.labore.moderngymnasium.ui.views.LabelledCheckbox
import ru.labore.moderngymnasium.ui.views.ParentCheckbox
import ru.labore.moderngymnasium.utils.hideKeyboard
import java.util.*

class CreateActivity: BaseActivity() {
    private val checkedRoles: HashMap<Int, MutableList<Int>> = hashMapOf()
    private var announcementText: String? = null
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
        if (checkedRoles.containsKey(roleId)) {
            if (isChecked) {
                if (
                    checkedRoles[roleId]
                        ?.contains(classId) == false
                ) {
                    checkedRoles[roleId]?.add(classId)
                }
            } else {
                checkedRoles[roleId]?.remove(classId)
                if (checkedRoles[roleId]?.isEmpty() == true) {
                    checkedRoles.remove(roleId)
                }
            }
        } else {
            checkedRoles[roleId] = mutableListOf()

            if (isChecked) {
                checkedRoles[roleId]!!.add(classId)
            } else {
                checkedRoles[roleId]!!.remove(classId)
            }
        }
    }

    private fun loadUI() = launch {
        val roles = repository.getUserRoles().filterNotNull()
        val classes = repository.getUserClasses()

        if (announcementText != null) {
            createAnnouncementEditText.setText(announcementText)
        }

        roles.forEach { role ->
            val checkboxLayout: View

            if (classes.size == 1) {
                val firstGrade = classes.keys.elementAt(0)

                if (
                    classes[firstGrade]?.size != null &&
                    classes[firstGrade]?.size!! > 0
                ) {
                    if (classes[firstGrade]?.size == 1) {
                        checkboxLayout = LabelledCheckbox(
                            this@CreateActivity,
                            "${role.name}, ${
                                classes[firstGrade]!![0].grade}${classes[firstGrade]!![0].letter
                            }"
                        )

                        checkboxLayout.checkedChangeHandler = { checked ->
                            childCheckedChangeHandler(
                                checked,
                                role.id,
                                classes[firstGrade]!![0].id
                            )
                        }

                        createAnnouncementRoleChoose.addView(checkboxLayout)
                    } else {
                        checkboxLayout = ParentCheckbox(
                            this@CreateActivity,
                            "${role.name}, ${classes[firstGrade]!![0].grade}"
                        )

                        classes[firstGrade]!!.forEach { classEntity ->
                            val nestedCheckbox = LabelledCheckbox(
                                this@CreateActivity,
                                classEntity.letter
                            )

                            nestedCheckbox.checkedChangeHandler = { checked ->
                                childCheckedChangeHandler(checked, role.id, classEntity.id)
                            }

                            checkboxLayout.addView(nestedCheckbox)
                        }
                    }
                }
            } else if (classes.size > 1) {
                checkboxLayout = ParentCheckbox(
                    this@CreateActivity,
                    role.name
                )

                classes.keys.forEach {
                    if (
                        classes[it]?.size != null &&
                        classes[it]!!.size > 0
                    ) {
                        val childCheckbox: View

                        if (classes[it]!!.size == 1) {
                            childCheckbox = LabelledCheckbox(
                                this@CreateActivity,
                                "${classes[it]!![0].grade}${classes[it]!![0].letter}"
                            )
                        } else {
                            childCheckbox = ParentCheckbox(
                                this@CreateActivity,
                                "${classes[it]!![0].grade}"
                            )

                            classes[it]!!.forEach { classEntity ->
                                val nestedCheckbox = LabelledCheckbox(
                                    this@CreateActivity,
                                    classEntity.letter
                                )

                                nestedCheckbox.checkedChangeHandler = { checked ->
                                    childCheckedChangeHandler(checked, role.id, classEntity.id)
                                }

                                childCheckbox.addView(nestedCheckbox)
                            }
                        }

                        checkboxLayout.addView(childCheckbox)
                        createAnnouncementRoleChoose.addView(checkboxLayout)
                    }
                }
            } else {
                val textView = TextView(this@CreateActivity)
                textView.text = getString(R.string.no_rights_to_announce)
                textView.gravity = Gravity.CENTER_HORIZONTAL

                createAnnouncementRoleChoose.addView(textView)
            }
        }
    }
}