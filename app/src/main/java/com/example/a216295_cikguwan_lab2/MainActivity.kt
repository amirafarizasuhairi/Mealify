package com.example.a216295_cikguwan_lab2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// --- THEME COLORS ---
val MealifyLightPurple = Color(0xFFE1BEE7)
val MealifyDarkPurple = Color(0xFFB02CAC)
// Green removed as requested to keep everything Purple
val MealifyHeaderGradient = Brush.verticalGradient(
    listOf(Color(0XFFC27EC0), Color(0xFFCC3DC8))
)

// --- NAVIGATION STATE ---
enum class AppScreen { HOME, CATEGORY, STORE, ACCOUNT, PARCELS, ORDERS, CART }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppNavigation()
        }
    }
}

@Composable
fun MainAppNavigation() {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var selectedItemName by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onTabSelected = { currentScreen = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreenUI(
                    onCategoryClick = { name ->
                        selectedItemName = name
                        currentScreen = AppScreen.CATEGORY
                    },
                    onStoreClick = { name ->
                        selectedItemName = name
                        currentScreen = AppScreen.STORE
                    }
                )
                AppScreen.CATEGORY -> DetailPage(selectedItemName) { currentScreen = AppScreen.HOME }
                AppScreen.STORE -> DetailPage(selectedItemName) { currentScreen = AppScreen.HOME }

                AppScreen.PARCELS -> ParcelsScreen()
                AppScreen.ORDERS -> OrdersScreen()
                AppScreen.CART -> CartScreen()
                AppScreen.ACCOUNT -> AccountScreen()
            }
        }
    }
}

// --- NAVIGATION SCREENS (UPDATED TO PURPLE & MEALIFY TEXT) ---

@Composable
fun ParcelsScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().background(MealifyDarkPurple).padding(20.dp), contentAlignment = Alignment.Center) {
            Text("Parcels", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(100.dp), tint = MealifyDarkPurple)
            Spacer(Modifier.height(16.dp))
            Text("Coming Soon", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            // Text changed from ReMeal to MEALIFY
            Text("Get a MEALIFY parcel - full of goodies delivered to your door!", textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
        }
    }
}

@Composable
fun CartScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Box(modifier = Modifier.fillMaxWidth().background(MealifyDarkPurple).padding(20.dp), contentAlignment = Alignment.Center) {
            Text("My Cart", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cafe Chef Wan @ TTDI", fontWeight = FontWeight.Bold)
            Text("Level 2, TT Garden, Bangi", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("Sold Out", fontSize = 10.sp, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Chicken Mushroom Pie", fontWeight = FontWeight.Bold)
                        // Color changed to Purple
                        Text("RM 13.36", color = MealifyDarkPurple)
                    }
                    Text("0", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Proceed to Checkout")
        }
    }
}

@Composable
fun OrdersScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().background(MealifyDarkPurple).padding(20.dp), contentAlignment = Alignment.Center) {
            Text("My Orders", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            // Color changed to Purple
            Text("Active Orders", modifier = Modifier.weight(1f).padding(16.dp), textAlign = TextAlign.Center, color = MealifyDarkPurple, fontWeight = FontWeight.Bold)
            Text("Past Orders", modifier = Modifier.weight(1f).padding(16.dp), textAlign = TextAlign.Center, color = Color.Gray)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Color changed to Purple
            Text("No orders found", color = MealifyDarkPurple)
        }
    }
}

@Composable
fun AccountScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9FBF9))) {
        Box(modifier = Modifier.fillMaxWidth().background(MealifyDarkPurple).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.3f)) {
                    Icon(Icons.Default.Person, null, tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("amira fariza", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    // Text changed to MEALIFYer
                    Text("MEALIFYer # (Verify phone)", color = Color.White, fontSize = 12.sp)
                }
            }
        }
        val options = listOf("Language", "Profile", "Saved Addresses", "Coupons", "Referrals", "Favourites")
        Column(modifier = Modifier.padding(16.dp).background(Color.White, RoundedCornerShape(12.dp))) {
            options.forEach { option ->
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(option)
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.LightGray)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            }
        }
    }
}

// --- HOME SCREEN (UNCHANGED) ---

@Composable
fun HomeScreenUI(onCategoryClick: (String) -> Unit, onStoreClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var searchStatus by remember { mutableStateOf("Discover delicious meals near you!") }
    var expanded by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("Kolej Pendeta Za'ba") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Column(modifier = Modifier.background(MealifyHeaderGradient).padding(16.dp)) {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                    ) {
                        Icon(Icons.Default.Place, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Text(
                            text = selectedLocation,
                            modifier = Modifier.padding(horizontal = 9.dp).weight(1f),
                            fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.Black, modifier = Modifier.padding(end = 13.dp))
                        Text("MEALIFY", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                        Icon(Icons.Default.Notifications, null, tint = Color.Black)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Kolej Pendeta Za'ba", "Kolej Ungku Omar", "FTSM UKM").forEach { loc ->
                            DropdownMenuItem(text = { Text(loc) }, onClick = { selectedLocation = loc; expanded = false })
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                        placeholder = { Text("Search food and stores", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f).height(56.dp).background(Color.White, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MealifyDarkPurple)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { searchStatus = if (searchQuery.isNotEmpty()) "Searching for: $searchQuery" else "Enter a search term!" },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MealifyDarkPurple),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("GO", fontWeight = FontWeight.Bold)
                    }
                }
                Text(searchStatus, color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
        }

        item {
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val suggestions = listOf("Cakes 🍰", "Sushi 🍣", "Burgers 🍔", "Healthy 🥗", "Coffee ☕")
                items(suggestions) { item ->
                    SuggestionChip(
                        onClick = { searchQuery = item.split(" ")[0] },
                        label = { Text(item, fontSize = 11.sp, color = MealifyDarkPurple) }
                    )
                }
            }
        }

        item { AutoSlidingBanner() }

        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text("Categories", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val cats = listOf("Meals" to "🍲", "Desserts" to "🍰", "Bread" to "🥐", "Drinks" to "🍹", "Snacks" to "🍟", "Dine-In" to "🍽️", "Mystery Bags" to "🛍️")
                    items(cats) { cat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onCategoryClick(cat.first) }
                        ) {
                            Surface(modifier = Modifier.size(70.dp), shape = RoundedCornerShape(12.dp), color = MealifyDarkPurple) {
                                Box(contentAlignment = Alignment.Center) { Text(cat.second, fontSize = 32.sp) }
                            }
                            Text(text = cat.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp).width(70.dp))
                        }
                    }
                }
            }
        }

        item { Text("Top Deals ➔", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { FoodItemCard(R.drawable.smatcha, "Strawberry Matcha", "RM 15.00", "RM 19.00") }
                item { FoodItemCard(R.drawable.cake, "Triple Chocolate Cake", "RM 12.00", "RM 15.00") }
                item { FoodItemCard(R.drawable.redvelvet, "Red Velvet Slice", "RM 8.00", "RM 16.00") }
            }
        }

        item { Text("All Stores", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        item {
            Column {
                StoreItem("Cafe Chef Wan @ IOI City", "16:00-17:00", "8.6 km", "6 items") { onStoreClick("Cafe Chef Wan") }
                StoreItem("Lava Shortcakes Bangi", "20:00-22:30", "11.0 km", "1 item") { onStoreClick("Lava Shortcakes") }
                StoreItem("Rembayung", "14:30-22:00", "1.5 km", "11 items") { onStoreClick("Rembayung") }
            }
        }

        item { Spacer(Modifier.height(100.dp)) }
    }
}

// --- DETAIL PAGE ---
@Composable
fun DetailPage(title: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(MealifyDarkPurple).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Details for $title coming soon!", color = Color.Gray)
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: AppScreen, onTabSelected: (AppScreen) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HOME
                NavItem(Icons.Default.Home, "HOME", currentScreen == AppScreen.HOME) { onTabSelected(AppScreen.HOME) }

                // PARCELS
                NavItem(Icons.Default.Email, "PARCELS", currentScreen == AppScreen.PARCELS) { onTabSelected(AppScreen.PARCELS) }

                Spacer(modifier = Modifier.width(72.dp)) // Space for the center Cart button

                // ORDERS
                NavItem(Icons.Default.List, "ORDERS", currentScreen == AppScreen.ORDERS) { onTabSelected(AppScreen.ORDERS) }

                // ACCOUNT
                NavItem(Icons.Default.Person, "ACCOUNT", currentScreen == AppScreen.ACCOUNT) { onTabSelected(AppScreen.ACCOUNT) }
            }
        }

        // CENTER FLOATING CART
        Surface(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.TopCenter)
                .border(4.dp, Color.White, CircleShape)
                .clickable { onTabSelected(AppScreen.CART) },
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart",
                    tint = Color.Black, // Solid Black Icon
                    modifier = Modifier.size(28.dp)
                )
                Text("CART", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(26.dp),
            // Black when selected, Gray when not
            tint = if (isSelected) Color.Black else Color.LightGray
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.LightGray
        )
    }
}
// --- HELPERS (UNCHANGED) ---

@Composable
fun StoreItem(name: String, time: String, distance: String, itemsCount: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp, 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                    Box(contentAlignment = Alignment.Center) { Text("🏪", fontSize = 24.sp) }
                }
                Surface(modifier = Modifier.align(Alignment.BottomCenter).offset(y = 6.dp), color = MealifyDarkPurple, shape = RoundedCornerShape(4.dp)) {
                    Text(itemsCount, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold)
                Text("🕒 $time  📍 $distance", fontSize = 12.sp, color = Color.Gray)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp)
    }
}

@Composable
fun AutoSlidingBanner() {
    val bannerImages = listOf(R.drawable.sweet, R.drawable.drink, R.drawable.dessert, R.drawable.waterr, R.drawable.off_30, R.drawable.goodf, R.drawable.fastfood)
    val pagerState = rememberPagerState(pageCount = { bannerImages.size })
    LaunchedEffect(Unit) { while (true) { delay(3000); pagerState.animateScrollToPage((pagerState.currentPage + 1) % bannerImages.size) } }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(170.dp), contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 12.dp) { page ->
            Card(shape = RoundedCornerShape(12.dp)) { Image(painter = painterResource(id = bannerImages[page]), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
        Row(Modifier.height(20.dp).fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            repeat(bannerImages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MealifyDarkPurple else Color.LightGray
                Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(8.dp))
            }
        }
    }
}

@Composable
fun FoodItemCard(imageRes: Int, title: String, price: String, originalPrice: String) {
    Card(modifier = Modifier.width(280.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.fillMaxWidth().height(130.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(8.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row {
                    Text("$price ", fontWeight = FontWeight.Bold, color = MealifyDarkPurple)
                    Text(originalPrice, textDecoration = TextDecoration.LineThrough, fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenUI(onCategoryClick = {}, onStoreClick = {})
    }
}