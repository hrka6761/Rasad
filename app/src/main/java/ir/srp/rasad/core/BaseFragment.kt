package ir.srp.rasad.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

open class BaseFragment : Fragment() {

    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = NavHostFragment.findNavController(this)
        setOnBackPressedCallback { onBackPressed() }
    }


    open fun onBackPressed() {
        requireActivity().finish()
    }


    private fun setOnBackPressedCallback(callback: () -> Unit) {
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            OnBackPressedCallback(callback)
        )
    }


    private class OnBackPressedCallback(private val callback: () -> Unit) :
        androidx.activity.OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            callback()
        }
    }
}