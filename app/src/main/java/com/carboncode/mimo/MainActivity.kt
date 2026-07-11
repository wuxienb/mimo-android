package com.carboncode.mimo

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var engine: MimoEngine
    private lateinit var adapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputField: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var statusView: View

    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        engine = MimoEngine(applicationContext)
        adapter = ChatAdapter()

        recyclerView = findViewById(R.id.recycler_chat)
        recyclerView.adapter = adapter

        inputField = findViewById(R.id.input_message)
        sendButton = findViewById(R.id.btn_send)
        statusView = findViewById(R.id.status_bar)

        sendButton.setOnClickListener { sendMessage() }
        inputField.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
        }

        // 监听引擎输出
        lifecycleScope.launch {
            engine.output.collectLatest { output ->
                // 每收到新行就刷新最后一条 AI 消息
                val lastMsg = messages.lastOrNull { !it.isUser }
                if (lastMsg != null) {
                    val idx = messages.indexOf(lastMsg)
                    messages[idx] = lastMsg.copy(content = output)
                    adapter.submitList(messages.toList())
                } else {
                    messages.add(ChatMessage(output, isUser = false))
                    adapter.submitList(messages.toList())
                }
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }

        // 监听引擎状态
        lifecycleScope.launch {
            engine.state.collectLatest { state ->
                val text = when (state) {
                    MimoEngine.State.STOPPED -> "已停止"
                    MimoEngine.State.STARTING -> "启动中..."
                    MimoEngine.State.RUNNING -> "运行中"
                    MimoEngine.State.ERROR -> "错误"
                }
                statusView.isEnabled = state == MimoEngine.State.RUNNING
            }
        }

        // 启动引擎
        engine.start()
    }

    private fun sendMessage() {
        val text = inputField.text.toString().trim()
        if (text.isEmpty()) return

        messages.add(ChatMessage(text, isUser = true))
        adapter.submitList(messages.toList())
        recyclerView.scrollToPosition(messages.size - 1)
        inputField.text.clear()

        engine.send(text)
    }

    override fun onDestroy() {
        engine.destroy()
        super.onDestroy()
    }
}
