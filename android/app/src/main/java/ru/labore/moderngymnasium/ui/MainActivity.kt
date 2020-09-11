package ru.labore.moderngymnasium.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import ru.labore.moderngymnasium.R

class MainActivity : AppCompatActivity() {
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user = getSharedPreferences(
            getString(R.string.utility_shared_preference_file_key),
            Context.MODE_PRIVATE
        ).getString("user", null)

        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        navController = Navigation.findNavController(this, R.id.navHostFragment)

        bottomNav.setupWithNavController(navController)

        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}