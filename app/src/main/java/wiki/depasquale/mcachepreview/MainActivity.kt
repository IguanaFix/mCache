package wiki.depasquale.mcachepreview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import wiki.depasquale.mcache.BuildConfig
import wiki.depasquale.mcache.Cache
import java.util.Locale

class MainActivity : AppCompatActivity(), Consumer<User> {

    private var startTime: Long = 0
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        et.setText("diareuse")
        et.post { fab.performClick() }

        plugin.text = BuildConfig.VERSION_NAME

        fab.setOnClickListener {
            message.text = null
            user.text = null
            responseTime.text = null

            val username = et.text.toString()
            if (username.isEmpty()) {
                input.error = "Please fill this field :)"
                input.isErrorEnabled = true
            } else {
                when {
                    username.equals(
                        "clean",
                        ignoreCase = true
                    ) -> Cache.obtain(User::class.java).build().delete()
                    username.equals("removeall", ignoreCase = true) -> removeAll()
                    else -> retrieveUser(username)
                }
            }
        }
    }

    private fun removeAll() {
        startTime = System.nanoTime()
        Cache.obtain(Cache::class.java)
            .build()
            .deleteLater()
            .subscribe({ success ->
                responseTime.append(if (responseTime.text.isNotEmpty()) "\n" else "")
                responseTime.append(
                    String.format(
                        Locale.getDefault(), "%d ms",
                        (System.nanoTime() - startTime) / 1000000
                    )
                )
                message.text = if (success) "OK" else "FAILED"
            })
            .addTo(disposables)
    }

    private fun retrieveUser(username: String) {
        input.isErrorEnabled = false
        user.text = String.format("/users/%s", username)
        startTime = System.nanoTime()
        Github.user(username)
            .subscribe(this,
                Consumer<Throwable> { error ->
                    error.printStackTrace()
                    Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                })
            .addTo(disposables)
    }

    @Throws(Exception::class)
    override fun accept(user: User) {
        Logger.d("User@${user.login} accepted")
        responseTime.append(if (responseTime.text.isNotEmpty()) "\n" else "")
        responseTime.append(
            String.format(
                Locale.getDefault(), "%d ms",
                (System.nanoTime() - startTime) / 1000000
            )
        )
        message.text = Gson().toJson(user)
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}
