package misk.crypto

import com.google.crypto.tink.KmsClient
import com.google.crypto.tink.integration.awskms.AwsKmsClient
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient
import com.google.inject.Provides
import com.google.inject.Singleton
import misk.inject.KAbstractModule
import javax.inject.Qualifier

/**
 * AWS specific KMS client module.
 * Currently uses a file path to a JSON credentials file to initialize the client.
 * If no file is provided, tries to initialize the client using the default
 * credentials path as specified in [AwsKmsClient.withDefaultCredentials]
 */
class AwsKmsClientModule @JvmOverloads constructor(private val credentialsPath: String? = null) :
  KAbstractModule() {
  // TODO: Allow initializing an AWS KMS client with a credentials provider
  // once tink supports it: https://github.com/google/tink/pull/184
  @Provides @Singleton
  fun getKmsClient(): KmsClient = credentialsPath?.let { AwsKmsClient().withCredentials(it) }
    ?: AwsKmsClient().withDefaultCredentials()
}

/**
 * GCP specific KMS client module.
 * Uses a file path to a JSON credentials file to initialize the client.
 * * If no file is provided, tries to initialize the client using the default
 * credentials path as specified in [GcpKmsClient.withDefaultCredentials]
 */
class GcpKmsClientModule @JvmOverloads constructor(private val credentialsPath: String? = null) :
  KAbstractModule() {
  @Provides @Singleton
  fun getKmsClient(): KmsClient = credentialsPath?.let { GcpKmsClient().withCredentials(it) }
    ?: GcpKmsClient().withDefaultCredentials()
}

/**
 * This annotation is used to specify which [com.amazonaws.services.kms.AWSKMS]
 * instance should be used by misk to construct a [KmsClient] and communicate with the KMS service
 */
@Qualifier
@Target(
  AnnotationTarget.FIELD,
  AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class MiskAWSKMS

/**
 * This annotation is used to specify the [KmsClient] that's
 * being used by misk to load encryption keys
 */
@Qualifier
@Target(
  AnnotationTarget.FIELD,
  AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class MiskKmsClient
