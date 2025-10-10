-injars ../bin/in.jar
-outjars ../bin/out.jar
-libraryjars ../sdk/lib/midpapi20.jar
-libraryjars ../sdk/lib/cldcapi11.jar
-libraryjars ../sdk/lib/jsr75.jar
-libraryjars ../sdk/lib/javapiglerapi.jar
-libraryjars ../sdk/lib/nokiaui.jar

-microedition
-target 1.2

# Disabled optimizations:
# - code/simplification/object breaks NNJSON
# - method/inlining/unique breaks Pigler API when the device doesn't support it

//#ifdef DEBUG
-dontoptimize
-dontobfuscate
//#else
# Comment/uncomment below: multiple passes will take much longer to compile, but can be used when publishing a release
-optimizationpasses 5
//#ifdef PIGLER_SUPPORT
-optimizations !code/simplification/object,!method/inlining/unique
//#else
-optimizations !code/simplification/object
//#endif
//#endif

-keep,allowobfuscation public class * extends javax.microedition.midlet.MIDlet
-applymapping ../mapping.map
-dontnote
-dontusemixedcaseclassnames
-repackageclasses ''
-overloadaggressively
-allowaccessmodification