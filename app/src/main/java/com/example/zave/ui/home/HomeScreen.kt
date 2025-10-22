package com.example.zave.ui.home

import android.Manifest
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.zave.ui.common.navigation.Screen
import com.example.zave.data.repository.RemoteCategory
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import com.example.zave.ui.theme.AccentBlue
import com.example.zave.ui.theme.AccentPink
import com.example.zave.ui.theme.DarkBackground
import com.example.zave.ui.theme.DarkBlueToBlackGradient
import com.example.zave.ui.theme.TextPrimary



// Category Data Model - Kept as is for the UI layer
data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

// Extension function to safely convert a hex string to a Compose Color
fun String?.toComposeColor(default: Color): Color {
    return try {
        this?.removePrefix("0x")?.removePrefix("0X")?.toLong(16)?.let { colorLong ->
            // If the parsed long is an RGB value (less than 0x1000000), prepend 0xFF for full opacity.
            val finalColorLong = if (colorLong < 0x1000000) (colorLong or 0xFF000000) else colorLong
            Color(finalColorLong)
        } ?: default
    } catch (e: Exception) {
        default
    }
}

// Helper to map a string key to a Material Icon ImageVector
fun mapKeyToImageVector(key: String): ImageVector {
    return when (key) {
        "Laptop" -> Icons.Default.Laptop
        "Smartphone" -> Icons.Default.Smartphone
        "Checkroom" -> Icons.Default.Checkroom
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Home" -> Icons.Default.Home
        "MenuBook" -> Icons.Default.MenuBook
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Face" -> Icons.Default.Face
        "LocalOffer" -> Icons.Default.LocalOffer
        else -> Icons.Default.Category // Default icon
    }
}

// Helper function to convert the RemoteCategory to the UI's CategoryItem
fun RemoteCategory.toCategoryItem(): CategoryItem {
    return CategoryItem(
        name = this.name,
        icon = mapKeyToImageVector(this.key),
        color = this.color.toComposeColor(AccentBlue)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val queryInput by viewModel.queryInput.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            viewModel.requestLocation()
        } else {
            viewModel.setPermissionRequired(true)
        }
    }

    LaunchedEffect(uiState.requiresLocationPermission) {
        if (!uiState.currentLocation.isReady() && !uiState.requiresLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val onSearch = {
        val query = viewModel.getEffectiveSearchQuery()
        focusManager.clearFocus()
        if (uiState.currentLocation.isReady()) {
            navController.navigate(Screen.Results.createRoute(query))
        } else {
            viewModel.requestLocation()
            Toast.makeText(context, "Location required", Toast.LENGTH_SHORT).show()
        }
    }

    // Setup Pull-to-Refresh State
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = viewModel::refreshData
    )

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Zave",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBlueToBlackGradient)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar Section
                SearchSection(
                    queryInput = queryInput,
                    onQueryChange = viewModel::updateQueryInput,
                    onSearch = onSearch,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Location Status
                if (!uiState.currentLocation.isReady() || uiState.requiresLocationPermission) {
                    LocationStatusCard(
                        uiState = uiState,
                        onPermissionRequest = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Error Message
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = AccentPink,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // NEARBY OFFERS CAROUSEL
                if (uiState.nearbyOffers.isNotEmpty()) {
                    OffersCarousel(
                        offers = uiState.nearbyOffers,
                        onOfferClick = { query ->
                            viewModel.updateQueryInput(query)
                            onSearch()
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }


                // Recent Searches (Will be empty initially)
                if (uiState.recentSearches.isNotEmpty()) {
                    RecentSearchesSection(
                        searches = uiState.recentSearches,
                        onSearchClick = { query ->
                            viewModel.updateQueryInput(query)
                            onSearch()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Featured Category Banner (Will be empty initially if config not loaded)
                if (uiState.featuredCategory.isNotEmpty()) {
                    FeaturedCategoryBanner(
                        category = uiState.featuredCategory,
                        onClick = {
                            viewModel.updateQueryInput(uiState.featuredCategory)
                            onSearch()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Category Cards Section (Will be empty initially)
                CategoryCardsSection(
                    remoteCategories = uiState.categoryItems,
                    onCategoryClick = { category ->
                        viewModel.updateQueryInput(category)
                        onSearch()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Banner Message (Will be empty initially)
                if (uiState.bannerMessage.isNotEmpty()) {
                    BannerCard(
                        message = uiState.bannerMessage,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Pull-to-Refresh Indicator is overlaid on top
            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = AccentBlue
            )
        }
    }
}
// Extension function
fun Location?.isReady(): Boolean {
    return this != null && (this.latitude != 0.0 || this.longitude != 0.0)
}
