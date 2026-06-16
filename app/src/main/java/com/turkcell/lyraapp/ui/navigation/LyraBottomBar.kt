package com.turkcell.lyraapp.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.turkcell.lyraapp.ui.icons.LyraIcons

enum class LyraBottomBarTab(
    val destination: LyraDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector

){
    Home(LyraDestination.Home,"Ana sayfa", LyraIcons.Home,LyraIcons.HomeOutlined),
    Search(LyraDestination.Search,"Ara", LyraIcons.Search, LyraIcons.Search),
    Library(LyraDestination.Library, "Kütüphane",LyraIcons.LibraryMusic, LyraIcons.LibraryMusicOutlined),
    Favorites(LyraDestination.Favorites,"Favoriler", LyraIcons.Favorite, LyraIcons.FavoriteOutlined),
    Profile(LyraDestination.Profile,"Profil", LyraIcons.Person, LyraIcons.PersonOutlined),
}

fun isTopLevelRoute(route: String?): Boolean =
    LyraBottomBarTab.entries.any{ it.destination.route == route}


@Composable
fun LyraBottomBar(
    currentRoute: String?,
    onTabSelected: (LyraDestination) -> Unit,
    modifier: Modifier = Modifier,
){
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        LyraBottomBarTab.entries.forEach { tab ->
            val selected = currentRoute == tab.destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab.destination)},
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else
                        tab.unselectedIcon,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}