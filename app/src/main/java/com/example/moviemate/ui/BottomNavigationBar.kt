package com.example.moviemate.ui

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.moviemate.R
import com.example.moviemate.ui.theme.BackgroundColor
import com.example.moviemate.ui.theme.DarkColorPalette
import com.example.moviemate.ui.theme.TextWhiteColor
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Parabolic
import com.exyte.animatednavbar.animation.indendshape.Height
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius
import com.exyte.animatednavbar.utils.noRippleClickable

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navigationBarItems = remember {NavigationBarItems.values()}
    var selectedIndex by remember { mutableIntStateOf(0) }

    AnimatedNavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        selectedIndex = selectedIndex,
        cornerRadius = shapeCornerRadius(cornerRadius = 34.dp),
        ballAnimation = Parabolic(tween(300)),
        indentAnimation = Height(tween(300)),
        barColor = DarkColorPalette.background,
        ballColor = TextWhiteColor
    ) {
        navigationBarItems.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .noRippleClickable {
                        selectedIndex = index
                        when(item){
                            NavigationBarItems.Cinema -> navController.navigate("cinema"){
                                popUpTo("cinema") { inclusive = true}
                            }
                            NavigationBarItems.Theatre -> navController.navigate("theatre"){
                                popUpTo("cinema") { inclusive = false}
                            }
                            NavigationBarItems.Ticket -> navController.navigate("ticket"){
                                popUpTo("cinema") { inclusive = false}
                            }
                            NavigationBarItems.Profile -> navController.navigate("profile"){
                                popUpTo("cinema") { inclusive = false}
                            }
                        } },
                contentAlignment = Alignment.Center
            ){
                val painter: Painter = painterResource(id = item.icon)
                Image(
                    modifier = Modifier.size(26.dp),
                    painter = painter,
                    colorFilter = ColorFilter.tint(color = TextWhiteColor),
                    contentDescription = "Bottom bar icon",

                    //tint = if(selectedIndex == item.ordinal) MaterialTheme.colors.secondary
                    //else MaterialTheme.colors.primarySurface
                )
            }
        }
    }
}


enum class NavigationBarItems(@SuppressLint("SupportAnnotationUsage") @DrawableRes val icon: Int){
    Cinema(icon = R.drawable.icons8_movie_50),
    Theatre(icon = R.drawable.icons8_movie_theater_50),
    Ticket(icon =  R.drawable.icons8_movie_ticket_50),
    Profile(icon = R.drawable.icons8_admin_settings_male_48)
}

fun Modifier.noRippleClickable(onClick: ()-> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }){
        onClick()
    }
}




