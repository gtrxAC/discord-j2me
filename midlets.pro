-injars bin/in.jar
-outjars bin/out.jar
-libraryjars sdk/lib/midpapi20.jar
-libraryjars sdk/lib/cldcapi10.jar
-libraryjars sdk/lib/jsr75.jar
-libraryjars sdk/lib/javapiglerapi.jar
-libraryjars sdk/lib/nokiaui.jar

-microedition
-target 1.2
-dontoptimize
# -dontobfuscate

-keep public class * extends javax.microedition.midlet.MIDlet
-applymapping mapping.map
-dontnote
-dontusemixedcaseclassnames
-repackageclasses ''
-overloadaggressively
-allowaccessmodification