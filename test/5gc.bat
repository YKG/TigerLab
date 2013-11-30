@echo ==================================================
@echo test starting
@echo =============
java -cp ../bin Tiger -codegen C GCX.java
gcc ../runtime/runtime.c GCX.java.c -o GCX
GCX.exe
@echo =============
@echo test finished
@echo ==================================================
