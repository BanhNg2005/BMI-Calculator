# Firebase Authentication Setup Guide

## Issues Fixed

### 1. Missing Internet Permissions ✅
- Added `INTERNET` and `ACCESS_NETWORK_STATE` permissions to AndroidManifest.xml
- These are required for Firebase to communicate with the server

### 2. Missing Google Web Client ID ⚠️ (NEEDS YOUR ACTION)
- Added placeholder `default_web_client_id` in strings.xml
- **You MUST replace this with your actual Web Client ID from Firebase Console**

### 3. Missing Facebook App ID ⚠️ (NEEDS YOUR ACTION)
- Added placeholder Facebook App ID in strings.xml
- **You MUST get this from Facebook Developer Console and update it**

### 4. Empty OAuth Client in google-services.json ⚠️ (CRITICAL)
- Your `google-services.json` has an empty `oauth_client` array
- This means Google Sign-In will NOT work

## How to Fix Google Sign-In

### Step 1: Get Your Web Client ID from Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **bmicalculatordb**
3. Click on **Project Settings** (gear icon)
4. Go to **Your apps** section
5. Find your Android app
6. Scroll down to **SDK setup and configuration**
7. Click **Download google-services.json** (to get the latest version)
8. Replace your current `google-services.json` with the new one

### Step 2: Enable Google Sign-In in Firebase

1. In Firebase Console, go to **Authentication** → **Sign-in method**
2. Click on **Google** provider
3. Click **Enable**
4. Set a support email
5. Click **Save**

### Step 3: Get the Web Client ID

After enabling Google Sign-In:
1. Go to **Project Settings** → **Your apps**
2. You'll see a **Web Client ID** listed under OAuth 2.0 Client IDs
3. Copy that Web Client ID (looks like: `703565416770-xxxxxxxxx.apps.googleusercontent.com`)

### Step 4: Update strings.xml

Open `app/src/main/res/values/strings.xml` and replace:
```xml
<string name="default_web_client_id">703565416770-PLACEHOLDER.apps.googleusercontent.com</string>
```

With your actual Web Client ID:
```xml
<string name="default_web_client_id">703565416770-YOUR_ACTUAL_CLIENT_ID.apps.googleusercontent.com</string>
```

## How to Fix Facebook Sign-In

### Step 1: Create a Facebook App

1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Click **My Apps** → **Create App**
3. Choose **Consumer** as app type
4. Fill in app details
5. Click **Create App**

### Step 2: Add Facebook Login Product

1. In your app dashboard, click **Add Product**
2. Find **Facebook Login** and click **Set Up**
3. Choose **Android** as platform

### Step 3: Configure Android Settings

1. Enter Package Name: `com.example.bmifrontend`
2. Enter Class Name: `com.example.bmifrontend.SignupActivity`
3. Get your Key Hash:
   - Open terminal in your project
   - Run: `keytool -exportcert -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore | openssl sha1 -binary | openssl base64`
   - Password is usually: `android`
   - Copy the hash and paste in Facebook settings

### Step 4: Get Your App ID

1. In Facebook App Dashboard, go to **Settings** → **Basic**
2. Copy your **App ID**

### Step 5: Update strings.xml and AndroidManifest.xml

Open `app/src/main/res/values/strings.xml` and replace:
```xml
<string name="facebook_app_id">YOUR_FACEBOOK_APP_ID</string>
<string name="fb_login_protocol_scheme">fbYOUR_FACEBOOK_APP_ID</string>
```

With your actual Facebook App ID:
```xml
<string name="facebook_app_id">123456789012345</string>
<string name="fb_login_protocol_scheme">fb123456789012345</string>
```

## Testing Email/Password Sign-Up

Email/Password registration should work now! The errors you saw were because:

1. **No Internet Permission** - Firebase couldn't connect to the server
2. **Poor Error Messages** - The error messages didn't show what went wrong

### Now the app will:
- ✅ Show detailed error messages
- ✅ Continue to MainActivity even if Firestore save fails (as long as Auth succeeds)
- ✅ Not crash on null values
- ✅ Handle Firebase connection errors gracefully

## Testing Guest Mode

Guest mode (Continue as Guest) should also work now with:
- ✅ Internet permission enabled
- ✅ Better error handling
- ✅ Firestore saves with fallback

## Common Errors and Solutions

### Error: "Failed to update profile"
**Cause**: Firebase Auth succeeded but profile update failed
**Solution**: The app now continues anyway and saves to Firestore

### Error: "Failed to save user data"
**Cause**: Firestore write failed (network issue or rules)
**Solution**: Check Firestore rules in Firebase Console:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow create: if request.auth != null;
    }
  }
}
```

### Error: "Google Sign-In not configured properly"
**Cause**: Missing or invalid Web Client ID
**Solution**: Follow "How to Fix Google Sign-In" steps above

### Error: "Facebook registration failed"
**Cause**: Missing Facebook App ID or incorrect configuration
**Solution**: Follow "How to Fix Facebook Sign-In" steps above

## Quick Test Checklist

- [ ] Email/Password Sign-Up works
- [ ] Guest mode works
- [ ] User data saves to Firestore
- [ ] App doesn't crash on errors
- [ ] Error messages are clear and helpful

## Need Help?

If you're still having issues:
1. Check Android Logcat for detailed error messages
2. Verify internet connection
3. Check Firebase Console for authentication logs
4. Ensure Firestore rules allow writes for authenticated users

