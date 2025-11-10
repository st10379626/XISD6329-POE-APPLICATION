package com.example.nunosrealtyapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.nunosrealtyapp.data.repository.AuthRepository
import com.example.nunosrealtyapp.data.repository.BookingRepository
import com.example.nunosrealtyapp.data.repository.ComplaintRepository
import com.example.nunosrealtyapp.data.repository.PropertyRepository
import com.example.nunosrealtyapp.data.repository.ApplicationRepository
import com.example.nunosrealtyapp.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): UserRepository {
        return UserRepository(auth, firestore, storage)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepository(auth, firestore)
    }

    @Provides
    @Singleton
    fun providePropertyRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): PropertyRepository {
        return PropertyRepository(firestore, storage)
    }

    @Provides
    @Singleton
    fun provideBookingRepository(
        firestore: FirebaseFirestore
    ): BookingRepository {
        return BookingRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideComplaintRepository(
        firestore: FirebaseFirestore
    ): ComplaintRepository {
        return ComplaintRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideApplicationRepository(
        firestore: FirebaseFirestore
    ): ApplicationRepository {
        return ApplicationRepository(firestore)
    }
}