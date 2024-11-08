package com.example.wsc2020day2session1competitorapp

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wsc2020day2session1competitorapp.models.SessionManager
import com.example.wsc2020day2session1competitorapp.ui.theme.Wsc2020Day2Session1CompetitorAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wsc2020day2session1competitorapp.api.authUser
import com.example.wsc2020day2session1competitorapp.api.checkForCheckIn
import com.example.wsc2020day2session1competitorapp.api.getAnnouncement
import com.example.wsc2020day2session1competitorapp.api.getCompetitor
import com.example.wsc2020day2session1competitorapp.api.login
import com.example.wsc2020day2session1competitorapp.models.Announcement
import com.example.wsc2020day2session1competitorapp.models.User
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val colorScheme = lightColorScheme(
            primary = Color(0xFF005CB9),
            onPrimary = Color.White,
            // Add other color customizations if needed
        )
        sessionManager = SessionManager(this)




        setContent {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = androidx.compose.material3.Typography(),
                content = {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                context = this@MainActivity,
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                context = this@MainActivity
                            )
                        }

                    }


                }
            )
        }
    }
    override fun onStop() {
        super.onStop()
        sessionManager.clearSession()
    }
}


@Composable
fun HomeScreen(navController: NavController, context: Context) {
    var selectedTabIndex by remember { mutableStateOf(0) } // Default to "Announcements" tab
    val tabs = listOf("AnnouncementsScreen", "ProfileScreen")
    val sessionManager = SessionManager(context)
    var alert by remember { mutableStateOf(false) }
    var validUser by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val authUser = authUser()
        authUser.postFunction(context,
            onSuccess = {
                validUser = true
            },
            onFailure = {
                Toast.makeText(
                    context,
                    "Authentication Failed, Please Login Again",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate("login")
            })

        while (validUser)
        {
            val checkForCheckIn = checkForCheckIn()
            val isCheckedIn = checkForCheckIn.getFunction(sessionManager.getSession()!!.userId)

            if (isCheckedIn != null) {
                if (isCheckedIn) {
                    alert = true
                    break
                }
            }
        }
    }

    if(alert)
    {
        AlertDialog(
            onDismissRequest = { alert = false },
            title = { Text("Check In Completed") },
            text = { Text("You Have Checked In") },
            confirmButton = {
                Button(
                    onClick = {
                        alert = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxWidth()
                .height(90.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Text(
                text = "Administrator App",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp)
            )
            Button(
                onClick = {
                    sessionManager.clearSession()
                    navController.navigate("login")
                }
            ) {
                Text(text = "Logout", color = Color.White)
            }
        }

        when (selectedTabIndex) {
            0 -> AnnouncementsScreen(navController, context)
            1 -> ProfileScreen(navController, context)
        }

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
    }
}

@Composable
fun AnnouncementsScreen(navController: NavController, context: Context)  {
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var validUser by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        while(true)
        {
            val getAnnouncement = getAnnouncement()

            CoroutineScope(Dispatchers.IO).launch {
                val announcementList = getAnnouncement.getFunction()
                if (announcementList != null) {
                    announcements = announcementList.sortedByDescending { it.announcementDate }
                }
            }
            delay(1000)
        }

    }

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Announcements", style = MaterialTheme.typography.headlineMedium)
        LazyColumn {
            items(announcements) { announcement ->
                AnnouncementCard(announcement = announcement)
            }
        }
    }
}
@Composable
fun AnnouncementCard(announcement: Announcement) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = announcement.announcementTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = announcement.announcementDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = announcement.announcementDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@Composable
fun ProfileScreen(navController: NavController, context: Context) {
    val fullName = remember { mutableStateOf("") } // Replace with actual full name

    var competitorId by remember { mutableStateOf("") }
    val qrCodeBitmap = generateQRCode(competitorId)

    val session = SessionManager(context).getSession()
    var validUser by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
         competitorId = session!!.userId
        CoroutineScope(Dispatchers.IO).launch {

            val authUser = authUser()
            authUser.postFunction(context,
                onSuccess = {
                    validUser = true
                },
                onFailure = {
                    Toast.makeText(
                        context,
                        "Authentication Failed, Please Login Again",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate("login")
                })

            if(validUser)
            {
                val competitor = getCompetitor().getFunction(competitorId)
                if (competitor != null) {
                    fullName.value = "${competitor.fullName}"
                }
            }

        }

    }



    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = fullName.value, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        qrCodeBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Competitor ID: $competitorId", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Full Name: $fullName.value", style = MaterialTheme.typography.bodyMedium)
    }
}

fun generateQRCode(text: String): Bitmap? {
    return try {
        val size = 512 // Size of the QR code
        val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

@Composable
fun LoginScreen(navController: NavController, context: Context) {

    var alert by remember { mutableStateOf(false) }
    var login by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if (alert) {
        AlertDialog(
            onDismissRequest = { alert = false },
            title = { Text("Invalid") },
            text = { Text("Invalid Login Details") },
            confirmButton = {
                Button(
                    onClick = {
                        alert = false

                    }
                ) {
                    Text("OK")
                }
            }
        )
    }


    if (login) {
        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val user = User(email, password)
                val loginService = login()
                loginService.postFunction(user, context,
                    onSuccess = {
                        navController.navigate("home")
                    },
                    onFailure = {
                        alert = true
                        email   = ""
                        password = ""
                    })

            }
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.width(280.dp),
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val user = User(email, password)
                    val loginService = login()
                    loginService.postFunction(user, context,
                        onSuccess = {
                            navController.navigate("home")
                        },
                        onFailure = {
                            alert = true
                            email   = ""
                            password = ""
                        })

                }
            }
        ) {
            Text(text = "Login")
        }
    }


}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Wsc2020Day2Session1CompetitorAppTheme {
        Greeting("Android")
    }
}