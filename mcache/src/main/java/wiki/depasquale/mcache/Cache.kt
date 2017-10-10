package wiki.depasquale.mcache

import android.app.Application
import android.content.Context
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import java.lang.ref.WeakReference

object Cache {

  private var contextReference: WeakReference<Context>? = null

  @JvmStatic
  fun withGlobalMode(mode: CacheMode): Cache {
    this.mode = mode
    return this
  }

  @JvmStatic
  fun with(context: Application) {
    contextReference = WeakReference(context)
    Logger.addLogAdapter(AndroidLogAdapter())
  }

  internal var mode: CacheMode = CacheMode.CACHE
    private set

  internal val context: Context
    get() {
      return contextReference?.get() ?: contextRationale()
    }

  @Throws(RuntimeException::class)
  private fun contextRationale(): Context {
    Logger.clearLogAdapters()
    Logger.addLogAdapter(AndroidLogAdapter(PrettyFormatStrategy.newBuilder()
        .showThreadInfo(true)
        .tag("diareuse/mCache")
        .build()))
    Logger.e(
        "You may have forgotten to initialize the library. Please visit my GitHub\n" +
            "[https://github.com/diareuse/mCache] for latest instructions on how to set it up.\n" +
            "Exception is thrown immediately after this message. You may have error you code as well as I can.\n" +
            "Post issues with description as accurate as possible. More info I have more code I can fix :)"
    )
    throw RuntimeException("Context must not be null.")
  }

  fun <T : Any> obtain(cls: Class<T>): FilePresenterBuilder<T> {
    return FilePresenterBuilder<T>()
        .ofClass(cls)
  }

  fun <T : Any> give(file: T): FilePresenterBuilder<T> {
    return FilePresenterBuilder<T>()
        .ofClass(file.javaClass)
        .ofFile(file)
  }
}