package com.example.vocanote.features.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vocanote.core.designsystem.ContentMaxWidth
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.core.designsystem.component.InitialAvatar
import com.example.vocanote.core.designsystem.component.PageHeader
import com.example.vocanote.core.designsystem.component.StepControl
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.ui.UserProfile

@Composable
fun SettingsScreen(
    user: UserProfile,
    settings: ReviewSettings,
    isSaving: Boolean,
    appVersion: String,
    onUpdateSettings: (ReviewSettings) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().widthIn(max = ContentMaxWidth),
            contentPadding = PaddingValues(
                start = VocaSpacing.large,
                top = VocaSpacing.xLarge,
                end = VocaSpacing.large,
                bottom = VocaSpacing.xLarge
            ),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.xLarge)
        ) {
            item { PageHeader(title = "내 학습", subtitle = "목표와 복습 분량을 내 페이스에 맞춰보세요") }
            item { AccountPanel(user = user) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
                    Text(text = "학습 목표", style = MaterialTheme.typography.titleLarge)
                    SettingsPanel {
                        SettingRow(
                            icon = Icons.Default.Flag,
                            title = "하루 학습 목표",
                            description = "하루에 복습할 단어 수"
                        ) {
                            StepControl(
                                value = settings.dailyGoal,
                                valueLabel = "개",
                                canDecrease = !isSaving && settings.dailyGoal > ReviewSettings.MIN_DAILY_GOAL,
                                canIncrease = !isSaving && settings.dailyGoal < ReviewSettings.MAX_DAILY_GOAL,
                                onDecrease = { onUpdateSettings(settings.copy(dailyGoal = settings.dailyGoal - 5)) },
                                onIncrease = { onUpdateSettings(settings.copy(dailyGoal = settings.dailyGoal + 5)) }
                            )
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
                    Text(text = "연습 설정", style = MaterialTheme.typography.titleLarge)
                    SettingsPanel {
                        SettingRow(
                            icon = Icons.AutoMirrored.Filled.ViewList,
                            title = "한 세션 분량",
                            description = "연습 방식과 관계없이 한 번에 볼 단어 수"
                        ) {
                            StepControl(
                                value = settings.sessionSize,
                                valueLabel = "개",
                                canDecrease = !isSaving && settings.sessionSize > ReviewSettings.MIN_SESSION_SIZE,
                                canIncrease = !isSaving && settings.sessionSize < ReviewSettings.MAX_SESSION_SIZE,
                                onDecrease = {
                                    onUpdateSettings(settings.copy(sessionSize = settings.sessionSize - 5))
                                },
                                onIncrease = {
                                    onUpdateSettings(settings.copy(sessionSize = settings.sessionSize + 5))
                                }
                            )
                        }
                    }
                }
            }
            item {
                SettingsPanel {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "앱 버전", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = appVersion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "로그아웃",
                        modifier = Modifier.padding(start = VocaSpacing.small),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountPanel(user: UserProfile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(VocaSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(VocaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialAvatar(initial = user.initial)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.email.isNotBlank()) {
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(VocaSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.large)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    description: String,
    control: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VocaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(9.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            control()
        }
    }
}
