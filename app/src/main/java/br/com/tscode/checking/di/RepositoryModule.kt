package br.com.tscode.checking.di

import br.com.tscode.checking.data.repository.AccidentRepositoryImpl
import br.com.tscode.checking.data.repository.AuthRepositoryImpl
import br.com.tscode.checking.data.repository.CheckRepositoryImpl
import br.com.tscode.checking.data.repository.ProjectRepositoryImpl
import br.com.tscode.checking.data.repository.TransportRepositoryImpl
import br.com.tscode.checking.domain.repository.AccidentRepository
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.repository.ProjectRepository
import br.com.tscode.checking.domain.repository.TransportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds @Singleton
    abstract fun bindCheckRepository(impl: CheckRepositoryImpl): CheckRepository

    @Binds @Singleton
    abstract fun bindTransportRepository(impl: TransportRepositoryImpl): TransportRepository

    @Binds @Singleton
    abstract fun bindAccidentRepository(impl: AccidentRepositoryImpl): AccidentRepository
}
