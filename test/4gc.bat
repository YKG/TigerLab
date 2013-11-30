@echo ==================================================
@echo test starting
@echo =============
java -cp ../bin Tiger -codegen C Triger_gc2.java
gcc ../runtime/runtime.c Triger_gc2.java.c -o Triger_gc2
Triger_gc2.exe
@echo =============
@echo test finished
@echo ==================================================
