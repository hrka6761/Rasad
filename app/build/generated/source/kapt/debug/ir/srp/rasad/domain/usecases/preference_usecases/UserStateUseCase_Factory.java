// Generated by Dagger (https://dagger.dev).
package ir.srp.rasad.domain.usecases.preference_usecases;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import ir.srp.rasad.domain.repositories.LocalUserDataRepo;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class UserStateUseCase_Factory implements Factory<UserStateUseCase> {
  private final Provider<LocalUserDataRepo> localUserDataRepoProvider;

  public UserStateUseCase_Factory(Provider<LocalUserDataRepo> localUserDataRepoProvider) {
    this.localUserDataRepoProvider = localUserDataRepoProvider;
  }

  @Override
  public UserStateUseCase get() {
    return newInstance(localUserDataRepoProvider.get());
  }

  public static UserStateUseCase_Factory create(
      Provider<LocalUserDataRepo> localUserDataRepoProvider) {
    return new UserStateUseCase_Factory(localUserDataRepoProvider);
  }

  public static UserStateUseCase newInstance(LocalUserDataRepo localUserDataRepo) {
    return new UserStateUseCase(localUserDataRepo);
  }
}
