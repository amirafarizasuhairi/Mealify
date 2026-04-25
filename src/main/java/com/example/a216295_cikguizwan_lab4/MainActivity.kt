package com.example.a216295_cikguizwan_lab4


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue // REQUIRED for 'by'
import androidx.compose.runtime.setValue // REQUIRED for 'by'
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController // Fixes the main navigation setup
import androidx.compose.foundation.clickable
// --- THEME COLORS ---
val MealifyLightPurple = Color(0xFFE1BEE7)
val MealifyDarkPurple = Color(0xFFB02CAC)
val MealifyHeaderGradient = Brush.verticalGradient(
    listOf(Color(0XFFC27EC0), Color(0xFFCC3DC8))
)

// --- DATA MODELS ---
enum class AppScreen { HOME, MEALS, ACCOUNT, PARCELS, ORDERS, CART, FOOD_DETAIL }

// Ensure this is outside of any class
data class FoodItem(
    val image: Int,
    val name: String,
    val price: String,
    val originalPrice: String,
    val description: String,
    val category: String,  // Added this
    val stock: String,     // Added this (e.g., "3 left")
    val storeName: String, // Added this
    val time: String,      // Added this
    val distance: String,  // Added this
    val discount: String   // Added this
)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // This is the Composable context - logic goes here!
            val navController = rememberNavController()
            val viewModel: MealifyViewModel = viewModel()

            var selectedFoodItem by remember { mutableStateOf<FoodItem?>(null) }

            NavHost(navController = navController, startDestination = "main_ui") {

                composable("main_ui") {
                    MainAppNavigation(navController, viewModel)
                }
                composable("meals/{categoryName}") { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("categoryName") ?: ""
                    MealsScreenUI(
                        categoryName = category,
                        onBack = { navController.popBackStack() },
                        onFoodClick = { clickedFood ->
                            selectedFoodItem = clickedFood
                            navController.navigate("foodDetail")
                        },
                        onCartClick = { /* Handle Cart */ }
                    )
                }
                composable("foodDetail") {
                    selectedFoodItem?.let { food ->
                        FoodDetailPage(
                            food = food,
                            onBack = { navController.popBackStack() },
                            onViewCart = { qty, total ->
                                navController.navigate("form")
                            }
                        )
                    }
                }
                composable("form") {
                    OrderFormScreen(navController, viewModel)
                }
                composable("confirmation") {
                    OrderConfirmationScreen(navController, viewModel)
                }
                composable("account") {
                    // Pass the viewModel so it can show the name
                    // Pass the navigate action so clicking "Profile" works
                    AccountScreen(viewModel = viewModel) {
                        navController.navigate("edit_profile")
                    }
                }

                composable("edit_profile") {
                    // This is the page where you insert Name, Phone, and Email
                    EditProfileScreen(navController = navController, viewModel = viewModel)
                }

                // --- PROFILE SECTION END ---
            }
        }
    }
}

@Composable
fun MainAppNavigation(navController: NavHostController, viewModel: MealifyViewModel) {
    var currentTab by remember { mutableStateOf(AppScreen.HOME) }
    var selectedCategory by remember { mutableStateOf("Meals") }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentTab,
                onTabSelected = { tab ->
                    // Logic for the center Cart icon
                    if (tab == AppScreen.CART) navController.navigate("form")
                    else currentTab = tab
                },
                viewModel = viewModel
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                AppScreen.HOME -> HomeScreenUI(
                    onCategoryClick = { category ->
                        selectedCategory = category
                        currentTab = AppScreen.MEALS
                    },
                    onStoreClick = { /* Logic for stores */ },
                    // NEW: Add to cart directly from Top Deals
                    onAddToCartClick = {
                        // For quick add, we just add 1 quantity
                        // You can pass a specific FoodItem here
                        navController.navigate("form")
                    }
                )
                AppScreen.MEALS -> MealsScreenUI(
                    categoryName = selectedCategory,
                    onBack = { currentTab = AppScreen.HOME },
                    onFoodClick = { food: FoodItem ->
                        selectedFood = food
                        currentTab = AppScreen.FOOD_DETAIL
                    },
                    onCartClick = { navController.navigate("form") }
                )
                AppScreen.FOOD_DETAIL -> selectedFood?.let { foodItem ->
                    FoodDetailPage(
                        food = foodItem,
                        onBack = { currentTab = AppScreen.MEALS },
                        onViewCart = { qty, total ->
                            // --- ADJUSTED HERE ---
                            // 1. Tell the ViewModel what food we just picked
                            viewModel.addToCart(foodItem, qty)

                            // 2. Go to the Form Screen immediately (image_4)
                            navController.navigate("form")
                        }
                    )
                }
                AppScreen.PARCELS -> ParcelsScreen()
                AppScreen.ORDERS -> OrdersScreen()
                AppScreen.ACCOUNT -> AccountScreen(viewModel) { navController.navigate("edit_profile") }
                else -> {}
            }
        }
    }
}






// --- NAVIGATION SCREENS ---

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
fun HomeScreenUI(
    onCategoryClick: (String) -> Unit,
    onStoreClick: (String) -> Unit,
    onAddToCartClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchStatus by remember { mutableStateOf("Discover delicious meals near you!") }
    var expanded by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("Kolej Pendeta Za'ba") }

    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
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
                        // Inside HomeScreenUI -> LazyColumn -> item -> Column -> Row
                        // Inside HomeScreenUI -> LazyColumn -> item -> Column -> Row
                        Text(
                            text = "MEALIFY",
                            style = MaterialTheme.typography.displayMedium, // Your base style
                            fontSize = 24.sp,                               // Your requested size
                            fontWeight = FontWeight.Bold,                  // <--- ADD THIS LINE
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
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

        item {
            Text(
                text = "Top Deals ➔",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge, // Error should disappear now!
                color = androidx.compose.ui.graphics.Color.Black
            )
        }
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
@Composable
fun MealsScreenUI(
    categoryName: String,
    onBack: () -> Unit,
    onFoodClick: (FoodItem) -> Unit,
    onCartClick: () -> Unit
) {
    // FIX 1: Properly declare the state so it can be updated
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // FIX 2: Complete FoodItem list with all fields from your Data Class
    val fullMealList = listOf(
        // --- MEALS CATEGORY (from image_36770a.jpg and image_3758a7.jpg) ---
        FoodItem(
            image = R.drawable.aglieolio,
            name = "Aglio E Olio",
            price = "RM 22.50",
            originalPrice = "RM 45.00",
            description = "(À la carte) Choice of fettucine, spaghetti or penne pasta, fresh ...",
            category = "Meals",
            stock = "3 left",
            storeName = "Palms Café, Palm Garden Hotel, Putrajaya",
            time = "12:00 - 23:00",
            distance = "9.2 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.nasigorengwarisan,
            name = "Nasi Goreng Warisan",
            price = "RM 17.50",
            originalPrice = "RM 35.00",
            description = "Malaysian stylr fried rice served with chicken wing, fish crackers...",
            category = "Meals",
            stock = "3 left",
            storeName = "Palms Café, Palm Garden Hotel, Putrajaya",
            time = "12:00 - 23:00",
            distance = "9.2 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.noodles,
            name = "Cantonese Styles Noodles with River Prawn",
            price = "RM 25.00",
            originalPrice = "RM 51.00",
            description = "(À la carte) Choice of yee mee or kuey teow noodles, large river ...",
            category = "Meals",
            stock = "1 left",
            storeName = "Palms Café, Palm Garden Hotel, Putrajaya",
            time = "12:00 - 23:00",
            distance = "9.2 km",
            discount = "51% off"
        ),
        FoodItem(
            image = R.drawable.kueyteow,
            name = "Fried Kuey Teow",
            price = "RM 16.00",
            originalPrice = "RM 32.00",
            description = "(À la carte) All-time favourite Penang style fried flat noodles wit...",
            category = "Meals",
            stock = "3 left",
            storeName = "Palms Café, Palm Garden Hotel, Putrajaya",
            time = "12:00 - 23:00",
            distance = "9.2 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.beehoon, // Replace with your bee hoon image
            name = "Fried Bee Hoon",
            price = "RM 16.00",
            originalPrice = "RM 32.00",
            description = "Fried rice vermicelli with julienned vegetables, chicken and seaf...",
            category = "Meals",
            stock = "3 left",
            storeName = "Palms Café, Palm Garden Hotel, Putrajaya",
            time = "12:00 - 23:00",
            distance = "9.2 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.meemamak, // Replace with your mee mamak image
            name = "Mee Mamak",
            price = "RM 16.00",
            originalPrice = "RM 32.00",
            description = "Spicy fried yellow noodles with prawns, beancurd and vegetables",
            category = "Meals",
            stock = "3 left",
            storeName = "Palms Café, Palm Garden Hotel, Putrajaya",
            time = "12:00 - 23:00",
            distance = "9.2 km",
            discount = "50% off"
        ),

        // --- DESSERTS CATEGORY (from image_367b2b.jpg) ---
        FoodItem(
            image = R.drawable.almondtiramisu,
            name = "Almond Tiramisu Cake",
            price = "RM 14.84",
            originalPrice = "RM 21.20",
            description = "Vanilla sponge cake layered with Mascarpone cheese, coffee syr...",
            category = "Desserts",
            stock = "10 left",
            storeName = "Cafe Chef Wan @ IOI City Mall",
            time = "16:00 - 17:00",
            distance = "8.5 km",
            discount = "30% off"
        ),
        FoodItem(
            image = R.drawable.bisscoff,
            name = "Biscoff Cheese Cake",
            price = "RM 14.84",
            originalPrice = "RM 21.20",
            description = "Creamy cheesecake layered with chocolate sponge, set on a Bis...",
            category = "Desserts",
            stock = "5 left",
            storeName = "Cafe Chef Wan @ IOI City Mall",
            time = "16:00 - 17:00",
            distance = "8.5 km",
            discount = "30% off"
        ),
        FoodItem(
            image = R.drawable.indulgence,
            name = "Chocolate Indulgent Cake",
            price = "RM 14.84",
            originalPrice = "RM 21.20",
            description = "Chocolate chiffon with creamy chocolate pudding and pitted da...",
            category = "Desserts",
            stock = "10 left",
            storeName = "Cafe Chef Wan @ IOI City Mall",
            time = "16:00 - 17:00",
            distance = "8.5 km",
            discount = "30% off"
        ),
        FoodItem(
            image = R.drawable.cempedakcheesecake, // Replace with cempedak cake image
            name = "Cempedak Baked Cheese Cake",
            price = "RM 5.00",
            originalPrice = "RM 23.32",
            description = "Smooth and creamy baked cheese with cracker base and cempe...",
            category = "Desserts",
            stock = "10 left",
            storeName = "Cafe Chef Wan @ IOI City Mall",
            time = "16:00 - 17:00",
            distance = "8.5 km",
            discount = "79% off"
        ),
        FoodItem(
            image = R.drawable.pandangulamelaka, // Replace with pandan cake image
            name = "Pandan Gula Melaka Cheese Cake",
            price = "RM 14.84",
            originalPrice = "RM 21.20",
            description = "Pandan baked cheese with light pandan mousse, based with gula...",
            category = "Desserts",
            stock = "5 left",
            storeName = "Cafe Chef Wan @ IOI City Mall",
            time = "16:00 - 17:00",
            distance = "8.5 km",
            discount = "30% off"
        ),
        FoodItem(
            image = R.drawable.applepie, // Replace with apple pie image
            name = "Apple Pie Slice",
            price = "RM 13.36",
            originalPrice = "RM 19.08",
            description = "Classic sweet tart shell filled with apple filling",
            category = "Desserts",
            stock = "5 left",
            storeName = "Cafe Chef Wan @ IOI City Mall",
            time = "16:00 - 17:00",
            distance = "8.5 km",
            discount = "30% off"
        ),
        FoodItem(
            image = R.drawable.bun, // Use appropriate bread icon
            name = "Assorted Bun (6 pcs)",
            price = "RM 10.00",
            originalPrice = "RM 20.00",
            description = "A mix of sweet and savoury buns freshly baked daily.",
            category = "Bread",
            stock = "2 left",
            storeName = "Deligateaux",
            time = "14:00 - 17:00",
            distance = "36.8 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.croisant,
            name = "Croissant Box (4 pcs)",
            price = "RM 12.00",
            originalPrice = "RM 24.00",
            description = "Flaky, buttery croissants perfect for breakfast.",
            category = "Bread",
            stock = "5 left",
            storeName = "Krem Tarik • Kota Damansara",
            time = "12:00 - 22:00",
            distance = "32.9 km",
            discount = "50% off"
        ),

        // --- GROCERY CATEGORY (From image_37c9a2.jpg & image_37c9bd.jpg) ---
        FoodItem(
            image = R.drawable.vege, // Use grocery/box icon
            name = "Organic Vegetable Box",
            price = "RM 15.00",
            originalPrice = "RM 30.00",
            description = "A selection of seasonal organic greens and roots.",
            category = "Grocery",
            stock = "8 left",
            storeName = "Watercolour Bakery & Cafe Ooak Suites",
            time = "11:00 - 18:00",
            distance = "30.7 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.greentea, // Ensure you have a drink icon in drawables
            name = "Green Tea",
            price = "RM 3.50",
            originalPrice = "RM 7.00",
            description = "This creamy, sweet, and refreshing beverage offers a bright green color.",
            category = "Drinks",
            stock = "10 left",
            storeName = "Cafe Chef Wan @ TTDI",
            time = "16:00 - 17:00",
            distance = "29.5 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.tehtarik,
            name = "Teh Tarik (Hot)",
            price = "RM 4.00",
            originalPrice = "RM 8.00",
            description = "Frothy pulled milk tea, a local Malaysian favorite.",
            category = "Drinks",
            stock = "10 left",
            storeName = "Cafe Chef Wan @ TTDI",
            time = "16:00 - 17:00",
            distance = "29.5 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.freshorange,
            name = "Fresh Orange Juice",
            price = "RM 8.50",
            originalPrice = "RM 12.00",
            description = "Freshly squeezed oranges with no added sugar.",
            category = "Drinks",
            stock = "10 left",
            storeName = "Sunset Terrace at Sunway Resort Hotel",
            time = "07:00 - 16:00",
            distance = "24.9 km",
            discount = "29% off"
        ),
        FoodItem(
            image = R.drawable.icelemontea,
            name = "Iced Lemon Tea",
            price = "RM 5.50",
            originalPrice = "RM 11.00",
            description = "Refreshing black tea with a splash of lemon and ice.",
            category = "Drinks",
            stock = "10 left",
            storeName = "Palms Café, Palm Garden Hotel, Putrajaya",
            time = "12:00 - 23:00",
            distance = "9.2 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.mineral,
            name = "Mineral Water (500ml)",
            price = "RM 2.00",
            originalPrice = "RM 4.00",
            description = "Chilled bottled mineral water.",
            category = "Drinks",
            stock = "10 left",
            storeName = "Deligateaux",
            time = "14:00 - 17:00",
            distance = "36.8 km",
            discount = "50% off"
        ),
// --- MYSTERY BAG CATEGORY ---
        FoodItem(
            image = R.drawable.mystery_bag,
            name = "Mystery Bag (Pastries)",
            price = "RM 15.00",
            originalPrice = "RM 30.00",
            description = "A surprise selection of our best-selling daily pastries and buns.",
            category = "Mystery Bag",
            stock = "10 left",
            storeName = "Watercolour Bakery & Cafe Ooak Suites",
            time = "11:00 - 18:00",
            distance = "30.7 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.surprisecookiebox,
            name = "Surprise Cookie Box",
            price = "RM 18.00",
            originalPrice = "RM 26.00",
            description = "A mix of our premium soft chunky cookies of the day.",
            category = "Mystery Bag",
            stock = "10 left",
            storeName = "EM'S Soft Chunky Cookies, Seventeen Mall",
            time = "17:00 - 21:00",
            distance = "28.0 km",
            discount = "31% off"
        ),

        // --- DINE-IN CATEGORY (from image_398f42.jpg) ---
        FoodItem(
            image = R.drawable.pasta,
            name = "Pasta & Drink Set",
            price = "RM 25.00",
            originalPrice = "RM 50.00",
            description = "Enjoy a main pasta dish with a refreshing iced lemon tea.",
            category = "Dine-In",
            stock = "10 left",
            storeName = "Palms Café, Palm Garden Hotel",
            time = "12:00 - 23:00",
            distance = "9.2 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.cafechefwan,
            name = "Chef Wan Special Platter",
            price = "RM 45.00",
            originalPrice = "RM 90.00",
            description = "A curated tasting platter of Chef Wan's signature local delights.",
            category = "Dine-In",
            stock = "10 left",
            storeName = "Cafe Chef Wan @ IOI City Mall",
            time = "16:00 - 17:00",
            distance = "8.5 km",
            discount = "50% off"
        ),

        // --- SNACKS CATEGORY (from image_398f5d.jpg) ---
        FoodItem(
            image = R.drawable.snackscheetoz,
            name = "Assorted Savory Snacks",
            price = "RM 8.00",
            originalPrice = "RM 16.00",
            description = "A variety of crunchy and savory local tea-time snacks.",
            category = "Snacks",
            stock = "10 left",
            storeName = "Deligateaux",
            time = "14:00 - 17:00",
            distance = "36.8 km",
            discount = "50% off"
        ),
        FoodItem(
            image = R.drawable.lavacake,
            name = "Lava Shortcake (Single)",
            price = "RM 10.00",
            originalPrice = "RM 15.00",
            description = "Rich chocolate lava shortcake with a molten center.",
            category = "Snacks",
            stock = "10 left",
            storeName = "Lava Shortcakes Bangi",
            time = "20:00 - 22:30",
            distance = "11.0 km",
            discount = "33% off"
        )
    )
    // FIX 3: Filter the list based on the clicked category
    val filteredList = fullMealList.filter { it.category == categoryName }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Green Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFB02CAC)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text(
                text = categoryName,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = { /* Search */ }) { Icon(Icons.Default.Search, null) }
            IconButton(onClick = onCartClick) { Icon(Icons.Default.ShoppingCart, null) }
        }

        // Tabs (Food | Stores)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = Color(0xFFB02CAC),
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFB02CAC)
                    )
                }
            }
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                Text("Food", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold,
                    color = if(selectedTabIndex == 0) Color(0xFFB02CAC) else Color.Gray)
            }
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                Text("Stores", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold,
                    color = if(selectedTabIndex == 1) Color(0xFFB02CAC) else Color.Gray)
            }
        }

        // List Content - This is scrollable (LazyColumn)
        if (selectedTabIndex == 0) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Use filteredList here to match the filtered data
                items(filteredList) { meal ->
                    MealListItem(meal, onFoodClick)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { StoreItemDetail("Cafe Chef Wan @ IOI City", "16:00-17:00", "8.6 km", "6 items") }
                item { StoreItemDetail("Lava Shortcakes Bangi", "20:00-22:30", "11.0 km", "1 item") }
                item { StoreItemDetail("Forus Farmers Market & Cafe", "16:00-17:00", "8.6 km", "6 items") }
                item { StoreItemDetail("Dorsett Kuala Lumpur", "20:00-22:30", "11.0 km", "1 item") }

            }
        }
    }
}




@Composable
fun MealListItem(meal: FoodItem, onFoodClick: (FoodItem) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onFoodClick(meal) }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Image(painter = painterResource(id = meal.image), contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Surface(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp), color = Color(
                0xFFB02CAC
            ), shape = RoundedCornerShape(4.dp)) {
                Text("5 left", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(meal.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(meal.price, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text(meal.originalPrice, textDecoration = TextDecoration.LineThrough, color = Color.Gray, fontSize = 12.sp)
            }
            Text("Cafe Mint, Cosmo Hotel KL", fontSize = 12.sp, color = Color.Gray)
            Text("🕒 08:00 - 11:30  📍 26.2 km", fontSize = 10.sp, color = Color.Gray)
            Text(meal.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
}



@Composable
fun MealsScreenUI(
    categoryName: String,
    onBack: () -> Unit,
    onFoodClick: (FoodItem) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // DATA LIST (Add all the items from your screenshots here)
    val allFoodItems = listOf(
        // MEALS SECTION
        FoodItem(R.drawable.fastfood, "Fried Kuey Teow", "RM 16.00", "RM 32.00", "All-time favourite Penang style...", "Meals", "3 left", "Palms Café", "12:00-23:00", "9.2 km", "50% off"),
        FoodItem(R.drawable.fastf, "Cantonese Style Noodles", "RM 25.00", "RM 51.00", "Large river prawn...", "Meals", "1 left", "Palms Café", "12:00-23:00", "9.2 km", "51% off"),
        FoodItem(R.drawable.burger, "Nasi Goreng Warisan", "RM 17.50", "RM 35.00", "Malaysian style fried rice...", "Meals", "3 left", "Palms Café", "12:00-23:00", "9.2 km", "50% off"),

        // DESSERTS SECTION
        FoodItem(R.drawable.cake, "Almond Tiramisu Cake", "RM 14.84", "RM 21.20", "Vanilla sponge cake...", "Desserts", "10 left", "Chef Wan @ IOI", "16:00-17:00", "8.5 km", "30% off"),
        FoodItem(R.drawable.dessert, "Biscoff Cheese Cake", "RM 14.84", "RM 21.20", "Creamy cheesecake...", "Desserts", "5 left", "Chef Wan @ IOI", "16:00-17:00", "8.5 km", "30% off")
    )

    val filteredList = allFoodItems.filter { it.category == categoryName }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Green Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFB02CAC)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text(categoryName, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Icon(Icons.Default.Search, null)
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Default.ShoppingCart, null)
        }

        // Tabs: Food | Stores
        TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White, contentColor = Color(
            0xFFB02CAC
        )
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                Text("Food", modifier = Modifier.padding(16.dp), color = if(selectedTabIndex==0) Color(
                    0xFFB02CAC
                ) else Color.Gray)
            }
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                Text("Stores", modifier = Modifier.padding(16.dp), color = if(selectedTabIndex==1) Color(
                    0xFFB02CAC
                ) else Color.Gray)
            }
        }

        if (selectedTabIndex == 0) {
            // Food List (SCROLLABLE)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredList) { item ->
                    MealListItem(item, onFoodClick)
                }
            }
        } else {
            // Stores List (SCROLLABLE)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { StoreItemDetail("Sunset Terrace", "07:00-16:00", "24.9 km", "2 items") }
                item { StoreItemDetail("Palms Café", "12:00-23:00", "9.2 km", "6 items") }
                item { StoreItemDetail("Cafe Chef Wan", "16:00-17:00", "8.5 km", "7 items") }
            }
        }
    }
}

@Composable
fun StoreItemDetail(name: String, time: String, distance: String, items: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(8.dp), color = Color.LightGray) {
            Box(contentAlignment = Alignment.Center) { Text("Logo") }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(name, fontWeight = FontWeight.Bold)
            Row {
                Text(time, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.width(10.dp))
                Text(distance, fontSize = 12.sp, color = Color.Gray)
            }
            Surface(color = Color(0xFFC035BB), shape = RoundedCornerShape(4.dp)) {
                Text("$items items", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
}

@Composable
fun BottomNavigationBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    viewModel: MealifyViewModel
) {
    // 1. Get the current count from your ViewModel
    // If you have a list in ViewModel named 'cartItems', we get its size
    val cartCount = viewModel.cartItems.size

    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            color = Color.White, shadowElevation = 8.dp
        ) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                NavItem(Icons.Default.Home, "HOME", currentScreen == AppScreen.HOME) { onTabSelected(AppScreen.HOME) }
                NavItem(Icons.Default.Email, "PARCELS", currentScreen == AppScreen.PARCELS) { onTabSelected(AppScreen.PARCELS) }
                Spacer(modifier = Modifier.width(72.dp))
                NavItem(Icons.Default.List, "ORDERS", currentScreen == AppScreen.ORDERS) { onTabSelected(AppScreen.ORDERS) }
                NavItem(Icons.Default.Person, "ACCOUNT", currentScreen == AppScreen.ACCOUNT) { onTabSelected(AppScreen.ACCOUNT) }
            }
        }

        // --- THE FLOATING CART BUTTON ---
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

                // 2. Wrap the Icon in a BadgedBox
                BadgedBox(
                    badge = {
                        // Only show the green circle if there is at least 1 item
                        if (cartCount > 0) {
                            Badge(
                                containerColor = Color(0xFFAD15B2), // Lime green color from your image
                                contentColor = Color.Black,
                                modifier = Modifier.offset(x = (-4).dp, y = 4.dp) // Adjust position
                            ) {
                                Text(cartCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = if (currentScreen == AppScreen.CART) MealifyDarkPurple else Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "CART",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentScreen == AppScreen.CART) MealifyDarkPurple else Color.Black
                )
            }
        }
    }
}

@Composable
fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(26.dp), tint = if (isSelected) MealifyDarkPurple else Color.LightGray)
        Text(text = label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MealifyDarkPurple else Color.LightGray)
    }
}

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
    // State to track if the card is expanded
    var isExpanded by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    // Task 3: Scale Animation for the "Click" feel
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "PressScale"
    )

    Card(
        modifier = Modifier
            .width(260.dp)
            .padding(bottom = 8.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale) // Press effect
            .animateContentSize( // Task 3: Smoothly animates the card height change
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { isExpanded = !isExpanded } // Toggle expansion on tap
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 12.dp else 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    // Task 3: Rotating Icon to indicate expansion
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer(
                            rotationZ = if (isExpanded) 180f else 0f
                        ),
                        tint = MealifyDarkPurple
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = price, fontWeight = FontWeight.ExtraBold, color = MealifyDarkPurple)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = originalPrice,
                        textDecoration = TextDecoration.LineThrough,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                // Task 3: Expanded Content
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Deliciously made with fresh ingredients. Click for more details!",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { /* Add to Cart Logic */ },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MealifyDarkPurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}




@Composable
fun FoodDealCard(
    image: Int,
    name: String,
    price: String,
    originalPrice: String,
    onAddToCartClick: () -> Unit // This is caught from HomeScreenUI
) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = image),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(price, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(originalPrice, color = Color.Gray, fontSize = 11.sp, textDecoration = TextDecoration.LineThrough)
                }

                Spacer(Modifier.height(8.dp))

                // THE PURPLE BUTTON
                Button(
                    onClick = onAddToCartClick, // <--- THIS TRIGGERS THE BADGE INCREASE
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)), // Purple
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Add to Cart", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
@Composable
fun ParcelsScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().background(MealifyDarkPurple).padding(20.dp), contentAlignment = Alignment.Center) {
            Text("Parcels", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text("No Active Parcels", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(
                "When you order a MEALIFY parcel, you can track its delivery status here!",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}
@Composable
fun OrdersScreen() {
    var selectedOrderTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9F9))) {
        Box(modifier = Modifier.fillMaxWidth().background(MealifyDarkPurple).padding(20.dp), contentAlignment = Alignment.Center) {
            Text("My Orders", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        TabRow(selectedTabIndex = selectedOrderTab, containerColor = Color.White, contentColor = MealifyDarkPurple) {
            Tab(selected = selectedOrderTab == 0, onClick = { selectedOrderTab = 0 }) {
                Text("Active", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedOrderTab == 1, onClick = { selectedOrderTab = 1 }) {
                Text("Past Orders", modifier = Modifier.padding(16.dp))
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.LightGray)
                Text("No orders yet", color = Color.Gray)
            }
        }
    }
}
@Composable
fun AccountScreen(viewModel: MealifyViewModel, onEditProfile: () -> Unit) {
    // Collect the live profile data from ViewModel
    val profile = viewModel.userProfile

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // --- 1. DYNAMIC PROFILE HEADER ---
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFAB15A3)).padding(30.dp)) { // Green header like image
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.3f)) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    // SHOWS DYNAMIC NAME FROM VIEWMODEL
                    Text(
                        text = "${profile.firstName} ${profile.lastName}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = profile.email,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- 2. LIST OPTIONS ---
        Column(modifier = Modifier.padding(16.dp).background(Color.White, RoundedCornerShape(12.dp))) {

            // LANGUAGE ROW (Static)
            ProfileOptionRow("Language", Icons.Default.Info) {}
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // PROFILE ROW (CLICKABLE - GOES TO EDIT PAGE)
            ProfileOptionRow("Profile", Icons.Default.Person) {
                onEditProfile() // This triggers navigation to EditProfileScreen
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // OTHER ROWS
            ProfileOptionRow("Saved Addresses", Icons.Default.Place) {}
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            ProfileOptionRow("Coupons", Icons.Default.Star) {}
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            ProfileOptionRow("Settings", Icons.Default.Settings) {}
        }
    }
}

// Reusable Row Component to handle clicks
@Composable
fun ProfileOptionRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // MAKE IT CLICKABLE
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFFAB15A3), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 16.sp)
        }
        Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.LightGray)
    }
}

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

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Welcome to $title", fontWeight = FontWeight.Bold)
            Text("Content for this category is currently being updated.", color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
@Composable
fun FoodDetailPage(food: FoodItem, onBack: () -> Unit, onViewCart: (Int, Double) -> Unit) {
    // 1. ADD QUANTITY STATE
    var quantity by remember { mutableIntStateOf(1) }

    // 2. MATH FOR TOTAL AMOUNT
    // We remove "RM " and convert the string to a number to calculate
    val pricePerItem = food.price.replace("RM ", "").toDoubleOrNull() ?: 0.0
    val totalAmount = pricePerItem * quantity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Image Header (Same as before)
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Image(
                painter = painterResource(id = food.image),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp).background(Color.Black.copy(0.3f), CircleShape)
            ) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = food.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Text(text = food.price, color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = food.originalPrice, textDecoration = TextDecoration.LineThrough, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = food.discount, color = Color(0xFFAB15A3), fontWeight = FontWeight.Bold)
            }

            Text(text = food.storeName, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            Text(text = "🕒 ${food.time}  📍 ${food.distance}", fontSize = 12.sp, color = Color.Gray)

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

            Text(text = "About this meal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                text = food.description,
                color = Color.DarkGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Push the next items to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // --- 3. QUANTITY SELECTOR SECTION ---
            Text("Select Quantity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Minus Button
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape).size(40.dp)
                ) { Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold) }

                // Number Display
                Text(
                    text = quantity.toString(),
                    modifier = Modifier.padding(horizontal = 32.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Plus Button
                IconButton(
                    onClick = { quantity++ },
                    modifier = Modifier.background(Color(0xFFAB15A3), CircleShape).size(40.dp)
                ) { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }

            // --- 4. ADJUSTED ADD TO CART BUTTON ---
            Button(
                onClick = { onViewCart(quantity, totalAmount) },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAB15A3)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add to cart", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    // Format to 2 decimal places (e.g., RM 29.68)
                    Text(
                        text = "RM ${String.format("%.2f", totalAmount)}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenUI(
            onCategoryClick = {},
            onStoreClick = {},
            onAddToCartClick = {}
        )
    }
}