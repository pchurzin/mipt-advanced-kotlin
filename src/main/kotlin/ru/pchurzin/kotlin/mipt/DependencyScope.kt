package ru.pchurzin.kotlin.mipt

import kotlinx.coroutines.*

interface DependencyScope {
    fun <T> asyncDependency(block: suspend CoroutineScope.() -> T): Deferred<T>
    fun launchDependency(block: suspend CoroutineScope.() -> Unit): Job
    fun <T> dAsync(block: suspend CoroutineScope.() -> T): Deferred<T>
    fun dLaunch(block: suspend CoroutineScope.() -> Unit): Job
}

fun <T> CoroutineScope.dependentAsync(
    block: DependencyScope.() -> Deferred<T>
): Deferred<T> = block(DefaultDependencyScope(this))

fun CoroutineScope.dependentLaunch(
    block: DependencyScope.() -> Job
): Job = block(DefaultDependencyScope(this))

private class DefaultDependencyScope(private val scope: CoroutineScope) : DependencyScope {

    private val dependencies = mutableSetOf<Job>()

    override fun <T> asyncDependency(block: suspend CoroutineScope.() -> T): Deferred<T> {
        val d = scope.async(start = CoroutineStart.LAZY, block = block)
        dependencies.add(d)
        return d
    }

    override fun launchDependency(block: suspend CoroutineScope.() -> Unit): Job {
        val d = scope.launch(start = CoroutineStart.LAZY, block = block)
        dependencies.add(d)
        return d
    }

    override fun <T> dAsync(block: suspend CoroutineScope.() -> T): Deferred<T> =
        scope.async(start = CoroutineStart.LAZY) {
            dependencies.forEach(Job::start)
            block()
        }

    override fun dLaunch(block: suspend CoroutineScope.() -> Unit): Job =
        scope.launch(start = CoroutineStart.LAZY) {
            dependencies.forEach(Job::start)
            block()
        }
}
