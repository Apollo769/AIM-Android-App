# A.I.M. - Agile Inventory Management

## Overview

A.I.M. (Agile Inventory Management) is an Android inventory management application originally developed in CS-360: Mobile Architecture and Programming and later enhanced as part of the CS-499 Computer Science Capstone. The application allows users to create accounts, securely authenticate, manage inventory items, and track inventory data through a cloud-based database.

## Features

- User account creation and authentication
- Add, update, and delete inventory items
- Search inventory items by name
- Sort inventory by name or quantity
- Filter low-stock inventory items
- Cloud-based inventory storage
- SMS notification permission management

## Technologies Used

- Java
- Android Studio
- Firebase Authentication
- Firebase Firestore
- RecyclerView
- DiffUtil
- Material Design Components

## Architecture

The application follows the Model-View-ViewModel (MVVM) architectural pattern and utilizes a repository layer to separate business logic, user interface components, and database operations. This structure improves maintainability, scalability, and separation of concerns.

## Algorithms & Data Structures

The application utilizes ArrayLists as its primary data structure for inventory management. Search, sorting, and filtering functionality were implemented to improve inventory organization and usability. RecyclerView performance was enhanced through DiffUtil to update only modified rows instead of refreshing the entire inventory list.

## Database & Authentication

Firebase Authentication is used to securely manage user accounts and sign-in functionality. Inventory data is stored within Firebase Firestore and isolated by authenticated user accounts through Firestore security rules to ensure users can only access their own inventory records.

## Installation

- Clone the repository.
- Open the project in Android Studio.
- Configure Firebase Authentication and Firestore.
- Build and run the application on an Android device or emulator.

## Future Enhancements

Potential future enhancements include password reset functionality, user sign-out options, expanded inventory reporting, advanced search capabilities, and additional inventory notification features.

## Link to ePortfolio

Additional project documentation, enhancement narratives, screenshots, and reflections can be found within the accompanying ePortfolio.
