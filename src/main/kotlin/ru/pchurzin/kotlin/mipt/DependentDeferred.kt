package ru.pchurzin.kotlin.mipt

import kotlinx.coroutines.*
import kotlin.time.measureTime

fun main(): Unit = runBlocking {

    println("========== dependentLaunch ==========")
    val job1 = dependentLaunchExample()
    job1.start()
    job1.join()

    println("========== dependentAsync ==========")
    val deferred1 = dependentAsyncExample()
    println("Deferred = ${deferred1.await()}")

    val calculation1: suspend CoroutineScope.() -> String = {
        println("a started")
        delay(200)
        println("a finished")
        "calculation1"
    }

    val calculation2: suspend CoroutineScope.() -> Int = {
        println("b started")
        delay(300)
        println("b finished")
        42
    }

    println("========== regular ==========")
    val regularDuration = measureTime {
        regularExample(calculation1, calculation2)
    }

    println("========== dependent ==========")
    val dependentDuration = measureTime {
        val d = dependentExample(calculation1, calculation2)
        d.start()
        d.await()
    }

    val timeResults = """
        ========== time results =========
          regular: $regularDuration
        dependent: $dependentDuration
    """.trimIndent()

    println(timeResults)
}

fun CoroutineScope.dependentLaunchExample() = dependentLaunch {
    val a = launchDependency {
        println("a started")
        delay(300)
        println("a finished")
    }

    val b = asyncDependency {
        println("b started")
        delay(200)
        println("b finished")
        "b"
    }

    dLaunch {
        println(b.await())
    }
}

fun CoroutineScope.dependentAsyncExample() = dependentAsync {
    val a = launchDependency {
        println("a started")
        delay(300)
        println("a finished")
    }

    val b = asyncDependency {
        println("b started")
        delay(200)
        println("b finished")
        "b"
    }

    dAsync {
        b.await()
    }
}

suspend fun regularExample(calc1: suspend CoroutineScope.() -> String, calc2: suspend CoroutineScope.() -> Int) =
    coroutineScope {
        val a = async(start = CoroutineStart.LAZY, block = calc1)
        val b = async(start = CoroutineStart.LAZY, block = calc2)
        println("calc1: ${a.await()}")
        println("calc2: ${b.await()}")
    }


fun CoroutineScope.dependentExample(
    calc1: suspend CoroutineScope.() -> String,
    calc2: suspend CoroutineScope.() -> Int
) =
    dependentAsync {
        val a = asyncDependency(calc1)
        val b = asyncDependency(calc2)
        dAsync {
            println("calc1: ${a.await()}")
            println("calc2: ${b.await()}")
        }
    }
