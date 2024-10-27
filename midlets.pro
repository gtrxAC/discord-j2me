-injars bin/in.jar
-outjars bin/out.jar
-libraryjars sdk/lib/midpapi20.jar
-libraryjars sdk/lib/cldcapi10.jar
-libraryjars sdk/lib/jsr75.jar
-libraryjars sdk/lib/javapiglerapi.jar
-printmapping bin/out.map

-microedition
-target 1.2
-dontoptimize

-keep public class * extends javax.microedition.midlet.MIDlet
-dontnote
-dontusemixedcaseclassnames
-repackageclasses ''
-overloadaggressively
-allowaccessmodification

-keep public class * implements language.Language