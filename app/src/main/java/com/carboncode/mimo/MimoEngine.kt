package com.carboncode.mimo

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 管理 mimocode 子进程：启动、读写 stdin/stdout、销毁
 */
class MimoEngine(private val context: Context) {

    enum class State { STOPPED, STARTING, RUNNING, ERROR }

    private val _state = MutableStateFlow(State.STOPPED)
    val state: StateFlow<State> = _state

    private val _output = MutableStateFlow("")
    val output: StateFlow<String> = _output

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val pendingOutput = StringBuilder()
    private val maxChars = 50000

    /** 从 assets 提取 mimocode 二进制到 files 目录 */
    private fun extractBinary(): File {
        val binFile = File(context.filesDir, "mimocode")
        if (binFile.exists() && binFile.canExecute()) return binFile

        context.assets.open("mimocode").use { input ->
            FileOutputStream(binFile).use { output ->
                input.copyTo(output)
            }
        }
        binFile.setExecutable(true)
        return binFile
    }

    /** 启动 mimocode 子进程 */
    fun start() {
        if (_state.value == State.RUNNING) return
        _state.value = State.STARTING

        scope.launch {
            try {
                val binary = extractBinary()
                val pb = ProcessBuilder(binary.absolutePath)
                    .directory(context.filesDir)
                    .redirectErrorStream(true)

                process = pb.start()
                writer = process?.outputStream?.bufferedWriter(Charsets.UTF_8)

                _state.value = State.RUNNING

                // 读取 stdout
                process?.inputStream?.bufferedReader(Charsets.UTF_8)?.let { reader ->
                    while (isActive) {
                        val line = reader.readLine() ?: break
                        appendOutput(line + "\n")
                    }
                }
            } catch (e: Exception) {
                appendOutput("\n[错误] ${e.message}\n")
                _state.value = State.ERROR
            }
        }
    }

    /** 发送消息到 mimocode */
    fun send(message: String) {
        if (_state.value != State.RUNNING) return
        scope.launch {
            try {
                writer?.write(message)
                writer?.newLine()
                writer?.flush()
            } catch (e: Exception) {
                appendOutput("\n[发送失败] ${e.message}\n")
            }
        }
    }

    /** 停止 mimocode */
    fun stop() {
        scope.launch {
            try {
                writer?.close()
                process?.destroy()
            } catch (_: Exception) {}
            _state.value = State.STOPPED
        }
    }

    private fun appendOutput(text: String) {
        pendingOutput.append(text)
        if (pendingOutput.length > maxChars) {
            pendingOutput.delete(0, pendingOutput.length - maxChars)
        }
        _output.value = pendingOutput.toString()
    }

    fun destroy() {
        scope.cancel()
        stop()
    }
}
