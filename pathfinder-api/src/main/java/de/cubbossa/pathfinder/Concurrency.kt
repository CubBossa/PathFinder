package de.cubbossa.pathfinder

import kotlinx.coroutines.*

val scope = CoroutineScope(CoroutineName("PathFinderCoroutine"))

fun launchIO(run: suspend CoroutineScope.() -> Unit) {
    scope.launch(Dispatchers.IO, CoroutineStart.DEFAULT, run)
}

fun launchCalc(run: suspend CoroutineScope.() -> Unit) {
    scope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, run)
}

fun launchMain(run: suspend CoroutineScope.() -> Unit) {
    scope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, run)
}