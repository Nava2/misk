package misk.moshi

import com.google.inject.Provides
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import misk.inject.KAbstractModule
import misk.moshi.adapters.BigDecimalAdapter
import misk.moshi.okio.ByteStringAdapter
import misk.moshi.time.InstantAdapter
import misk.moshi.time.LocalDateAdapter
import wisp.moshi.buildMoshi
import java.util.Date
import javax.inject.Singleton
import com.squareup.wire.WireJsonAdapterFactory as WireOnlyJsonAdapterFactory
import misk.moshi.wire.WireMessageAdapter as MiskOnlyMessageAdapter

/**
 * For service setup, prefer to install [misk.MiskCommonServiceModule] over installing [MoshiModule]
 * directly.
 */
class MoshiModule @JvmOverloads constructor(
  private val useWireToRead: Boolean = false,
  private val useWireToWrite: Boolean = false
) : KAbstractModule() {
  override fun configure() {
    val wireFactory = WireOnlyJsonAdapterFactory()
    val miskFactory = MiskOnlyMessageAdapter.Factory()
    install(
      MoshiAdapterModule(
        MigratingJsonAdapterFactory(
          reader = if (useWireToRead) wireFactory else miskFactory,
          writer = if (useWireToWrite) wireFactory else miskFactory
        )
      )
    )

    install(MoshiAdapterModule(ByteStringAdapter))
    install(MoshiAdapterModule<Date>(Rfc3339DateJsonAdapter()))
    install(MoshiAdapterModule(InstantAdapter))
    install(MoshiAdapterModule(BigDecimalAdapter))
    install(MoshiAdapterModule(LocalDateAdapter))
  }

  @Provides
  @Singleton
  fun provideMoshi(
    @MoshiJsonAdapter jsonAdapters: List<Any>
  ): Moshi {
    return buildMoshi(jsonAdapters)
  }
}
