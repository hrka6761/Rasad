package ir.srp.rasad.presentation.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.databinding.FragmentLoginBinding

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: RegisterViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }
}