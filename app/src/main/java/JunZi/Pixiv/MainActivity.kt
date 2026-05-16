package JunZi.Pixiv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import JunZi.Pixiv.ui.PuxivApp

class MainActivity : ComponentActivity() {
    private val viewModel: PixivViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PuxivApp(viewModel = viewModel)
        }
        handlePixivCallback(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePixivCallback(intent)
    }

    private fun handlePixivCallback(intent: Intent?) {
        val code = intent?.data?.pixivAuthCode() ?: return
        viewModel.exchangeAuthCode(code)
    }
}

private fun Uri.pixivAuthCode(): String? {
    if (scheme != "pixiv" && scheme != "pixiv-inner") return null
    return getQueryParameter("code")?.takeIf { it.isNotBlank() }
}
