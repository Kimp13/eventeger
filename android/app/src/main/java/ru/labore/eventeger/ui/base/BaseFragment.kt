package ru.labore.eventeger.ui.base

import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import ru.labore.eventeger.R
import ru.labore.eventeger.data.AppRepository
import ru.labore.eventeger.data.network.exceptions.ClientConnectionException
import ru.labore.eventeger.data.network.exceptions.ClientErrorException
import ru.labore.eventeger.ui.activities.LoginActivity
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseFragment : Fragment() {
    protected abstract val viewModel: BaseViewModel

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = viewLifecycleOwner.lifecycleScope.launch(context, start, block)

    protected fun makeRequest(
        toTry: suspend () -> Unit,
        whenCaught: suspend () -> Unit = {}
    ) = launch {
        try {
            toTry()
        } catch(e: Exception) {
            whenCaught()

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
        }
    }
}