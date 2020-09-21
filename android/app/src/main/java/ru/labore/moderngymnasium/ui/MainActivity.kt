package ru.labore.moderngymnasium.ui

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.repository.AppRepository
import ru.labore.moderngymnasium.utils.hideKeyboard

class MainActivity : AppCompatActivity(), DIAware {
    override val di: DI by lazy { (applicationContext as DIAware).di }

    private lateinit var viewPagerAdapter: MainFragmentPagerAdapter
    private val repository: AppRepository by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (repository.user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, repository.user.toString(), Toast.LENGTH_SHORT).show()

            // Navigation Setup
            setSupportActionBar(toolbar)

            viewPagerAdapter = MainFragmentPagerAdapter(supportFragmentManager, lifecycle)
            navHostFragment.adapter = viewPagerAdapter

            bottomNav.setOnNavigationItemSelectedListener {
                loadFragment(it)
            }

            bottomNav.setOnNavigationItemReselectedListener {
                loadFragment(it)
            }
        }

        rootMainLayout.setOnClickListener {hideKeyboard()}
    }

    private fun loadFragment(menuItem: MenuItem): Boolean {
        for (i in 0..viewPagerAdapter.itemCount) {
            if (bottomNav.menu.getItem(i).itemId == menuItem.itemId) {
                navHostFragment.currentItem = i
                return true
            }
        }

        return false
    }
}