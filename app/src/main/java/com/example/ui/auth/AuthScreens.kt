package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.ZaynUiState
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.ZaynLiveRed

@Composable
fun LoginScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(theme.card)
                .border(1.dp, theme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = theme.primary, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("SIGN IN TO ZAYNTV", color = theme.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Access synchronized channels and preferences", color = theme.textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(22.dp))

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("Username, Email or Phone", color = theme.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = theme.primary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = theme.primary.copy(alpha = 0.2f),
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = theme.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primary) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = theme.textSecondary)
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = theme.primary.copy(alpha = 0.2f),
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = theme.primary)
                        )
                        Text("Remember me", color = theme.textPrimary, fontSize = 13.sp)
                    }

                    TextButton(onClick = { viewModel.showToast("Password reset link sent if registered") }) {
                        Text("Forgot password?", color = theme.primary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { viewModel.login(identifier, password, rememberMe) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("SIGN IN", color = theme.background, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account?", color = theme.textSecondary, fontSize = 13.sp)
                    TextButton(onClick = { viewModel.navigateTo(CurrentScreen.SIGN_UP) }) {
                        Text("Create Account", color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                TextButton(onClick = { viewModel.navigateTo(CurrentScreen.HOME) }) {
                    Text("Continue as Guest", color = theme.textSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    viewModel: ZaynTvViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(theme.card)
                .border(1.dp, theme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CREATE ZAYNTV ACCOUNT", color = theme.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = theme.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, unfocusedBorderColor = theme.primary.copy(alpha = 0.2f), focusedTextColor = theme.textPrimary, unfocusedTextColor = theme.textPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = theme.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, unfocusedBorderColor = theme.primary.copy(alpha = 0.2f), focusedTextColor = theme.textPrimary, unfocusedTextColor = theme.textPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)", color = theme.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, unfocusedBorderColor = theme.primary.copy(alpha = 0.2f), focusedTextColor = theme.textPrimary, unfocusedTextColor = theme.textPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number (Optional)", color = theme.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, unfocusedBorderColor = theme.primary.copy(alpha = 0.2f), focusedTextColor = theme.textPrimary, unfocusedTextColor = theme.textPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = theme.textSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, unfocusedBorderColor = theme.primary.copy(alpha = 0.2f), focusedTextColor = theme.textPrimary, unfocusedTextColor = theme.textPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.signUp(name, username, email, phone, password) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("REGISTER", color = theme.background, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Already registered?", color = theme.textSecondary, fontSize = 13.sp)
                    TextButton(onClick = { viewModel.navigateTo(CurrentScreen.LOGIN) }) {
                        Text("Sign In", color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    val profile = uiState.userProfile

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(profile.name) }
    var editEmail by remember { mutableStateOf(profile.email) }
    var editPhone by remember { mutableStateOf(profile.phone) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { viewModel.navigateTo(CurrentScreen.HOME) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("USER PROFILE", color = theme.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(theme.primary.copy(alpha = 0.2f))
                        .border(2.dp, theme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = theme.primary, modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(profile.name, color = theme.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("@${profile.username}", color = theme.primary, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.card)
                        .border(1.dp, theme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(22.dp)
                ) {
                    if (!isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ProfileInfoItem(label = "Email", value = profile.email.ifBlank { "Not configured" }, icon = Icons.Default.Email)
                            ProfileInfoItem(label = "Mobile", value = profile.phone.ifBlank { "Not configured" }, icon = Icons.Default.Phone)
                            ProfileInfoItem(label = "Status", value = if (profile.isLoggedIn) "Logged In" else "Guest Mode", icon = Icons.Default.AccountCircle)

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { isEditing = true },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("EDIT PROFILE", color = theme.background, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Name", color = theme.textSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, unfocusedBorderColor = theme.primary.copy(alpha = 0.2f), focusedTextColor = theme.textPrimary, unfocusedTextColor = theme.textPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = { Text("Email", color = theme.textSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, unfocusedBorderColor = theme.primary.copy(alpha = 0.2f), focusedTextColor = theme.textPrimary, unfocusedTextColor = theme.textPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("Phone", color = theme.textSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, unfocusedBorderColor = theme.primary.copy(alpha = 0.2f), focusedTextColor = theme.textPrimary, unfocusedTextColor = theme.textPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { isEditing = false }) { Text("CANCEL", color = theme.textPrimary) }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateProfile(editName, editEmail, editPhone)
                                        isEditing = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                                ) {
                                    Text("SAVE", color = theme.background, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (profile.isLoggedIn) {
                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ZaynLiveRed),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ZaynLiveRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SIGN OUT", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.navigateTo(CurrentScreen.LOGIN) },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Text("SIGN IN TO AN ACCOUNT", color = theme.background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val theme = LocalAppThemeColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = theme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = theme.textSecondary, fontSize = 11.sp)
            Text(value, color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
