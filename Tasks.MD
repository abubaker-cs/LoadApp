# TODO List

## Main Screen

### Radio List

Create a radio list of the following options where one of them can be selected for downloading:

1. Glide - https://github.com/bumptech/glide
2. Udacity - https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter
3. Retrofit - https://github.com/square/retrofit

### Loading Button (Custom)

1. Create a custom loading button by extending View class and assigning custom attributes to it
2. Animate properties of the custom button once it’s clicked
3. Add the custom button to the main screen, set on click listener and call download() function with
   selected Url

## No Option Selected

If there is no selected option, display a Toast to let the user know to select one.

## Notification

1. Once the download is complete, send a notification with custom style and design
2. Add a button with action to the notification, that opens a detailed screen of a downloaded
   repository

## Detail Activity

1. Create the details screen and display the name of the repository and status of the download
2. Use declarative XML with motionLayout to coordinate animations across the views on the detail
   screen
3. Add a button to the detail screen to return back to the main screen.