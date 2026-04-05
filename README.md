# SkillLink

SkillLink is a freelancer marketplace built for Android. The idea came from a gap I kept noticing — local service providers have no good way to connect with clients nearby, and the big platforms are either overkill or not accessible to everyone. This project is an attempt to solve that, at least at a small scale.

It's a full-stack Android app written in Java, using SQLite for local user data and Firebase for everything that needs to be shared across devices.

---

## What it does

The app has two sides — clients looking to hire, and freelancers offering services. Both go through the same login screen, and the experience branches from there. There's a third role too: admin, which uses a separate Firebase-authenticated OTP flow instead of the standard password login.

Once inside, users can browse gigs, filter by category, search in real time, and post their own listings. Freelancers can't accept their own gigs, which is one of those small details that matters more than it sounds. The admin panel pulls user data from SQLite and gig data from Firebase and lets you manage both from one place.

A few other things worth mentioning:

There's an SMS-based status control system. Sending `activate <id>` or `deactivate <id>` to the device toggles a freelancer's availability without opening the app. It's niche, but it's genuinely useful in low-connectivity situations.

The app runs a foreground music service with notification playback controls. Background ambient audio, essentially — you can toggle it from the toolbar. It's aesthetic, not functional, but it adds something to the experience.

Maps integration pulls in nearby freelancers and lets you pin gig locations. Firebase Cloud Messaging handles push notifications for things like gig updates and new messages.

---

## Tech stack

- Native Android, Java
- Min SDK 24, Target SDK 36
- SQLite for user management
- Firebase Realtime Database for gigs
- Firebase Authentication for admin OTP
- Material 3, ViewPager2, RecyclerView, CardView
- FCM, Foreground Services, Broadcast Receivers

---

## Setup

Clone the repo:

```bash
git clone https://github.com/SpoodermanCodes/fiverr-clone.git
```

Firebase: drop your `google-services.json` into the `app/` directory. In the Firebase console, enable Realtime Database and Phone Authentication.

Maps: add your Maps SDK key to `local.properties` as:

```
MAPS_API_KEY=YOUR_KEY
```

Then open the project in Android Studio, sync Gradle, and run it on a device or emulator. A physical device works better for testing SMS and location features.

---

## User flow

Splash screen comes up first, then you choose to log in or register. From there, clients and freelancers land on the marketplace dashboard. Admin login is separate and goes through OTP. Background music and SMS control work independently of which role you're in.

---

## License

Academic and demo use only. Not intended for commercial deployment.

---

## Note

The SMS system requires the app to have SMS receive permissions granted on the device. On Android 10 and above, this may not work on emulators — test it on a real device. The OTP flow for admin is simulated in the current build; it logs the OTP to console rather than sending an actual SMS, so don't expect it to work like a production auth system out of the box.

Maps won't render without a valid API key. If you're just testing the UI, you can stub out the map fragment temporarily — the rest of the app runs fine without it.

---

## Note to self

The SQLite schema needs a migration strategy if the user table ever changes — right now it just drops and recreates on version bump, which obviously wipes all local data. That's fine for a demo but would be a problem in any real deployment.

The music service sometimes doesn't release the audio focus properly when the app is killed from the recents tray. Worth looking into `AudioManager.abandonAudioFocus()` being called in the right lifecycle hook.

Also, the gig filtering logic on the dashboard is doing client-side filtering on a full Firebase fetch. That's fine at small scale but will get slow. Firebase queries with `orderByChild` and `equalTo` would fix this when the dataset grows.
