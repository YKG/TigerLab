@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do gcc ../runtime/runtime.c %%i.c -o %%~ni.exe
for %%i in (*.java) do %%~ni.exe >%%~ni.gc.txt
for %%i in (*.java) do fc %%~ni.j.txt %%~ni.gc.txt
@echo =============
@echo test finished
@echo ==================================================
