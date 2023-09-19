# KeepScreenOn

Keep Screen On allows you to add a quick settings tile, with which you can easily disable screen timeout and then restore the previous timeout value.

For example, this may be useful to you if you need the display to stay on temporarily when viewing a website or document or if your device does not have the option to set the screen timeout to never in the settings.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
alt="Get it on F-Droid"
height="80">](https://f-droid.org/packages/com.elasticrock.keepscreenon/)

**Permissions**

 - **"android.permission.WRITE_SETTINGS"** - Required to read and update system timeout
 - **"android.permission.FOREGROUND_SERVICE"** - Needed to listen for battery_low and/or screen_off actions, if you enable the feature "Restore timeout when battery is low" and/or "Restore timeout when screen is turned off"
 - **"android.permission.FOREGROUND_SERVICE_SPECIAL_USE"** - Needed to specify the appropriate foreground service type
 - **"android.permission.POST_NOTIFICATIONS"** - Needed to post notifications when foreground service is running

**Credits**

 - [**JunkFood02/Seal**](https://github.com/JunkFood02/Seal/) - The UI came from here
