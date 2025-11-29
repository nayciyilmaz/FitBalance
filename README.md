![Logo](https://github.com/nayciyilmaz/FitBalance/blob/main/proje_edited.png?raw=true)

A modern Android fitness and nutrition application built with Jetpack Compose and Firebase that allows users to get personalized meal plans powered by AI, track their daily nutrition, and monitor their health progress.

## Features
AI-Powered Meal Planning: Generate personalized daily meal plans using Google Gemini API based on your health metrics and fitness goals
User Authentication: Secure login and registration with Firebase Authentication
Daily Meal Tracking: Track breakfast, lunch, and dinner with detailed calorie information
Calorie Counter: Monitor total daily calorie intake with detailed meal breakdowns
Reading Statistics: View your progress with weight tracking and calorie statistics
Smart Notifications: Receive automatic reminders for meals and water intake throughout the day
Meal Customization: Edit, modify, or regenerate meal plans to match your preferences
Meal Sharing: Share your meal plans with friends and family
Personal Profile: Manage your personal health information and fitness goals
Material 3 Design: Modern UI with Material Design 3 components

## Frontend
Jetpack Compose - Modern Android UI toolkit
Material 3 - Latest Material Design components
Navigation Compose - Type-safe navigation between screens
Coil/Image Loading - Efficient image loading and caching

## Backend & Data
Firebase Firestore - NoSQL cloud database for storing meal plans and user data
Firebase Authentication - Secure user authentication service
Google Gemini API - AI-powered meal plan generation
Retrofit - HTTP client for API calls
Firebase Cloud Messaging - Push notifications

## Architecture & DI
Dagger Hilt - Dependency injection
MVVM Pattern - Model-View-ViewModel architecture
ViewModel - UI-related data holder
Repository Pattern - Data layer abstraction
Coroutines - Asynchronous programming
StateFlow & LiveData - Reactive data management

## Additional Libraries
Kizitonwose Calendar - Calendar component for date selection
Jetpack Navigation - Navigation framework
Jetpack Lifecycle - Lifecycle-aware components

## Design & UI
Modern Compose UI: Built entirely with Jetpack Compose
Material 3 Design: Follows latest Material Design guidelines
Responsive Layout: Optimized for all Android screen sizes
Color Scheme: Green and Blue palette for health/wellness theme
Smooth Animations: Transitions and animations for better UX
Dark Mode Support: Adaptive theme switching

## Google Gemini API
Generates personalized meal plans based on user metrics
Considers fitness goals and dietary preferences
Suggests Turkish cuisine items
Regenerates meals to provide variety
