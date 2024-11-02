@file:OptIn(ExperimentalLayoutApi::class)

package osp.sparkj.more

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import osp.sparkj.okswap.bluetooth.BtStateBox
import osp.sparkj.ui.theme.MoreTheme


class MainActivity : ComponentActivity() {


    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val flow: Flow<String>? = null
//        flow?.collectAsState()
//        requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 0)
//        bluetoothLifecycle {
//            bluetoothScope.launch {  }
//        }
//        bluetoothScope.launch {  }
//        lifecycleScope.launch {  }

        setContent {

//            val openBluetooth = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
//                result.resultCode
//            }
//
//            openBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))

            MoreTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        Greeting("Android")

                        BtStateBox { state, click ->
                            Spacer(modifier = Modifier.size(20.dp))
                            Button(onClick = click) {
                                Text(text = state.toString())
                            }
                        }
                    }
                }
            }
        }
    }


}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MoreTheme {
//        OkHttpClient()
//        Interceptor
        Greeting("Android")
    }
}