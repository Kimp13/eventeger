package ru.labore.moderngymnasium.ui.fragments.create

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.fragment_create.*
import kotlinx.coroutines.launch
import org.threeten.bp.ZonedDateTime
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.ui.base.ListElementFragment
import ru.labore.moderngymnasium.ui.fragments.DatePickerFragment
import ru.labore.moderngymnasium.ui.fragments.TimePickerFragment
import ru.labore.moderngymnasium.ui.views.LabelledCheckbox
import ru.labore.moderngymnasium.ui.views.ParentCheckbox
import ru.labore.moderngymnasium.utils.hideKeyboard

class CreateFragment(
    controls: Companion.ListElementFragmentControls
) : ListElementFragment(controls) {
    override val viewModel: CreateViewModel by viewModels()
    private val startDatePicker: DatePickerFragment
    private val startTimePicker: TimePickerFragment
    private val endDatePicker: DatePickerFragment
    private val endTimePicker: TimePickerFragment
    private var startDateTime: ZonedDateTime = viewModel.appRepository.zonedNow()
    private var endDateTime: ZonedDateTime = startDateTime.plusHours(1)

    init {
        startDatePicker = DatePickerFragment { year, month, day ->
            onStartDateChanged(year, month, day)
        }

        startTimePicker = TimePickerFragment { hour, minute ->
            onStartTimeChanged(hour, minute)
        }

        endDatePicker = DatePickerFragment { year, month, day ->
            onEndDateChanged(year, month, day)
        }

        endTimePicker = TimePickerFragment { hour, minute ->
            onEndTimeChanged(hour, minute)
        }

        (startDatePicker.dialog as DatePickerDialog).datePicker.minDate =
            startDateTime.toEpochSecond() * 1000

        (endDatePicker.dialog as DatePickerDialog).datePicker.minDate =
            endDateTime.toEpochSecond() * 1000
    }

    private fun verifyDateTimes() {
        if (startDateTime.isBefore(viewModel.appRepository.zonedNow()))
            startDateTime = viewModel.appRepository.zonedNow()

        if (startDateTime.isAfter(endDateTime))
            endDateTime = startDateTime.plusHours(1)
    }

    private fun onStartDateChanged(year: Int, month: Int, day: Int) {
        startDateTime = startDateTime
            .withYear(year)
            .withMonth(month)
            .withDayOfMonth(day)

        verifyDateTimes()
    }

    private fun onStartTimeChanged(hour: Int, minute: Int) {
        startDateTime = startDateTime
            .withHour(hour)
            .withMinute(minute)

        verifyDateTimes()
    }

    private fun onEndDateChanged(year: Int, month: Int, day: Int) {
        endDateTime = endDateTime
            .withYear(year)
            .withMonth(month)
            .withDayOfMonth(day)

        verifyDateTimes()
    }

    private fun onEndTimeChanged(hour: Int, minute: Int) {
        endDateTime = endDateTime
            .withHour(hour)
            .withMinute(minute)

        verifyDateTimes()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createFragmentToolbar.inflateMenu(R.menu.create_toolbar_menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val progressBar = ProgressBar(activity)
        progressBar.isIndeterminate = true

        launch {
            loadUI()

            createRecipientsLoading?.visibility = View.GONE
            createRecipientsProgressBar?.visibility = View.GONE
        }

        createFragmentToolbar.setOnMenuItemClickListener {
            createFragmentToolbar.menu[0].actionView = progressBar

            createAnnouncement {
                createFragmentToolbar.menu.clear()
                createFragmentToolbar.inflateMenu(R.menu.create_toolbar_menu)
            }

            true
        }

        createAnnouncementBackButton?.setOnClickListener {
            controls.finish()
        }

        createFragmentScrollView
            .viewTreeObserver
            .addOnScrollChangedListener {
                val scrollView = createFragmentScrollView

                if (scrollView != null) {
                    val difference = scrollView.children.last().bottom -
                            scrollView.height - scrollView.scrollY

                    if (difference <= 50)
                        controls.hideBottomNav()
                    else
                        controls.showBottomNav()
                }
            }

        createFragmentScrollView?.setOnClickListener { hideKeyboard() }
        createFragmentParametersLayout?.setOnClickListener { hideKeyboard() }
        createFragmentParametersLayout?.children?.forEach {
            it.setOnClickListener { hideKeyboard() }
        }

        createStartDate?.setOnClickListener {
        }

        createStartTime?.setOnClickListener {

        }

        createEndDate?.setOnClickListener {

        }

        createEndTime?.setOnClickListener {

        }
    }

    private fun createAnnouncement(
        afterAll: () -> Unit
    ) {
        if (viewModel.checkedRoles.keys.size == 0) {
            Toast.makeText(
                activity,
                getString(R.string.choose_recipients),
                Toast.LENGTH_SHORT
            ).show()

            afterAll()
        } else {
            val text = createAnnouncementEditText
                .text
                .toString()
                .trim()

            if (text.isEmpty()) {
                Toast.makeText(
                    activity,
                    getString(R.string.enter_announcement_text),
                    Toast.LENGTH_SHORT
                ).show()

                afterAll()
            } else makeRequest({
                viewModel.createAnnouncement(text)

                createAnnouncementEditText.setText("")

                afterAll()
            }, {
                afterAll()
            })
        }
    }

    private fun childCheckedChangeHandler(isChecked: Boolean, roleId: Int, classId: Int) {
        if (!viewModel.checkedRoles.containsKey(roleId))
            viewModel.checkedRoles[roleId] = hashSetOf()

        if (isChecked)
            viewModel.checkedRoles[roleId]!!.add(classId)
        else
            viewModel.checkedRoles[roleId]!!.remove(classId)
    }

    private suspend fun loadUI() {
        val act = activity

        if (act != null) {
            val roles = viewModel.getRoles(act)
            val classes = viewModel.getClasses(act)

            viewModel.appRepository.announceMap.entries.forEach { roleMap ->
                val role = roles[roleMap.key]
                val checkboxLayout: View
                val context = context

                if (role != null && context != null) {
                    if (roleMap.value.size == 1) {
                        val onlyClass = classes[roleMap.value[0]]!!

                        checkboxLayout = LabelledCheckbox(
                            context,
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
                            context,
                            "Роль: ${role.name}"
                        )

                        gradedClasses.entries.forEach {
                            val childCheckbox: View

                            if (it.value.size == 1) {
                                val onlyClass = classes[it.value[0]]!!

                                childCheckbox = LabelledCheckbox(
                                    context,
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
                                    context,
                                    "${it.key}-я параллель"
                                )

                                it.value.forEach { classId ->
                                    val leafCheckbox = LabelledCheckbox(
                                        context,
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
}