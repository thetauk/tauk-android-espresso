adb shell am instrument -m -w -e listener com.tauk.android.espresso.listeners.ExecutionListener -e debug false -e taukApiUrl http://localhost:5000/api/v1/session/upload -e apiToken 5WOnv-h9SsGE5XR0KcucKiKONWyA -e projectId mr9wjRTze io.aj.sample.test/androidx.test.runner.AndroidJUnitRunner