# JARVIS Android

A local-first Android JARVIS foundation.

## What works in this build

- Voice input through Android SpeechRecognizer
- Spoken replies through Android Text-to-Speech
- Direct opening of YouTube, WhatsApp, Instagram, Chrome, Camera, Calendar and Android Settings
- Generic installed-app launcher: `open <app name>` or `go to <app name>`
- Calls, SMS compose, alarms and timers through Android system intents
- AccessibilityService foundation for screen interaction
- No server or API key required for the basic command engine

## Important

To let JARVIS tap/type/scroll inside other apps, install the APK and enable:

Settings -> Accessibility -> JARVIS -> Allow

Android controls what an app can do. Some actions require runtime permission or a system confirmation screen.

## Example commands

- Open YouTube
- Go to WhatsApp
- Open Instagram
- Open Chrome
- Open Camera
- Open Calendar
- Open Settings
- Search YouTube for animal quiz
- Call 9876543210
- Message hello
- Set alarm
- Timer 60
- Go to YouTube

The next development layer can connect the voice command engine to the AccessibilityService so a command such as “Open YouTube and search animal quiz” can launch YouTube and then interact with its visible search controls.
