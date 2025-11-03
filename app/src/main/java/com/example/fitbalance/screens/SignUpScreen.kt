package com.example.fitbalance.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fitbalance.R
import com.example.fitbalance.components.EditOutlineSpinner
import com.example.fitbalance.components.EditOutlineTextField
import com.example.fitbalance.components.ValidationErrorText
import com.example.fitbalance.navigation.FitBalanceScreens
import com.example.fitbalance.viewmodels.SignUpScreenViewModel

@Composable
fun SignUpScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SignUpScreenViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.light_green),
                        colorResource(id = R.color.green)
                    )
                )
            )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.sign_up).uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = modifier.weight(1f)) {
                    EditOutlineTextField(
                        modifier = modifier.fillMaxWidth(),
                        text = viewModel.inputName,
                        onValueChange = viewModel::updateName,
                        label = stringResource(R.string.name),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        isError = uiState.validationErrors.nameError != null
                    )
                    uiState.validationErrors.nameError?.let {
                        ValidationErrorText(it)
                    }
                }

                Column(modifier = modifier.weight(1f)) {
                    EditOutlineTextField(
                        modifier = modifier.fillMaxWidth(),
                        text = viewModel.inputSurname,
                        onValueChange = viewModel::updateSurname,
                        label = stringResource(R.string.surname),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        isError = uiState.validationErrors.surnameError != null
                    )
                    uiState.validationErrors.surnameError?.let {
                        ValidationErrorText(it)
                    }
                }
            }

            Column(modifier = modifier
                .fillMaxWidth()
                .padding(top = 12.dp)) {
                EditOutlineTextField(
                    modifier = modifier.fillMaxWidth(),
                    text = viewModel.inputMail,
                    onValueChange = viewModel::updateMail,
                    label = stringResource(R.string.mail),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = uiState.validationErrors.emailError != null
                )
                uiState.validationErrors.emailError?.let {
                    ValidationErrorText(it)
                }
            }

            Column(modifier = modifier
                .fillMaxWidth()
                .padding(top = 12.dp)) {
                EditOutlineTextField(
                    modifier = modifier.fillMaxWidth(),
                    text = viewModel.inputPassword,
                    onValueChange = viewModel::updatePassword,
                    label = stringResource(R.string.password),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true,
                    isError = uiState.validationErrors.passwordError != null
                )
                uiState.validationErrors.passwordError?.let {
                    ValidationErrorText(it)
                }
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = modifier.weight(1f)) {
                    EditOutlineTextField(
                        modifier = modifier.fillMaxWidth(),
                        text = viewModel.inputHeight,
                        onValueChange = viewModel::updateHeight,
                        label = stringResource(R.string.boy),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.validationErrors.heightError != null
                    )
                    uiState.validationErrors.heightError?.let {
                        ValidationErrorText(it)
                    }
                }

                Column(modifier = modifier.weight(1f)) {
                    EditOutlineTextField(
                        modifier = modifier.fillMaxWidth(),
                        text = viewModel.inputWeight,
                        onValueChange = viewModel::updateWeight,
                        label = stringResource(R.string.kilo),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.validationErrors.weightError != null
                    )
                    uiState.validationErrors.weightError?.let {
                        ValidationErrorText(it)
                    }
                }

                Column(modifier = modifier.weight(1f)) {
                    EditOutlineTextField(
                        modifier = modifier.fillMaxWidth(),
                        text = viewModel.inputAge,
                        onValueChange = viewModel::updateAge,
                        label = stringResource(R.string.yas),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.validationErrors.ageError != null
                    )
                    uiState.validationErrors.ageError?.let {
                        ValidationErrorText(it)
                    }
                }
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = modifier.weight(1f)) {
                    EditOutlineSpinner(
                        modifier = modifier.fillMaxWidth(),
                        label = stringResource(R.string.gender_label),
                        options = listOf(
                            stringResource(R.string.gender_female),
                            stringResource(R.string.gender_male)
                        ),
                        selectedOption = viewModel.inputGender,
                        onOptionSelected = viewModel::updateGender,
                        isError = uiState.validationErrors.genderError != null
                    )
                    uiState.validationErrors.genderError?.let {
                        ValidationErrorText(it)
                    }
                }

                Column(modifier = modifier.weight(1f)) {
                    EditOutlineSpinner(
                        modifier = modifier.fillMaxWidth(),
                        label = stringResource(R.string.goal_label),
                        options = listOf(
                            stringResource(R.string.goal_gain),
                            stringResource(R.string.goal_keep),
                            stringResource(R.string.goal_lose)
                        ),
                        selectedOption = viewModel.inputGoal,
                        onOptionSelected = viewModel::updateGoal,
                        isError = uiState.validationErrors.goalError != null
                    )
                    uiState.validationErrors.goalError?.let {
                        ValidationErrorText(it)
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.signUp {
                        navController.navigate(FitBalanceScreens.SignInScreen.route) {
                            popUpTo(FitBalanceScreens.SignUpScreen.route) { inclusive = true }
                        }
                    }
                },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = colorResource(id = R.color.green)
                ),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = stringResource(R.string.sign_up),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    modifier = modifier.padding(vertical = 8.dp)
                )
            }

            ElevatedButton(
                onClick = {
                    navController.navigate(FitBalanceScreens.SignInScreen.route) {
                        popUpTo(FitBalanceScreens.SignUpScreen.route) { inclusive = true }
                    }
                },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    modifier = modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    val navController = rememberNavController()
    SignUpScreen(navController = navController)
}