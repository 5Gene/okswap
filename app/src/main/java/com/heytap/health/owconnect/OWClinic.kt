package com.heytap.health.owconnect

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import osp.sparkj.okswap.bluetooth.BtStateBox


interface ComposableData {
    @Composable
    fun Content()
}

class BasicMsg(val title: String = "", val desc: String = "", val ui: (@Composable () -> Unit)? = null) : ComposableData {
    @Composable
    override fun Content() {
        Box(modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp)) {
            ui?.invoke() ?: Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title)
                Text(text = desc, style = MaterialTheme.typography.caption)
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@ExperimentalLayoutApi
@Preview
@Composable
//fun OWClinic(viewModel: OWConnectViewModel = viewModel(modelClass = OWConnectViewModel::class.java)) {
fun OWClinic(viewModel: OWClinicViewModel = OWClinicViewModel()) {
    BtStateBox { btOpened, _ ->
        if (!btOpened) {
            return@BtStateBox
        }
        Column(modifier = Modifier.padding(16.dp)) {
            BasicInfoCard(viewModel)

            Spacer(modifier = Modifier.size(10.dp))

            DoctorCard(viewModel)
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun DoctorCard(viewModel: OWClinicViewModel) {
    val cornerSize = 10.dp
    val dogState by OWWatchDog.dog.collectAsState()
    val failReason = dogState.failReason()
    if (failReason != null) {
        Text(text = "连接诊断:异常 [$failReason]", style = MaterialTheme.typography.h6, color = Color.Red)

        Spacer(modifier = Modifier.size(6.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(shape = RoundedCornerShape(cornerSize), color = Color.White)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            color = Color.Transparent
        ) {
            LaunchedEffect(key1 = 1) {
                viewModel.connectDiagnose()
            }
            val doctor by viewModel.doctor.collectAsState(initial = null)
//                val count = 0
//                AnimatedContent(
//                    targetState = count,
//                    transitionSpec = {
//                        // Compare the incoming number with the previous number.
//                        if (targetState > initialState) {
//                            // If the target number is larger, it slides up and fades in
//                            // while the initial (smaller) number slides up and fades out.
//                            slideInVertically { height -> height } + fadeIn() with
//                                    slideOutVertically { height -> -height } + fadeOut()
//                        } else {
//                            // If the target number is smaller, it slides down and fades in
//                            // while the initial number slides down and fades out.
//                            slideInVertically { height -> -height } + fadeIn() with
//                                    slideOutVertically { height -> height } + fadeOut()
//                        }.using(
//                            // Disable clipping since the faded slide-in/out should
//                            // be displayed out of bounds.
//                            SizeTransform(clip = false)
//                        )
//                    }
//                ) { targetCount ->
//                    Text(text = "$targetCount")
//                }
            doctor?.let {
                AnimatedContent(targetState = it, label = "",
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { width -> width }) + fadeIn() with slideOutHorizontally(targetOffsetX = { width -> -width }) + fadeOut()
                    }) { doctor ->
                    doctor.Suggestion()
                }
            }
        }

    } else if (dogState.isSucceed()) {
        Text(text = "连接诊断: [已连接]", style = MaterialTheme.typography.h6, color = Color.Green)
        Spacer(modifier = Modifier.size(6.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(shape = RoundedCornerShape(cornerSize), color = Color.White)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            color = Color.Transparent
        ) {

        }
    } else {
        Text(text = "连接诊断: [连接中]", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.size(6.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(shape = RoundedCornerShape(cornerSize), color = Color.White)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            color = Color.Transparent
        ) {
            LinearProgressIndicator(progress = dogState.events.size / 6f, color = Color.Green, backgroundColor = Color.LightGray)
            //显示连接进度
            Column {
                dogState.events.forEach {
                    Text(text = it.log())
                }
            }
        }
    }
}

@ExperimentalLayoutApi
@Composable
private fun BasicInfoCard(viewModel: OWClinicViewModel) {
    val cornerSize = 10.dp
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(cornerSize), color = Color.White)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        ProvideTextStyle(
            value = TextStyle(
                fontSize = 16.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
        ) {
            FlowRow(horizontalArrangement = Arrangement.SpaceAround) {
                viewModel.basicMsgs.forEach { basicMsg -> basicMsg.Content() }
            }
        }
//            CompositionLocalProvider(
//                LocalTextStyle provides TextStyle(
//                    fontSize = 16.sp,
//                    color = Color.Red,
//                    textAlign = TextAlign.Center
//                )
//            ) {
//                FlowRow(horizontalArrangement = Arrangement.SpaceAround) {
//                    viewModel.basicMsgs.forEach { basicMsg -> basicMsg.Content() }
//                }
//            }

    }
}