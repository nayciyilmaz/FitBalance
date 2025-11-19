package com.example.fitbalance.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.fitbalance.components.EditScaffold
import com.example.fitbalance.components.SettingsInputField
import com.example.fitbalance.components.ValidationErrorText
import com.example.fitbalance.components.EditButtons
import com.example.fitbalance.navigation.FitBalanceScreens
import com.example.fitbalance.viewmodels.SettingsScreenViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    EditScaffold(
        navController = navController,
        title = stringResource(R.string.ayarlar),
        showBottomBar = false
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ExpandableCard(
                        title = stringResource(R.string.personal_info),
                        isExpanded = viewModel.personalInfoExpanded,
                        onExpandChange = { viewModel.togglePersonalInfoExpanded() }
                    ) {
                        SettingsInputField(
                            value = viewModel.inputName,
                            onValueChange = { viewModel.updateName(it) },
                            label = stringResource(R.string.name),
                            keyboardType = KeyboardType.Text,
                            isError = viewModel.nameError != null
                        )
                        viewModel.nameError?.let {
                            ValidationErrorText(error = it)
                        }

                        SettingsInputField(
                            value = viewModel.inputSurname,
                            onValueChange = { viewModel.updateSurname(it) },
                            label = stringResource(R.string.surname),
                            keyboardType = KeyboardType.Text,
                            isError = viewModel.surnameError != null
                        )
                        viewModel.surnameError?.let {
                            ValidationErrorText(error = it)
                        }

                        SettingsInputField(
                            value = viewModel.inputAge,
                            onValueChange = { viewModel.updateAge(it) },
                            label = stringResource(R.string.yas),
                            keyboardType = KeyboardType.Number,
                            isError = viewModel.ageError != null
                        )
                        viewModel.ageError?.let {
                            ValidationErrorText(error = it)
                        }
                    }

                    ExpandableCard(
                        title = stringResource(R.string.physical_info),
                        isExpanded = viewModel.physicalInfoExpanded,
                        onExpandChange = { viewModel.togglePhysicalInfoExpanded() }
                    ) {
                        Row(
                            modifier = modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = modifier.weight(1f)) {
                                SettingsInputField(
                                    value = viewModel.inputHeight,
                                    onValueChange = { viewModel.updateHeight(it) },
                                    label = stringResource(R.string.boy),
                                    keyboardType = KeyboardType.Number,
                                    isError = viewModel.heightError != null,
                                    modifier = Modifier
                                )
                                viewModel.heightError?.let {
                                    ValidationErrorText(error = it)
                                }
                            }

                            Column(modifier = modifier.weight(1f)) {
                                SettingsInputField(
                                    value = viewModel.inputWeight,
                                    onValueChange = { viewModel.updateWeight(it) },
                                    label = stringResource(R.string.kilo),
                                    keyboardType = KeyboardType.Number,
                                    isError = viewModel.weightError != null,
                                    modifier = Modifier
                                )
                                viewModel.weightError?.let {
                                    ValidationErrorText(error = it)
                                }
                            }
                        }

                        EditOutlineSpinner(
                            label = stringResource(R.string.goal_label),
                            options = listOf(
                                stringResource(R.string.goal_gain),
                                stringResource(R.string.goal_keep),
                                stringResource(R.string.goal_lose)
                            ),
                            selectedOption = viewModel.inputGoal,
                            onOptionSelected = { viewModel.updateGoal(it) },
                            modifier = modifier.fillMaxWidth(),
                            isError = viewModel.goalError != null
                        )
                        viewModel.goalError?.let {
                            ValidationErrorText(error = it)
                        }
                    }

                    ExpandableCard(
                        title = stringResource(R.string.change_password),
                        isExpanded = viewModel.passwordExpanded,
                        onExpandChange = { viewModel.togglePasswordExpanded() }
                    ) {
                        SettingsInputField(
                            value = viewModel.inputCurrentPassword,
                            onValueChange = { viewModel.updateCurrentPassword(it) },
                            label = stringResource(R.string.current_password),
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            passwordVisible = viewModel.currentPasswordVisible,
                            onVisibilityChange = { viewModel.toggleCurrentPasswordVisibility() },
                            isError = viewModel.currentPasswordError != null
                        )
                        viewModel.currentPasswordError?.let {
                            ValidationErrorText(error = it)
                        }

                        SettingsInputField(
                            value = viewModel.inputNewPassword,
                            onValueChange = { viewModel.updateNewPassword(it) },
                            label = stringResource(R.string.new_password),
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            passwordVisible = viewModel.newPasswordVisible,
                            onVisibilityChange = { viewModel.toggleNewPasswordVisibility() },
                            isError = viewModel.newPasswordError != null
                        )
                        viewModel.newPasswordError?.let {
                            ValidationErrorText(error = it)
                        }

                        SettingsInputField(
                            value = viewModel.inputNewPasswordConfirm,
                            onValueChange = { viewModel.updateNewPasswordConfirm(it) },
                            label = stringResource(R.string.new_password_confirm),
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            passwordVisible = viewModel.confirmPasswordVisible,
                            onVisibilityChange = { viewModel.toggleConfirmPasswordVisibility() },
                            isError = viewModel.newPasswordConfirmError != null
                        )
                        viewModel.newPasswordConfirmError?.let {
                            ValidationErrorText(error = it)
                        }
                    }
                }

                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditButtons(
                        text = stringResource(R.string.geri),
                        onClick = { navController.popBackStack() },
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color(0xFF666666)
                    )

                    EditButtons(
                        text = stringResource(R.string.save_changes),
                        onClick = {
                            viewModel.saveChanges {
                                navController.navigate(FitBalanceScreens.ProfileScreen.route) {
                                    popUpTo(FitBalanceScreens.SettingsScreen.route) { inclusive = true }
                                }
                            }
                        },
                        enabled = !viewModel.isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableCard(
    title: String,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.light_green)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = colorResource(R.color.dark_green)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colorResource(R.color.dark_green)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    val navController = rememberNavController()
    SettingsScreen(navController = navController)
}