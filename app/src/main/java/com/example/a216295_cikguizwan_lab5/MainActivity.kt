package com.example.a216295_cikguizwan_lab5
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

// --- THEME COLORS ---
val MealifyLightPurple = Color(0xFFE1BEE7)
val MealifyDarkPurple = Color(0xFFB02CAC)
val MealifyHeaderGradient = Brush.verticalGradient(
    listOf(Color(0XFFC27EC0), Color(0xFFCC3DC8))
)

// --- DATA MODELS ---
enum class AppScreen { HOME, MEALS, ACCOUNT, PARCELS, ORDERS, CART, FOOD_DETAIL }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: MealifyViewModel = viewModel()

            // 🛠️ DAFTARKAN CONTEXT KEPADA VIEWMODEL DI SINI SEBELUM APA-APA NAVIGASI BERLAKU!
            viewModel.initRepository(applicationContext)

            var selectedFoodItem by remember { mutableStateOf<FoodItem?>(null) }

            // Seterusnya kod NavHost anda dikekalkan 100% tanpa sebarang perubahan...
            NavHost(navController = navController, startDestination = "main_ui") {
                // ... (Kekalkan semua kod rute anda yang lama di bawah ini)
                // Skrin Utama Aplikasi (Senarai makanan/Kategori)
                composable("main_ui") {
                    MainAppNavigation(navController, viewModel)
                }

                // Skrin Senarai Makanan Mengikut Kategori
                composable("meals/{categoryName}") { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("categoryName") ?: ""
                    MealsScreenUI(
                        categoryName = category,
                        onBack = { navController.popBackStack() },
                        onFoodClick = { clickedFood ->
                            var selectedFoodItem = clickedFood
                            navController.navigate("foodDetail")
                        },
                        onCartClick = {
                            // Membawa pengguna ke paparan Orders / Cart apabila ikon troli ditekan
                            navController.navigate("orders_route_name")
                        }
                    )
                }

                // Skrin Perincian Makanan (Food Detail)
                composable("foodDetail") {
                    val selectedFoodItem = null
                    selectedFoodItem?.let { food ->
                        FoodDetailPage(
                            food = food,
                            viewModel = viewModel, // 🛠️ PAS PARAMETER VIEWMODEL KE SINI AGAR ROOM DB BERFUNGSI
                            onBack = { navController.popBackStack() },
                            onViewCart = { qty, total ->
                                navController.navigate("form")
                            }
                        )
                    }
                }

                // Skrin Borang Pesanan (Order Form)
                composable("form") {
                    OrderFormScreen(navController, viewModel)
                }

                // Skrin Pengesahan Pesanan (Confirmation)
                composable("confirmation") {
                    OrderConfirmationScreen(navController, viewModel)
                }

                // 🛠️ PENAMBAHAN RUTE DYNAMIK ORDERS SCREEN SEPERTI DALAM VIDEO DEMO
                composable("orders_route_name") {
                    OrdersScreen(viewModel = viewModel)
                }

                // GERBANG UTAMA: APABILA ICON/TAB ACCOUNT DITEKAN, MESTI LALU DI SINI DAHULU
                composable("google_login") {
                    GoogleLoginScreen(navController = navController, viewModel = viewModel)
                }

                // Skrin Paparan Profil
                composable("account") {
                    AccountScreen(
                        viewModel = viewModel,
                        navController = navController,
                        onEditProfile = {
                            navController.navigate("edit_profile")
                        }
                    )
                }

                // Skrin Mengedit Maklumat Profil
                composable("edit_profile") {
                    EditProfileScreen(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}
@Composable
fun MainAppNavigation(navController: NavHostController, viewModel: MealifyViewModel) {
    var currentTab by remember { mutableStateOf(AppScreen.HOME) }
    var selectedCategory by remember { mutableStateOf("Meals") }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }

    // 🛠️ KOD PENYELAMAT MUTLAK: Memaksa pangkalan data Room dihubungkan semula ke ViewModel
    // setiap kali skrin utama atau tab bawah ini dimuatkan semula!
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initRepository(context)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentTab,
                onTabSelected = { tab ->
                    if (tab == AppScreen.CART) navController.navigate("form")
                    else currentTab = tab
                },
                viewModel = viewModel
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // ONLY ONE when statement should exist here
            when (currentTab) {
                AppScreen.HOME -> HomeScreenUI(
                    onCategoryClick = { category ->
                        selectedCategory = category
                        currentTab = AppScreen.MEALS
                    },
                    onStoreClick = { /* Logic */ },
                    onAddToCartClick = { navController.navigate("form") }
                )

                AppScreen.MEALS -> MealsScreenUI(
                    categoryName = selectedCategory,
                    onBack = { currentTab = AppScreen.HOME },
                    onFoodClick = { food ->
                        selectedFood = food
                        currentTab = AppScreen.FOOD_DETAIL
                    },
                    onCartClick = { navController.navigate("form") }
                )

                AppScreen.FOOD_DETAIL -> selectedFood?.let { foodItem ->
                    FoodDetailPage(
                        food = foodItem,
                        viewModel = viewModel,
                        onBack = { currentTab = AppScreen.MEALS },
                        onViewCart = { qty, total ->
                            viewModel.addToCart(foodItem, qty)
                            navController.navigate("form")
                        }
                    )
                }

                // 🛠️ Membaca data dengan stabil menggunakan viewModel yang sudah di-init dengan context
                AppScreen.ORDERS -> OrdersScreen(viewModel = viewModel)

                // --- FIX: This is now a proper case inside the when block ---
                AppScreen.ACCOUNT -> AccountScreen(
                    viewModel = viewModel,
                    navController = navController,
                    onEditProfile = { navController.navigate("edit_profile") }
                )

                else -> { /* Handle other cases if needed */ }
            }
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
                color = Color.Black
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
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
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

// =========================================================================
// 🛒 DATA INDUK UNTUK RUJUKAN GAMBAR & BUTIRAN DINAMIK (26 MENU AMIRA)
// =========================================================================
val masterFoodList = listOf(
    FoodItem(image = R.drawable.aglieolio, name = "Aglio E Olio", price = "RM 22.50", originalPrice = "RM 45.00", description = "(À la carte) Choice of fettucine, spaghetti or penne pasta, fresh ...", category = "Meals", stock = "3 left", storeName = "Palms Café, Palm Garden Hotel, Putrajaya", time = "12:00 - 23:00", distance = "9.2 km", discount = "50% off"),
    FoodItem(image = R.drawable.nasigorengwarisan, name = "Nasi Goreng Warisan", price = "RM 17.50", originalPrice = "RM 35.00", description = "Malaysian stylr fried rice served with chicken wing, fish crackers...", category = "Meals", stock = "3 left", storeName = "Palms Café, Palm Garden Hotel, Putrajaya", time = "12:00 - 23:00", distance = "9.2 km", discount = "50% off"),
    FoodItem(image = R.drawable.noodles, name = "Cantonese Styles Noodles with River Prawn", price = "RM 25.00", originalPrice = "RM 51.00", description = "(À la carte) Choice of yee mee or kuey teow noodles, large river ...", category = "Meals", stock = "1 left", storeName = "Palms Café, Palm Garden Hotel, Putrajaya", time = "12:00 - 23:00", distance = "9.2 km", discount = "51% off"),
    FoodItem(image = R.drawable.kueyteow, name = "Fried Kuey Teow", price = "RM 16.00", originalPrice = "RM 32.00", description = "(À la carte) All-time favourite Penang style fried flat noodles wit...", category = "Meals", stock = "3 left", storeName = "Palms Café, Palm Garden Hotel, Putrajaya", time = "12:00 - 23:00", distance = "9.2 km", discount = "50% off"),
    FoodItem(image = R.drawable.beehoon, name = "Fried Bee Hoon", price = "RM 16.00", originalPrice = "RM 32.00", description = "Fried rice vermicelli with julienned vegetables, chicken and seaf...", category = "Meals", stock = "3 left", storeName = "Palms Café, Palm Garden Hotel, Putrajaya", time = "12:00 - 23:00", distance = "9.2 km", discount = "50% off"),
    FoodItem(image = R.drawable.meemamak, name = "Mee Mamak", price = "RM 16.00", originalPrice = "RM 32.00", description = "Spicy fried yellow noodles with prawns, beancurd and vegetables", category = "Meals", stock = "3 left", storeName = "Palms Café, Palm Garden Hotel, Putrajaya", time = "12:00 - 23:00", distance = "9.2 km", discount = "50% off"),
    FoodItem(image = R.drawable.almondtiramisu, name = "Almond Tiramisu Cake", price = "RM 14.84", originalPrice = "RM 21.20", description = "Vanilla sponge cake layered with Mascarpone cheese, coffee syr...", category = "Desserts", stock = "10 left", storeName = "Cafe Chef Wan @ IOI City Mall", time = "16:00 - 17:00", distance = "8.5 km", discount = "30% off"),
    FoodItem(image = R.drawable.bisscoff, name = "Biscoff Cheese Cake", price = "RM 14.84", originalPrice = "RM 21.20", description = "Creamy cheesecake layered with chocolate sponge, set on a Bis...", category = "Desserts", stock = "5 left", storeName = "Cafe Chef Wan @ IOI City Mall", time = "16:00 - 17:00", distance = "8.5 km", discount = "30% off"),
    FoodItem(image = R.drawable.indulgence, name = "Chocolate Indulgent Cake", price = "RM 14.84", originalPrice = "RM 21.20", description = "Chocolate chiffon with creamy chocolate pudding and pitted da...", category = "Desserts", stock = "10 left", storeName = "Cafe Chef Wan @ IOI City Mall", time = "16:00 - 17:00", distance = "8.5 km", discount = "30% off"),
    FoodItem(image = R.drawable.cempedakcheesecake, name = "Cempedak Baked Cheese Cake", price = "RM 5.00", originalPrice = "RM 23.32", description = "Smooth and creamy baked cheese with cracker base and cempe...", category = "Desserts", stock = "10 left", storeName = "Cafe Chef Wan @ IOI City Mall", time = "16:00 - 17:00", distance = "8.5 km", discount = "79% off"),
    FoodItem(image = R.drawable.pandangulamelaka, name = "Pandan Gula Melaka Cheese Cake", price = "RM 14.84", originalPrice = "RM 21.20", description = "Pandan baked cheese with light pandan mousse, based with gula...", category = "Desserts", stock = "5 left", storeName = "Cafe Chef Wan @ IOI City Mall", time = "16:00 - 17:00", distance = "8.5 km", discount = "30% off"),
    FoodItem(image = R.drawable.applepie, name = "Apple Pie Slice", price = "RM 13.36", originalPrice = "RM 19.08", description = "Classic sweet tart shell filled with apple filling", category = "Desserts", stock = "5 left", storeName = "Cafe Chef Wan @ IOI City Mall", time = "16:00 - 17:00", distance = "8.5 km", discount = "30% off"),
    FoodItem(image = R.drawable.bun, name = "Assorted Bun (6 pcs)", price = "RM 10.00", originalPrice = "RM 20.00", description = "A mix of sweet and savoury buns freshly baked daily.", category = "Bread", stock = "2 left", storeName = "Deligateaux", time = "14:00 - 17:00", distance = "36.8 km", discount = "50% off"),
    FoodItem(image = R.drawable.croisant, name = "Croissant Box (4 pcs)", price = "RM 12.00", originalPrice = "RM 24.00", description = "Flaky, buttery croissants perfect for breakfast.", category = "Bread", stock = "5 left", storeName = "Krem Tarik • Kota Damansara", time = "12:00 - 22:00", distance = "32.9 km", discount = "50% off"),
    FoodItem(image = R.drawable.vege, name = "Organic Vegetable Box", price = "RM 15.00", originalPrice = "RM 30.00", description = "A selection of seasonal organic greens and roots.", category = "Grocery", stock = "8 left", storeName = "Watercolour Bakery & Cafe Ooak Suites", time = "11:00 - 18:00", distance = "30.7 km", discount = "50% off"),
    FoodItem(image = R.drawable.greentea, name = "Green Tea", price = "RM 3.50", originalPrice = "RM 7.00", description = "This creamy, sweet, and refreshing beverage offers a bright green color.", category = "Drinks", stock = "10 left", storeName = "Cafe Chef Wan @ TTDI", time = "16:00 - 17:00", distance = "29.5 km", discount = "50% off"),
    FoodItem(image = R.drawable.tehtarik, name = "Teh Tarik (Hot)", price = "RM 4.00", originalPrice = "RM 8.00", description = "Frothy pulled milk tea, a local Malaysian favorite.", category = "Drinks", stock = "10 left", storeName = "Cafe Chef Wan @ TTDI", time = "16:00 - 17:00", distance = "29.5 km", discount = "50% off"),
    FoodItem(image = R.drawable.freshorange, name = "Fresh Orange Juice", price = "RM 8.50", originalPrice = "RM 12.00", description = "Freshly squeezed oranges with no added sugar.", category = "Drinks", stock = "10 left", storeName = "Sunset Terrace at Sunway Resort Hotel", time = "07:00 - 16:00", distance = "24.9 km", discount = "29% off"),
    FoodItem(image = R.drawable.icelemontea, name = "Iced Lemon Tea", price = "RM 5.50", originalPrice = "RM 11.00", description = "Refreshing black tea with a splash of lemon and ice.", category = "Drinks", stock = "10 left", storeName = "Palms Café, Palm Garden Hotel, Putrajaya", time = "12:00 - 23:00", distance = "9.2 km", discount = "50% off"),
    FoodItem(image = R.drawable.mineral, name = "Mineral Water (500ml)", price = "RM 2.00", originalPrice = "RM 4.00", description = "Chilled bottled mineral water.", category = "Drinks", stock = "10 left", storeName = "Deligateaux", time = "14:00 - 17:00", distance = "36.8 km", discount = "50% off"),
    FoodItem(image = R.drawable.mystery_bag, name = "Mystery Bag (Pastries)", price = "RM 15.00", originalPrice = "RM 30.00", description = "A surprise selection of our best-selling daily pastries and buns.", category = "Mystery Bag", stock = "10 left", storeName = "Watercolour Bakery & Cafe Ooak Suites", time = "11:00 - 18:00", distance = "30.7 km", discount = "50% off"),
    FoodItem(image = R.drawable.surprisecookiebox, name = "Surprise Cookie Box", price = "RM 18.00", originalPrice = "RM 26.00", description = "A mix of our premium soft chunky cookies of the day.", category = "Mystery Bag", stock = "10 left", storeName = "EM'S Soft Chunky Cookies, Seventeen Mall", time = "17:00 - 21:00", distance = "28.0 km", discount = "31% off"),
    FoodItem(image = R.drawable.pasta, name = "Pasta & Drink Set", price = "RM 25.00", originalPrice = "RM 50.00", description = "Enjoy a main pasta dish with a refreshing iced lemon tea.", category = "Dine-In", stock = "10 left", storeName = "Palms Café, Palm Garden Hotel", time = "12:00 - 23:00", distance = "9.2 km", discount = "50% off"),
    FoodItem(image = R.drawable.cafechefwan, name = "Chef Wan Special Platter", price = "RM 45.00", originalPrice = "RM 90.00", description = "A curated tasting platter of Chef Wan's signature local delights.", category = "Dine-In", stock = "10 left", storeName = "Cafe Chef Wan @ IOI City Mall", time = "16:00 - 17:00", distance = "8.5 km", discount = "50% off"),
    FoodItem(image = R.drawable.snackscheetoz, name = "Assorted Savory Snacks", price = "RM 8.00", originalPrice = "RM 16.00", description = "A variety of crunchy and savory local tea-time snacks.", category = "Snacks", stock = "10 left", storeName = "Deligateaux", time = "14:00 - 17:00", distance = "36.8 km", discount = "50% off"),
    FoodItem(image = R.drawable.lavacake, name = "Lava Shortcake (Single)", price = "RM 10.00", originalPrice = "RM 15.00", description = "Rich chocolate lava shortcake with a molten center.", category = "Snacks", stock = "10 left", storeName = "Lava Shortcakes Bangi", time = "20:00 - 22:30", distance = "11.0 km", discount = "33% off")
)
@Composable
fun OrdersScreen(viewModel: MealifyViewModel) {
    val dbOrders by viewModel.cartItemsState.collectAsState()

    // 🌟 STATE: Mengawal tab mana yang aktif (0 = Orders, 1 = Table Bookings)
    var selectedTabIndex by remember { mutableStateOf(0) }

    var selectedOrderForPay by remember { mutableStateOf<CartItem?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
        // --- HEADER ---
        Box(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFB02CAC)).padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("My Activity History", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // --- 🔄 TABS DINAMIK BARU (DIJAMIN BOLEH DIKLIK & RESPONSIF) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // ---- TAB 1: ORDERS ----
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTabIndex = 0 } // Tukar state ke skrin Orders
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Orders",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTabIndex == 0) Color(0xFFB02CAC) else Color.Gray
                    )
                    if (selectedTabIndex == 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(40.dp).height(3.dp).background(Color(0xFFB02CAC)))
                    }
                }
            }

            // ---- TAB 2: TABLE BOOKINGS ----
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTabIndex = 1 } // Tukar state ke skrin Bookings
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Table Bookings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTabIndex == 1) Color(0xFFB02CAC) else Color.Gray
                    )
                    if (selectedTabIndex == 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(80.dp).height(3.dp).background(Color(0xFFB02CAC)))
                    }
                }
            }
        }

        // --- 🎭 PAPARAN SKRIN MENGIKUT TAB YANG DIPILIH ---
        if (selectedTabIndex == 0) {
            // ==========================================
            // 🛒 SKRIN A: ACTIVE ORDERS
            // ==========================================
            Text(
                text = "Active Orders",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB02CAC),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            if (dbOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active orders found in Database", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dbOrders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOrderForPay = order },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            OrderFoodCard(cartItem = order)
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // 📅 SKRIN B: TABLE BOOKINGS
            // ==========================================
            Text(
                text = "Active Table Bookings",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB02CAC),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No active table bookings found", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Your reservations will appear here ✨", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }

    // --- DIALOG 1: PAYMENT PROCEDURE ---
    selectedOrderForPay?.let { order ->
        AlertDialog(
            onDismissRequest = { selectedOrderForPay = null },
            confirmButton = {},
            dismissButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { selectedOrderForPay = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payment Procedure", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Transaction Context", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth())
                    Text(text = "Order Payment: ${order.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8EAF6), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋 Dine-In / Delivery Order (5.0% SST Included)", fontSize = 12.sp, color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val basePrice = order.price * order.quantity
                    val sstTax = basePrice * 0.05
                    val totalDue = basePrice + sstTax

                    DetailRow(label = "Item Subtotal", value = "RM ${String.format("%.2f", basePrice)}")
                    DetailRow(label = "SST Tax (5.0%)", value = "RM ${String.format("%.2f", sstTax)}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount Due", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("RM ${String.format("%.2f", totalDue)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF3F51B5))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            selectedOrderForPay = null
                            showSuccessDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Proceed to Pay", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cancel Transaction",
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { selectedOrderForPay = null }
                            .padding(8.dp)
                    )
                }
            }
        )
    }

    // --- DIALOG 2: PAYMENT SUCCESSFUL ---
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Awesome!", color = Color.White)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF2ECC71),
                    modifier = Modifier.size(60.dp)
                )
            },
            title = {
                Text(text = "Payment Successful!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Text(
                    text = "Your food order is fully locked in and reserved. See you there! ✨🍔",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

// =========================================================================
// 🛠️ FUNGSI KAD MAKANAN DINAMIK (MENCARI PADANAN MAKANAN SECARA AUTOMATIK)
// =========================================================================
@Composable
fun OrderFoodCard(cartItem: CartItem) {
    // Memadankan nama dari database dengan master list menu anda
    val matchedFood = masterFoodList.find { it.name.equals(cartItem.name, ignoreCase = true) }

    // Dapatkan drawable gambar dan nama kedai yang tepat. Jika tak jumpa, letak default backup.
    val finalImageResource = matchedFood?.image ?: R.drawable.bisscoff
    val finalStoreName = matchedFood?.storeName ?: "Cafe Chef Wan @ IOI City Mall"

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(70.dp)
        ) {
            Image(
                painter = painterResource(id = finalImageResource),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = cartItem.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            Text(text = finalStoreName, fontSize = 11.sp, color = Color.Gray)
            Text(text = cartItem.category, fontSize = 11.sp, color = Color.Gray)
            Text(text = "Quantity: ${cartItem.quantity}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "RM ${String.format("%.2f", cartItem.price)}",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3F51B5),
            fontSize = 16.sp
        )
    }
}


@Composable
fun TabItem(label: String, isSelected: Boolean, modifier: Modifier) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFFB02CAC) else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.height(2.dp).width(60.dp).background(Color(0xFFB02CAC)))
        }
    }
}

@Composable
fun AccountScreen(
    viewModel: MealifyViewModel,
    navController: NavHostController,
    onEditProfile: () -> Unit
) {
    val profile = viewModel.userProfile

    // PENTING: Semak status sama ada pengguna sudah log masuk ataupun belum (Guest)
    // Anda boleh sesuaikan logik ini mengikut boolean isLoggedIn di ViewModel anda
    val isLoggedIn = profile.email.isNotEmpty() && profile.email.contains("@")

    // State to track if English is selected (True = English, False = BM)
    var isEnglish by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {

        // --- 1. DYNAMIC PROFILE HEADER (PURPLE / PINK - Menyamai Gambar 2) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB02CAC)) // Warna Ungu/Pink Identikal
                .statusBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 40.dp, bottom = 40.dp)
            ) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    // JIKA LOG MASUK: Papar nama asal. JIKA BELUM: Papar Guest User (Gambar 2)
                    Text(
                        text = if (isLoggedIn) "${profile.firstName} ${profile.lastName}" else "Guest User",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = if (isLoggedIn) profile.email else "Please sign in to manage your profile",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- 2. LIST OPTIONS ---
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            // --- CUSTOM LANGUAGE TOGGLE ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFB02CAC),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Language", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "English",
                        fontSize = 12.sp,
                        color = if (isEnglish) Color(0xFFB02CAC) else Color.Gray,
                        fontWeight = if (isEnglish) FontWeight.Bold else FontWeight.Normal
                    )

                    Switch(
                        checked = !isEnglish,
                        onCheckedChange = { isEnglish = !it },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFB02CAC),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )

                    Text(
                        text = "BM",
                        fontSize = 12.sp,
                        color = if (!isEnglish) Color(0xFFB02CAC) else Color.Gray,
                        fontWeight = if (!isEnglish) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // --- SEKSYEN KLIK PROFILE YANG DIUBAH SUAI (IKUT ALIRAN PENSYARAH) ---
            ProfileOptionRow("Profile", Icons.Default.Person) {
                if (isLoggedIn) {
                    // Jika sudah log masuk sebelum ini, bawa ke skrin Edit Profile biasa
                    onEditProfile()
                } else {
                    // PENTING: Jika belum log masuk, bawa ke skrin SIGN UP dahulu seperti Gambar 3!
                    navController.navigate("profile_signup")
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            ProfileOptionRow("Saved Addresses", Icons.Default.Place) {}
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            ProfileOptionRow("Coupons", Icons.Default.Star) {}
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            ProfileOptionRow("Settings", Icons.Default.Settings) {}
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. BACK TO HOME BUTTON ---
        OutlinedButton(
            onClick = {
                navController.navigate("main_ui") {
                    popUpTo("main_ui") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            border = BorderStroke(1.dp, Color(0xFFB02CAC)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back to Home", color = Color(0xFFB02CAC), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ProfileOptionRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFFB02CAC), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}


// ====================================================================
// 🛑 KOD SKRIN LOG IN UTAMA (LOGIK IKON MATA & STRUKTUR GAMBAR 4 & 5)
// ====================================================================
@Composable
fun ProfileLoginScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Please enter your details", fontSize = 14.sp, color = Color.Gray)
        Text(text = "Welcome back", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Spacer(modifier = Modifier.height(32.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Password dengan Ikon Mata (Gambar 4 & Gambar 5)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "👁️" else "🙈",
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .padding(8.dp),
                    fontSize = 18.sp
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Forgot Password Row (Gambar 5)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = false, onCheckedChange = {})
                Text(text = "Remember for 30 days", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = "Forgot password",
                color = Color(0xFF4285F4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { /* Handle Forgot Password logic */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Butang Log Masuk Utama -> Membawa terus ke skrin menu pilihan akaun asal ("account")
        Button(
            onClick = {
                if (email.isNotEmpty() && password.length >= 4) {
                    // PEMBETULAN: Memastikan rujukan route selaras dengan nama "account" di dalam NavHost
                    navController.navigate("account") {
                        popUpTo("main_ui") { inclusive = false }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Sign in", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Butang Sign In With Google (Gambar 5)
        OutlinedButton(
            onClick = { /* Handle Google Sign In Direct */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔴 ", fontSize = 14.sp)
                Text(text = "Sign in with Google", color = Color.Black, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pautan Daftar Akaun Baru (Gambar 5)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "Sign up",
                color = Color(0xFF4285F4),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { navController.navigate("profile_signup") }
            )
        }
    }
}

// ====================================================================
// 🛑 KOD SKRIN DAFTAR AKAUN (SIGN UP - STRUKTUR GAMBAR 3)
// ====================================================================
@Composable
fun ProfileSignUpScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB02CAC))
        Text(text = "Please fill in your details to sign up", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Spacer(modifier = Modifier.height(40.dp))

        // Butang Sign Up -> Bawa ke halaman login (Welcome Back)
        Button(
            onClick = {
                if (firstName.isNotEmpty() && email.contains("@")) {
                    navController.navigate("profile_login")
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB02CAC)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Sign Up", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}




@Composable
fun FoodDetailPage(food: FoodItem, viewModel: MealifyViewModel, onBack: () -> Unit, onViewCart: (Int, Double) -> Unit) {
    // 1. ADD QUANTITY STATE
    var quantity by remember { mutableIntStateOf(1) }

    // 2. MATH FOR TOTAL AMOUNT
    val pricePerItem = food.price.replace("RM ", "").toDoubleOrNull() ?: 0.0
    val totalAmount = pricePerItem * quantity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Image Header
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

            Spacer(modifier = Modifier.weight(1f))

            // --- QUANTITY SELECTOR SECTION ---
            Text("Select Quantity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape).size(40.dp)
                ) { Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold) }

                Text(
                    text = quantity.toString(),
                    modifier = Modifier.padding(horizontal = 32.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { quantity++ },
                    modifier = Modifier.background(Color(0xFFAB15A3), CircleShape).size(40.dp)
                ) { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }

            // --- 🛠️ LAB 5 REQUIREMENT INTEGRATION ---
            Button(
                onClick = {
                    // 1. Simpan ke dalam Room Database secara automatik
                    viewModel.addItemToRoomCart(
                        name = food.name,
                        category = food.category,
                        price = pricePerItem,
                        quantity = quantity
                    )
                    // 2. Simpan juga ke memori cart lama (backwards compatibility)
                    viewModel.addToCart(food, quantity)

                    // 3. Navigasi ke skrin seterusnya
                    onViewCart(quantity, totalAmount)
                },
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
