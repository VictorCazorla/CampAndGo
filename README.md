# 🧪 Camp&Go - Development Branch (dev)

This document is intended for developers and collaborators actively working on the **dev** branch of the **Camp&Go** project. Here you will find technical details, setup instructions, internal conventions, and necessary dependencies to effectively contribute to the project.

---

## ⚙️ Environment Setup

### Requirements

- **Android Studio Giraffe or higher**  
- **JDK 17**  
- Minimum SDK: API 24 (Android 7.0)  
- Internet connection (for Firebase dependencies and external APIs)

### API Keys and Sensitive Configuration

To protect sensitive data, **files such as google-services.json and secrets.xml are not included in the repository**.

#### Steps to configure locally:

1. Create the file `app/google-services.json` from the [Firebase Console](https://console.firebase.google.com/).  
2. Manually create the file `app/src/main/res/values/secrets.xml` with the following content:

```xml
<resources>
    <string name="google_maps_key">YOUR_GOOGLE_MAPS_KEY</string>
    <string name="open_weather_key">YOUR_OPENWEATHER_KEY</string>
</resources>
