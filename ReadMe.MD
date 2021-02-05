As the layout is using Constraint Layout as part of the AAR packaged.
In your kony project, add the below entry so you are not running into any build issue.


Project settings --> native --> android --> Gradle Entries --> build.gradle suffix entries.

dependencies.implementation'androidx.constraintlayout:constraintlayout:2.0.4'
