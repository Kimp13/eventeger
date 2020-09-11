package ru.labore.moderngymnasium.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.user.UserCredentials
import ru.labore.moderngymnasium.data.user.UserSignIn
import ru.labore.moderngymnasium.utils.hideKeyboard

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val apiService = UserSignIn(this)

        GlobalScope.launch(Dispatchers.Main) {
            val user = apiService
                .signIn(
                    UserCredentials(
                        "Kimp13",
                        "Iampinkdog!23"
                    )
                )

            println(user.toString())
        }

        rootLayout.setOnClickListener {hideKeyboard()}
    }
}