# Using Scala Slick on Android

## Intro

Slick takes a bit of work to get working on Android, but it's a million times better than the provided SQLite API on Android and most of the third party solutions that wrap the SQLite API. 

Some of the hiccups and solutions I found while setting it up:

1. Android does not provide (a public) JDBC driver. There is an internal one, but you should not use it.
2. There is a [third party](https://github.com/SQLDroid/SQLDroid) JDBC driver called SQLDroid that works. SQLDroid does not implement some functionality that Slick uses to automatically create foreign keys and indices  
   However, you can get around this issue by creating the tables through queries in Slick instead of Slick creating them
3. I also wanted to avoid relying on the built in SQLite implementation on Android, which is buggy and outdated on many older devices. Sqlite provides an up to date project [targeting Android](https://www.sqlite.org/android/doc/trunk/www/index.wiki). 
4. I included a SQLite wrapper jar and native binaries targeting all 32/64 bit architectures. Add the jar and the native libs if you want to use them
  
## Getting Started

1. Download and set up Intellij IDEA (see below for guide related to Android Scala). Feel free to use whatever editor/ide you want, but I will only answer questions about setting it up in Intellij IDEA and the steps below assume you are using it.

2. Set up Scala for Android either with [my guide](https://github.com/yareally/android-scala-intellij-no-sbt-plugin) or using the [SBT for Android](https://github.com/pfn/android-sdk-plugin) project. If using SBT, I will assume you know how to set it up and link local/remote libraries. Questions related to using SBT should be directed to the Google Groups [SBT group](https://groups.google.com/forum/#!forum/simple-build-tool) or the [Scala on Android](https://groups.google.com/forum/#!forum/scala-on-android) group.

3. If you check this project out from git in Intellij, it should mostly set itself up (after doing everything above), but you may still have to add the jars found in the libs directory. Native ones you will be built into the apk though as long as they stay where they currently are in the libs directory. Be sure to add the proguard project file or it won't compile.

4. For usage and an example that works so you can test if everything is set up, see the code attached to this project.


## Final Note

You might be wondering if Slick is reliable enough to use on Android. Sure, for your own personal apps meant as side projects. Though if you're working on something meant to be "enterprise grade" you're probably not using Scala on Android to begin with I would imagine.

If you run into issues, please create an issue in the Github project. Questions via email will be ignored because they're selfish and don't help others with the same question.