# Fetch App

This is a native Android application built using Kotlin that retrieves data from the provided URL (https://fetch-hiring.s3.amazonaws.com/hiring.json) and displays a list of items to the user.

## Features
+ Fetches JSON data from the specified URL
+ Filters out items with blank or null name values
+ Groups items by listId
+ Sorts items within each group by name
+ Displays the list of items in a RecyclerView with pagination
+ Shows a progress bar while data is being loaded
  
## Requirements
+ Android Studio (Latest version recommended)
+ Android SDK (API level 33 or higher)
+ Dependencies
+ Retrofit (for network requests)
+ Gson (for JSON parsing)
+ RecyclerView (for displaying the list of items)


## Usage
+ Upon launching the app, you will see a "Result" button.
+ Click the "Result" button to navigate to the activity_results.
+ The app will fetch the first page of data and display it in a RecyclerView.
+ Each item in the list will show its id, listId, and name.
+ Scroll to the bottom of the list to load the next page of data (if available).
+ The app will automatically fetch and append the next page of data to the RecyclerView.
  
## Code Structure
+ MainActivity.kt: Contains the initial activity with a button to navigate to the Results.
+ Results.kt: Responsible for fetching the JSON data, parsing it, and displaying the list of items in a RecyclerView with pagination.

## Note
 The sorting of items within each group is based on the alphabetical order of the "name" field, as mentioned in the exercise. If numerical sorting  is required, it can be achieved
by sorting the items by "id" before displaying them.
