package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ArchitectureScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SfxExplorerScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.VideoEditorViewModel
import com.example.ui.viewmodel.VideoSelectionViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VideoEditorViewModel by viewModels()
    private val selectionViewModel: VideoSelectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainAppScreen(
                    viewModel = viewModel,
                    selectionViewModel = selectionViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: VideoEditorViewModel,
    selectionViewModel: VideoSelectionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(ObsidianDark),
        containerColor = ObsidianDark,
        bottomBar = {
            NavigationBar(
                containerColor = StudioNavySurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(BorderHighlight, CyberCyan.copy(alpha = 0.3f))),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .testTag("main_bottom_nav_bar")
            ) {
                val items = listOf(
                    NavItem("الاستوديو", Icons.Default.MovieCreation, 0, "nav_tab_studio"),
                    NavItem("المشاريع", Icons.Default.Folder, 1, "nav_tab_projects"),
                    NavItem("المؤثرات", Icons.Default.GraphicEq, 2, "nav_tab_sfx"),
                    NavItem("البنية التحتية", Icons.Default.Hub, 3, "nav_tab_architecture")
                )

                items.forEach { item ->
                    val isSelected = uiState.currentTab == item.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(item.index) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) CyberCyan else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyberCyan else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = CyberCyan,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = SlateGlassVariant
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = uiState.currentTab, label = "tabTransition") { tab ->
                when (tab) {
                    0 -> StudioScreen(
                        viewModel = viewModel,
                        selectionViewModel = selectionViewModel
                    )
                    1 -> ProjectsScreen(viewModel = viewModel)
                    2 -> SfxExplorerScreen(viewModel = viewModel)
                    3 -> ArchitectureScreen(viewModel = viewModel)
                }
            }
        }
    }
}

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int,
    val testTag: String
)

