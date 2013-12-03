@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do fc %%~ni.j.txt %%~ni.bytecode.txt
@echo =============
@echo test finished
@echo ==================================================
