package misk.aws2.dynamodb

import com.google.common.util.concurrent.AbstractService
import com.google.inject.Provides
import misk.ReadyService
import misk.ServiceModule
import misk.cloud.aws.AwsRegion
import misk.exceptions.dynamodb.DynamoDbExceptionMapperModule
import misk.healthchecks.HealthCheck
import misk.inject.KAbstractModule
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClient
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClientBuilder
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Install this module to have access to a DynamoDbClient.
 */
open class RealDynamoDbModule @JvmOverloads constructor(
  private val clientOverrideConfig: ClientOverrideConfiguration =
    ClientOverrideConfiguration.builder().build(),
  private val requiredTables: List<RequiredDynamoDbTable> = listOf(),
  private val endpointOverride: URI? = null,
) : KAbstractModule() {
  override fun configure() {
    requireBinding<AwsCredentialsProvider>()
    requireBinding<AwsRegion>()
    multibind<HealthCheck>().to<DynamoDbHealthCheck>()
    bind<DynamoDbService>().to<RealDynamoDbService>()
    install(ServiceModule<DynamoDbService>().enhancedBy<ReadyService>())
    install(DynamoDbExceptionMapperModule())
  }

  @Provides @Singleton
  fun providesDynamoDbClient(
    awsRegion: AwsRegion,
    awsCredentialsProvider: AwsCredentialsProvider
  ): DynamoDbClient {
    val builder = DynamoDbClient.builder()
      .region(Region.of(awsRegion.name))
      .credentialsProvider(awsCredentialsProvider)
      .overrideConfiguration(clientOverrideConfig)
    if (endpointOverride != null) {
      builder.endpointOverride(endpointOverride)
    }
    configureClient(builder)
    return builder.build()
  }

  @Provides @Singleton
  fun providesDynamoDbStreamsClient(
    awsRegion: AwsRegion,
    awsCredentialsProvider: AwsCredentialsProvider
  ): DynamoDbStreamsClient {
    val builder = DynamoDbStreamsClient.builder()
      .region(Region.of(awsRegion.name))
      .credentialsProvider(awsCredentialsProvider)
      .overrideConfiguration(clientOverrideConfig)
    if (endpointOverride != null) {
      builder.endpointOverride(endpointOverride)
    }
    configureClient(builder)
    return builder.build()
  }

  open fun configureClient(builder: DynamoDbClientBuilder) {}
  open fun configureClient(builder: DynamoDbStreamsClientBuilder) {}

  @Provides @Singleton
  fun provideRequiredTables(): List<RequiredDynamoDbTable> = requiredTables

  /** We don't currently perform any startup work to connect to DynamoDB. */
  @Singleton
  private class RealDynamoDbService @Inject constructor() : AbstractService(), DynamoDbService {
    override fun doStart() = notifyStarted()
    override fun doStop() = notifyStopped()
  }
}
