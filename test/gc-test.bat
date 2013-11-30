@echo ==================================================
@echo test starting
@echo =============
java -cp ../bin Tiger -codegen C GCTest.java
gcc ../runtime/runtime.c GCTest.java.c -o GCTest
GCTest.exe
@echo =============
@echo test finished
@echo ==================================================
