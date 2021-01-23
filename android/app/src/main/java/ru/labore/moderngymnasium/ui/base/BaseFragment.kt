package ru.labore.moderngymnasium.ui.base

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.network.ClientConnectionException
import ru.labore.moderngymnasium.data.network.ClientErrorException
import ru.labore.moderngymnasium.data.repository.AppRepository
import ru.labore.moderngymnasium.ui.activities.LoginActivity
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment : Fragment(), CoroutineScope {
    protected abstract val viewModel: BaseViewModel
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }

    protected fun makeRequest(
        toTry: suspend () -> Unit,
        whenCaught: suspend () -> Unit = {}
    ) = launch {
        try {
            toTry()
        } catch(e: Exception) {
            Toast.makeText(
                activity,
                when (e) {
                    is ConnectException -> getString(R.string.server_unavailable)
                    is ClientConnectionException -> getString(R.string.no_internet)
                    is ClientErrorException -> {
                        if (e.errorCode == AppRepository.HTTP_RESPONSE_CODE_UNAUTHORIZED) {
                            viewModel.cleanseUser()

                            startActivity(
                                Intent(
                                    activity,
                                    LoginActivity::class.java
                                )
                            )
                            activity?.finish()

                            getString(R.string.invalid_credentials)
                        } else {
                            println(e.toString())
                            "An unknown error occurred"
                        }
                    }
                    else -> {
                        println(e.toString())
                        "An unknown error occurred."
                    }
                         },
                Toast.LENGTH_LONG
            ).show()

            whenCaught()
        }
    }
}