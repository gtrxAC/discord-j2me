-injars bin/in.jar
-outjars bin/out.jar
-libraryjars sdk/lib/midpapi10.jar
-libraryjars sdk/lib/cldcapi10.jar
-applymapping mapping.txt
-printmapping bin/out.map

-microedition
-target 1.2
-optimizations !code/simplification/object
# -dontobfuscate

-keep,allowobfuscation public class * extends javax.microedition.midlet.MIDlet
-dontnote
-dontusemixedcaseclassnames
-repackageclasses ''
-overloadaggressively
-allowaccessmodification