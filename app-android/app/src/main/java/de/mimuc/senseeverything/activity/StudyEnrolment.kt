package de.mimuc.senseeverything.activity

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.volley.Response
import de.mimuc.senseeverything.activity.ui.theme.AppandroidTheme
import de.mimuc.senseeverything.api.ApiClient
import de.mimuc.senseeverything.data.DataStoreManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StudyEnrolment : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppandroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EnrolmentScreen()
                }
            }
        }
    }
}

class EnrolmentViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _isEnrolled = MutableStateFlow(false)
    val isEnrolled: StateFlow<Boolean> get() = _isEnrolled

    private val _participantId = MutableStateFlow("")
    val participantId: StateFlow<String> get() = _participantId

    init {
        viewModelScope.launch {
            combine(
                    dataStoreManager.tokenFlow,
                    dataStoreManager.participantIdFlow
            ) { token, participantId ->
                if (token.isNotEmpty()) {
                    _isEnrolled.value = true
                }

                if (participantId.isNotEmpty()) {
                    _participantId.value = participantId
                }
            }.collect()
        }
    }

    fun performAction(context: Context, text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val client = ApiClient.getInstance(context)
            val body = JSONObject()
            body.put("enrolmentKey", text)

            val response = suspendCoroutine { continuation ->
                client.post("https://siapi.timweiss.dev/v1/enrolment", body,
                    { response ->
                        continuation.resume(response)
                    },
                    { error ->
                        continuation.resume(null)
                    })
            }

            if (response == null) {
                _isEnrolled.value = false
                _isLoading.value = false
            } else {
                val token = response.getString("token")
                val participantId = response.getString("participantId")

                _isLoading.value = false
                _isEnrolled.value = true
                _participantId.value = participantId

                Log.d("EnrolmentViewModel", "participantId: $participantId")

                dataStoreManager.saveTokenAndParticipantId(token, participantId)
            }
        }
    }

    fun removeEnrolment() {
        viewModelScope.launch {
            dataStoreManager.saveToken("")
            dataStoreManager.saveParticipantId("")
            _isEnrolled.value = false
        }
    }
}

@Composable
fun EnrolmentScreen(viewModel: EnrolmentViewModel = viewModel()) {
    val textState = remember { mutableStateOf("") }
    val isLoading = viewModel.isLoading.collectAsState()
    val isEnrolled = viewModel.isEnrolled.collectAsState()
    val participantId = viewModel.participantId.collectAsState()
    val context = LocalContext.current

    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.Top
    ) {
        if (!isEnrolled.value) {
            TextField(
                    value = textState.value,
                    onValueChange = { textState.value = it },
                    label = { Text("Enrolment Key") },
                    modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading.value) {
                CircularProgressIndicator()
            } else {
                Button(
                        onClick = {
                            viewModel.performAction(context, textState.value)
                        },
                        modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enrol in Study")
                }
            }
        } else {
            Icon(Icons.Rounded.Check, contentDescription = "success!", modifier = Modifier.size(32.dp))
            Text("Enrolment Successful!")
            Text("Participant ID: ${participantId.value}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                    onClick = {
                        viewModel.removeEnrolment()
                    },
                    modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove Enrolment")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppandroidTheme {
        EnrolmentScreen()
    }
}