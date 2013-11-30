@echo ==================================================
@echo test starting
@echo =============
java -cp ../bin Tiger -codegen C Triger_gc1.java
gcc ../runtime/runtime.c Triger_gc1.java.c -o Triger_gc1
Triger_gc1.exe
@echo =============
@echo test finished
@echo ==================================================
