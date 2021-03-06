package com.cmd.cmd_app_android.view.fragments.password_validation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.cmd.cmd_app_android.data.models.defaultUser
import com.cmd.cmd_app_android.view.activities.MainActivity
import com.cmd.cmd_app_android.view.utils.handleError
import com.cmd.cmd_app_android.view.utils.onChange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import thecmdteam.cmd_app_android.R
import thecmdteam.cmd_app_android.databinding.FragmentPasswordBinding

@AndroidEntryPoint
class PasswordFragment : Fragment(R.layout.fragment_password) {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PasswordValidationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPasswordBinding.bind(view)

        lifecycleScope.launchWhenStarted {
            viewModel.uiEvents.collect {
                when (it) {
                    is UiEvents.ChangedSuccessfully -> {
                        Intent(requireContext(), MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }.also {
                            requireContext().startActivity(it)
                        }
                    }
                }
            }
        }

        binding.passwordTextField.onChange {
            viewModel.execute(PasswordValidationEvents.ChangePasswordTextField(it))
        }
        binding.confirmNewPasswordTextField.onChange {
            viewModel.execute(PasswordValidationEvents.ChangeConfirmPasswordTextField(it))
        }

        binding.buttonContinue.setOnClickListener {
            viewModel.execute(PasswordValidationEvents.Continue)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.passwordValidationState.collect {
                if (it.loading) {
                    binding.loading(requireContext())
                }
                if (it.user != defaultUser && !it.loading) {
                    binding.success(requireContext())
                }
                if (it.error.isNotBlank() && !it.loading) {
                    binding.error(requireContext())
                }
                binding.apply {
                    newPasswordError.handleError(it.password.errorMessage, it.password.valid)
                    confirmNewPasswordError.handleError(
                        it.confirmPassword.errorMessage,
                        it.confirmPassword.valid
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

fun FragmentPasswordBinding.loading(context: Context) {
    this.apply {
        continueButtonText.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        buttonContinue.isClickable = false
        buttonContinue.background =
            AppCompatResources.getDrawable(context, R.drawable.background_auth_button_loading)
    }

}

fun FragmentPasswordBinding.success(context: Context) {
    this.apply {
        continueButtonText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        buttonContinue.isClickable = true
        buttonContinue.background =
            AppCompatResources.getDrawable(context, R.drawable.background_auth_button)
    }

}

fun FragmentPasswordBinding.error(context: Context) {
    this.apply {
        continueButtonText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        buttonContinue.isClickable = true
        buttonContinue.background =
            AppCompatResources.getDrawable(context, R.drawable.background_auth_button)
    }
}